package com.cxy.geohash;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.cxy.data.GetConnection;

public class GeoHash {
	private LocationBean location;
	/**
	 * 1 2500km;2 630km;3 78km;4 30km 5 2.4km; 6 610m; 7 76m; 8 19m
	 */
	private int hashLength = 8; // ��γ��ת��Ϊgeohash����
	private int latLength = 20; // γ��ת��Ϊ�����Ƴ���
	private int lngLength = 20; // ����ת��Ϊ�����Ƴ���

	private double minLat;// ÿ��γ�ȵĵ�λ��С
	private double minLng;// ÿ�����ȵĵ���
	private static final char[] CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
			'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	public GeoHash() {
		location = new LocationBean();
		setMinLatLng();
	}

	public GeoHash(double lat, double lng) {
		location = new LocationBean(lat, lng);
		setMinLatLng();
	}

	public int gethashLength() {
		return hashLength;
	}

	/**
	 * @Author:lulei
	 * @Description: ���þ�γ�ȵ���С��λ
	 */
	private void setMinLatLng() {
		minLat = LocationBean.MAXLAT - LocationBean.MINLAT;
		for (int i = 0; i < latLength; i++) {
			minLat /= 2.0;
		}
		minLng = LocationBean.MAXLNG - LocationBean.MINLNG;
		for (int i = 0; i < lngLength; i++) {
			minLng /= 2.0;
		}
	}

	/**
	 * @return
	 * @Author:lulei
	 * @Description: ����������㼰��Χ����ɵľŸ�
	 */
	public List<String> getGeoHashBase32For9() {
		double leftLat = location.getLat() - minLat;
		double rightLat = location.getLat() + minLat;
		double upLng = location.getLng() - minLng;
		double downLng = location.getLng() + minLng;
		List<String> base32For9 = new ArrayList<String>();
		// �����ϵ��� 3��
		String leftUp = getGeoHashBase32(leftLat, upLng);
		if (!(leftUp == null || "".equals(leftUp))) {
			base32For9.add(leftUp);
		}
		String leftMid = getGeoHashBase32(leftLat, location.getLng());
		if (!(leftMid == null || "".equals(leftMid))) {
			base32For9.add(leftMid);
		}
		String leftDown = getGeoHashBase32(leftLat, downLng);
		if (!(leftDown == null || "".equals(leftDown))) {
			base32For9.add(leftDown);
		}
		// �м���ϵ��� 3��
		String midUp = getGeoHashBase32(location.getLat(), upLng);
		if (!(midUp == null || "".equals(midUp))) {
			base32For9.add(midUp);
		}
		String midMid = getGeoHashBase32(location.getLat(), location.getLng());
		if (!(midMid == null || "".equals(midMid))) {
			base32For9.add(midMid);
		}
		String midDown = getGeoHashBase32(location.getLat(), downLng);
		if (!(midDown == null || "".equals(midDown))) {
			base32For9.add(midDown);
		}
		// �Ҳ���ϵ��� 3��
		String rightUp = getGeoHashBase32(rightLat, upLng);
		if (!(rightUp == null || "".equals(rightUp))) {
			base32For9.add(rightUp);
		}
		String rightMid = getGeoHashBase32(rightLat, location.getLng());
		if (!(rightMid == null || "".equals(rightMid))) {
			base32For9.add(rightMid);
		}
		String rightDown = getGeoHashBase32(rightLat, downLng);
		if (!(rightDown == null || "".equals(rightDown))) {
			base32For9.add(rightDown);
		}
		return base32For9;
	}

	/**
	 * @param length
	 * @return
	 * @Author:lulei
	 * @Description: ���þ�γ��ת��Ϊgeohash����
	 */
	public boolean sethashLength(int length) {
		if (length < 1) {
			return false;
		}
		hashLength = length;
		latLength = (length * 5) / 2;
		if (length % 2 == 0) {
			lngLength = latLength;
		} else {
			lngLength = latLength + 1;
		}
		setMinLatLng();
		return true;
	}

	/**
	 * @return
	 * @Author:lulei
	 * @Description: ��ȡ��γ�ȵ�base32�ַ���
	 */
	public String getGeoHashBase32() {
		return getGeoHashBase32(location.getLat(), location.getLng());
	}

	/**
	 * @param lat
	 * @param lng
	 * @return
	 * @Author:lulei
	 * @Description: ��ȡ��γ�ȵ�base32�ַ���
	 */
	public String getGeoHashBase32(double lat, double lng) {
		boolean[] bools = getGeoBinary(lat, lng);
		if (bools == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bools.length; i = i + 5) {
			boolean[] base32 = new boolean[5];
			for (int j = 0; j < 5; j++) {
				base32[j] = bools[i + j];
			}
			char cha = getBase32Char(base32);
			if (' ' == cha) {
				return null;
			}
			sb.append(cha);
		}
		return sb.toString();
	}

	/**
	 * @param base32
	 * @return
	 * @Author:lulei
	 * @Description: ����λ������ת��Ϊbase32
	 */
	private char getBase32Char(boolean[] base32) {
		if (base32 == null || base32.length != 5) {
			return ' ';
		}
		int num = 0;
		for (boolean bool : base32) {
			num <<= 1;
			if (bool) {
				num += 1;
			}
		}
		return CHARS[num % CHARS.length];
	}

	/**
	 * @param lat
	 * @param lng
	 * @return
	 * @Author:lulei
	 * @Description: ��ȡ�����geo�������ַ���
	 */
	private boolean[] getGeoBinary(double lat, double lng) {
		boolean[] latArray = getHashArray(lat, LocationBean.MINLAT, LocationBean.MAXLAT, latLength);
		boolean[] lngArray = getHashArray(lng, LocationBean.MINLNG, LocationBean.MAXLNG, lngLength);
		return merge(latArray, lngArray);
	}

	/**
	 * @param latArray
	 * @param lngArray
	 * @return
	 * @Author:lulei
	 * @Description: �ϲ���γ�ȶ�����
	 */
	private boolean[] merge(boolean[] latArray, boolean[] lngArray) {
		if (latArray == null || lngArray == null) {
			return null;
		}
		boolean[] result = new boolean[lngArray.length + latArray.length];
		Arrays.fill(result, false);
		for (int i = 0; i < lngArray.length; i++) {
			result[2 * i] = lngArray[i];
		}
		for (int i = 0; i < latArray.length; i++) {
			result[2 * i + 1] = latArray[i];
		}
		return result;
	}

	/**
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 * @Author:lulei
	 * @Description: ������ת��Ϊgeohash�������ַ���
	 */
	private boolean[] getHashArray(double value, double min, double max, int length) {
		if (value < min || value > max) {
			return null;
		}
		if (length < 1) {
			return null;
		}
		boolean[] result = new boolean[length];
		for (int i = 0; i < length; i++) {
			double mid = (min + max) / 2.0;
			if (value > mid) {
				result[i] = true;
				min = mid;
			} else {
				result[i] = false;
				max = mid;
			}
		}
		return result;
	}

	public void insert() {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id,latitude,longitude from checkintwi";
		HashMap<Integer, String> map = new HashMap<>();
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();

			while (rs.next()) {
				int id = rs.getInt(1);
				double latitude = rs.getDouble(2);
				double longitude = rs.getDouble(3);
				location.setLat(latitude);
				location.setLng(longitude);
				// System.out.println(id + " " + getGeoHashBase32());
				map.put(id, getGeoHashBase32());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("GeoHash is done");
		String sql2 = "update checkintwi set geohash=? where id=?";
		int count = 0;
		try {
			ps = con.prepareStatement(sql2);
			for (Entry<Integer, String> entry : map.entrySet()) {
				ps.setString(1, entry.getValue());
				ps.setInt(2, entry.getKey());
				ps.executeUpdate();
				if (count++ % 1000 == 0)
					System.out.println(count);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				ps.close();
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {

		// GeoHash g = new GeoHash();
		// // GeoHash g = new GeoHash(40.222012, 116.248283);
		// System.out.println(g.getGeoHashBase32(40.222012, 116.248283));
		// g.insert();
	}
}

package com.cxy.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class UpdateLocation {
	static Connection con = GetConnection.getCon();

	public static void update() {
		String sql = "select venue_id,location_id from checkinfour";
		PreparedStatement ps = null;
		ResultSet rs = null;
		HashMap<String, Integer> map = new HashMap<>();
		int value = 1;
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				String venueid = rs.getString(1);
				if (!map.containsKey(venueid)) {
					map.put(venueid, value++);
				}
			}
			rs.close();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String sql2 = "update checkinfour set location_id=? where venue_id=?";
		String sql3 = "select venue_id,location_id from checkinfour where id>257577";
		PreparedStatement ps2 = null;
		try {
			ps = con.prepareStatement(sql3);
			rs = ps.executeQuery();
			ps2 = con.prepareStatement(sql2);
			while (rs.next()) {
				String venue_id = rs.getString(1);
				// System.out.println(venue_id);
				String locationid = rs.getString(2);
				if (locationid == null) {
					int location_id = map.get(venue_id);
					ps2.setInt(1, location_id);
					ps2.setString(2, venue_id);
					ps2.executeUpdate();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		update();
	}
}

package com.cxy.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class RecodeLocationID {
	public static void main(String[] args) throws Exception {
		Connection connection = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select geohash from checkintwi";
		ps = connection.prepareStatement(sql);
		rs = ps.executeQuery();
		HashMap<String, Integer> map = new LinkedHashMap<>();
		int index = 0;
		while (rs.next()) {
			String geohash = rs.getString(1);
			if (!map.containsKey(geohash)) {
				map.put(geohash, index++);
			}
		}
		// System.out.println(map);
		sql = "update checkintwi set location_id=? where geohash=?";
		ps = connection.prepareStatement(sql);
		for (Entry<String, Integer> entry : map.entrySet()) {
			ps.setInt(1, entry.getValue());
			ps.setString(2, entry.getKey());
			ps.execute();
		}
		connection.close();
	}
}

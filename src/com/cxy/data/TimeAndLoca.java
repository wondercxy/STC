package com.cxy.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TimeAndLoca {
	public static HashMap<Integer, Set<Integer>> timeandloca() throws Exception {
		HashMap<Integer, Set<Integer>> map = new HashMap<>();

		Connection con = GetConnection.getCon();
		String sql = "select time,location_id from checkinnew";
		PreparedStatement ps = null;
		ResultSet rs = null;
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();

		while (rs.next()) {
			// System.out.println(rs.getString(1));
			int hour = Integer.parseInt(rs.getString(1).split(" ")[1].split(":")[0]);
			int loca = rs.getInt(2);
			if (map.containsKey(hour)) {
				Set<Integer> set = map.get(hour);
				set.add(loca);
				map.put(hour, set);
			} else {
				Set<Integer> set = new HashSet<>();
				set.add(loca);
				map.put(hour, set);
			}
		}

		rs.close();
		ps.close();
		con.close();

		System.out.println("TimeAndLoca is done!");
		return map;
	}

}

package com.cxy.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class TimeAndContent {
	public static HashMap<Integer, List<String>> timeandcontent() throws Exception {
		HashMap<Integer, List<String>> map = new HashMap<>();

		Connection con = GetConnection.getCon();
		String sql = "select time,content from checkinnew";
		PreparedStatement ps = null;
		ResultSet rs = null;

		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();

		while (rs.next()) {
			int hour = Integer.parseInt(rs.getString(1).split(" ")[1].split(":")[0]);
			String content = rs.getString(2);
			if (map.containsKey(hour)) {
				List<String> list = map.get(hour);
				list.add(content);
				map.put(hour, list);
			} else {
				List<String> list = new ArrayList<>();
				list.add(content);
				map.put(hour, list);
			}
		}

		rs.close();
		ps.close();
		con.close();

		return map;
	}

	public static HashMap<Integer, List<String>> timecontent() throws Exception {
		HashMap<Integer, List<String>> map = new HashMap<>();

		Connection con = GetConnection.getCon();
		String sql = "select time,content from checkinnew";
		PreparedStatement ps = null;
		ResultSet rs = null;

		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();

		while (rs.next()) {
			int hour = Integer.parseInt(rs.getString(1).split(" ")[1].split(":")[0]);
			String content = rs.getString(2);
			if (map.containsKey(hour)) {
				List<String> list = map.get(hour);
				for (String s : content.split(","))
					list.add(s);
				map.put(hour, list);
			} else {
				List<String> list = new ArrayList<>();
				for (String s : content.split(","))
					list.add(s);
				map.put(hour, list);
			}
		}

		rs.close();
		ps.close();
		con.close();

		return map;
	}

	public static void main(String[] args) throws Exception {
		HashMap<Integer, List<String>> map = timeandcontent();
		String path = "F:\\WorkSpace\\JIM\\JIM\\input\\deal\\time\\";
		BufferedWriter writer = null;
		for (Entry<Integer, List<String>> entry : map.entrySet()) {
			int t = entry.getKey();
			writer = new BufferedWriter(new FileWriter(path + t + ".txt"));
			List<String> list = entry.getValue();
			for (String s : list) {
				writer.write(s + "\r\n");
			}
			writer.flush();
			writer.close();
		}
	}
}

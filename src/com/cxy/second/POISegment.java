package com.cxy.second;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import com.cxy.data.GetConnection;

public class POISegment {
	public HashMap<Integer, ArrayList<ArrayList<Integer>>> user_poiseg = new HashMap<>();
	public HashMap<Integer, HashMap<String, ArrayList<String>>> tmp = new HashMap<>();

	public void getFromDataBase() throws Exception {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select user_id,time,location_id from checkintwitrain";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		while (rs.next()) {
			int user_id = rs.getInt(1);
			String[] time = rs.getString(2).split(" ");
			String date = time[0];
			int hour = Integer.valueOf(time[1].split(":")[0]);
			int loca_id = rs.getInt(3);
			if (tmp.containsKey(user_id)) {
				HashMap<String, ArrayList<String>> map = tmp.get(user_id);
				if (map.containsKey(date)) {
					map.get(date).add(hour + " " + loca_id);
				} else {
					ArrayList<String> list = new ArrayList<>();
					list.add(hour + " " + loca_id);
					map.put(date, list);
				}
			} else {
				HashMap<String, ArrayList<String>> map = new HashMap<>();
				ArrayList<String> list = new ArrayList<>();
				list.add(hour + " " + loca_id);
				map.put(date, list);
				tmp.put(user_id, map);
			}
		}
	}

	public HashMap<Integer, ArrayList<ArrayList<Integer>>> dealPOISeg(int timeInterval) throws Exception {
		getFromDataBase();
		for (Entry<Integer, HashMap<String, ArrayList<String>>> entry : tmp.entrySet()) {
			int uid = entry.getKey();
			HashMap<String, ArrayList<String>> map = entry.getValue();
			for (Entry<String, ArrayList<String>> en : map.entrySet()) {
				ArrayList<String> list = en.getValue();
				Collections.sort(list);
				for (int i = 0; i < list.size(); i++) {
					ArrayList<Integer> res = new ArrayList<>();
					// res.add(Integer.valueOf(list.get(i).split(" ")[1]));
					while (i + 1 < list.size() && Integer.valueOf(list.get(i + 1).split(" ")[0])
							- Integer.valueOf(list.get(i).split(" ")[0]) <= timeInterval) {
						if (Integer.valueOf(list.get(i).split(" ")[1]) == Integer
								.valueOf(list.get(i + 1).split(" ")[1])) {
							i++;
							continue;
						}
						res.add(Integer.valueOf(list.get(++i).split(" ")[1]));
					}
					if (user_poiseg.containsKey(uid)) {
						user_poiseg.get(uid).add(res);
					} else {
						ArrayList<ArrayList<Integer>> list2 = new ArrayList<>();
						list2.add(res);
						user_poiseg.put(uid, list2);
					}
				}
			}
		}
		return user_poiseg;
	}

	public static void main(String[] args) throws Exception {
		POISegment poi = new POISegment();
		poi.getFromDataBase();
		poi.dealPOISeg(24);
		for (Entry<Integer, ArrayList<ArrayList<Integer>>> entry : poi.user_poiseg.entrySet())
			System.out.println(entry.getKey() + " " + entry.getValue());
	}

}

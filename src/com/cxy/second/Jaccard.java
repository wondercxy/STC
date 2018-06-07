package com.cxy.second;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.cxy.data.GetConnection;

public class Jaccard {
	HashMap<Integer, HashMap<Integer, Integer>> user_location = new HashMap<>();
	HashMap<Integer, Integer> count = new HashMap<>();

	public void jaccard() throws Exception {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select user_id,location_id from checkintwi";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		while (rs.next()) {
			int user_id = rs.getInt(1);
			int location_id = rs.getInt(2);
			if (user_location.containsKey(user_id)) {
				HashMap<Integer, Integer> map = user_location.get(user_id);
				if (map.containsKey(location_id))
					map.put(location_id, map.get(location_id) + 1);
				else
					map.put(location_id, 1);
			} else {
				HashMap<Integer, Integer> map = new HashMap<>();
				map.put(location_id, 1);
				user_location.put(user_id, map);
			}
			if (count.containsKey(user_id))
				count.put(user_id, count.get(user_id) + 1);
			else
				count.put(user_id, 1);
		}
	}

	public void getSim() {
		HashMap<Integer, TreeMap<Double, Integer>> result = new HashMap<>();
		for (Entry<Integer, HashMap<Integer, Integer>> entry : user_location.entrySet()) {
			int u1 = entry.getKey();
			for (Entry<Integer, HashMap<Integer, Integer>> entry2 : user_location.entrySet()) {
				int u2 = entry2.getKey();
				if (u1 != u2) {
					HashMap<Integer, Integer> l1 = entry.getValue();
					HashMap<Integer, Integer> l2 = entry2.getValue();
					int size = count.get(u1) + count.get(u2);
					int sum = 0;
					for (Entry<Integer, Integer> e : l1.entrySet()) {
						int locationid = e.getKey();
						if (l2.containsKey(locationid)) {
							sum += Math.abs(e.getValue() - l2.get(locationid)) * 2;
						}
					}
					double score = Double.valueOf(sum) / Double.valueOf(size);
					if (result.containsKey(u1)) {
						TreeMap<Double, Integer> treeMap = result.get(u1);
						treeMap.put(score, u2);
					} else {
						TreeMap<Double, Integer> treeMap = new TreeMap<>();
						treeMap.put(score, u2);
						result.put(u1, treeMap);
					}
				}
			}
		}
		for (Entry<Integer, TreeMap<Double, Integer>> entry : result.entrySet()) {
			int c = 0;
			String text = entry.getKey() + " ";
			for (Entry<Double, Integer> e : entry.getValue().descendingMap().entrySet()) {
				if (c++ < 20) {
					text += e.getValue() + "-" + e.getKey() + " ";
				}
			}
			System.out.println(text);
		}
	}

	public static void main(String[] args) throws Exception {
		Jaccard jaccard = new Jaccard();
		jaccard.jaccard();
		jaccard.getSim();
	}
}

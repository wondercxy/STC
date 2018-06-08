package com.cxy.second;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import com.cxy.data.GetConnection;

public class TA {
	public HashMap<Integer, TreeMap<Double, Integer>> user_jaccard;
	public HashMap<Integer, TreeSet<Integer>> user_allpoints;
	public HashMap<Integer, HashSet<Integer>> user_location;
	public HashMap<Integer, HashMap<Integer, HashMap<Double, Integer>>> user_rankindex;
	public HashMap<Integer, HashMap<Integer, HashSet<Integer>>> user_hour_location;
	public HashMap<Integer, Double[]> location_ll;
	public HashMap<Integer, String> location_content;

	public void init() throws Exception {
		Jaccard jaccard = new Jaccard();
		user_jaccard = jaccard.getSim();
		user_rankindex = new HashMap<>();
		user_allpoints = new HashMap<>();
		user_location = new HashMap<>();
		user_hour_location = new HashMap<>();
		location_content = new HashMap<>();
		location_ll = new HashMap<>();
	}

	public void contentList() throws Exception {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select user_id,time,location_id,latitude,longitude,content from checkinnew";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		while (rs.next()) {
			int user_id = rs.getInt(1);
			int hour = Integer.valueOf(rs.getString(2).split(" ")[1].split(":")[0]);
			int loca_id = rs.getInt(3);
			double lat = rs.getDouble(4);
			double log = rs.getDouble(5);
			String content = rs.getString(6);
			if (user_location.containsKey(user_id)) {
				HashSet<Integer> set = user_location.get(user_id);
				set.add(loca_id);
			} else {
				HashSet<Integer> set = new HashSet<>();
				set.add(loca_id);
				user_location.put(user_id, set);
			}
			if (user_hour_location.containsKey(user_id)) {
				HashMap<Integer, HashSet<Integer>> map = user_hour_location.get(user_id);
				if (map.containsKey(hour)) {
					HashSet<Integer> set = map.get(hour);
					set.add(loca_id);
				} else {
					HashSet<Integer> set = new HashSet<>();
					set.add(loca_id);
					map.put(hour, set);
				}
			} else {
				HashMap<Integer, HashSet<Integer>> map = new HashMap<>();
				HashSet<Integer> set = new HashSet<>();
				set.add(loca_id);
				map.put(hour, set);
				user_hour_location.put(user_id, map);
			}
			if (!location_ll.containsKey(loca_id)) {
				Double[] ll = { lat, log };
				location_ll.put(loca_id, ll);
				location_content.put(loca_id, content);
			}
		}
	}

	public void user_AllPoint(int userid) {
		TreeMap<Double, Integer> treeMap = user_jaccard.get(userid);
		int count = 0;
		for (Entry<Double, Integer> entry : treeMap.descendingMap().entrySet()) {
			int uid = entry.getValue();
			if (user_allpoints.containsKey(userid)) {
				user_allpoints.get(userid).addAll(user_location.get(uid));
			} else {
				TreeSet<Integer> set = new TreeSet<>();
				set.addAll(user_location.get(uid));
				user_allpoints.put(userid, set);
			}
			if (count++ > 20)
				break;
		}
	}

	public HashSet<Integer> exploreTextual(String content, int userid, int iter) {
		HashSet<Integer> textSet = new HashSet<>();
		TreeMap<Double, Integer> treeMap = new TreeMap<>();

		TFIDF tfidf = new TFIDF();
		for (int loca : user_allpoints.get(userid)) {
			tfidf.get_tfidf(location_content.get(loca), ",");
			double value = tfidf.get_tfidf4test(content, ",");
			treeMap.put(value, loca);
		}

		System.out.println(treeMap);

		return textSet;
	}

	public static void main(String[] args) throws Exception {
		TA ta = new TA();
		ta.init();
		ta.contentList();
		ta.user_AllPoint(2);
		// ta.exploreTextual("Travel & Transport", 2, 0);
		// System.out.println(ta.user_allpoints.get(2));
	}
}

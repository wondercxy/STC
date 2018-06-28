package com.cxy.second;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.cxy.data.GetConnection;
import com.cxy.model.Point;

public class TA {
	public HashMap<Integer, ArrayList<Integer>> user_jaccard;
	public HashMap<Integer, TreeSet<Integer>> user_allpoints;
	public HashMap<Integer, HashSet<Integer>> user_location;
	public HashMap<Integer, TreeMap<Integer, Double>> user_postlist;
	public HashMap<Integer, HashMap<Integer, HashSet<Integer>>> user_hour_location;
	public HashMap<Integer, Point> location_ll;
	public HashMap<Integer, String> location_geohash;
	public HashMap<Integer, String> location_content;

	public HashMap<Integer, ArrayList<ArrayList<Integer>>> user_poiseg;
	public HashMap<Integer, HashSet<ArrayList<Integer>>> user_simSeg;

	HashMap<Integer, HashSet<Integer>> postlist;
	HashMap<Integer, Double> postlistValue;
	HashMap<Integer, HashSet<Integer>> gridindex;
	HashMap<Integer, Double> gridindexValue;

	public double Dmax;
	public int iter;
	public int timeInterval;

	public void init() throws Exception {
		user_jaccard = new HashMap<>();
		user_postlist = new HashMap<>();
		user_allpoints = new HashMap<>();
		user_location = new HashMap<>();
		user_hour_location = new HashMap<>();
		location_content = new HashMap<>();
		location_geohash = new HashMap<>();
		location_ll = new HashMap<>();

		user_poiseg = new HashMap<>();
		user_simSeg = new HashMap<>();

		postlist = new HashMap<>();
		gridindex = new HashMap<>();
		postlistValue = new HashMap<>();
		gridindexValue = new HashMap<>();

		Dmax = 0;
		iter = 5;
		timeInterval = 24;

		getFromDataBase();
	}

	public void loadJaccard() throws Exception {
		String path = "F:\\WorkSpace\\JIM\\JIM\\input\\deal\\cosinetwi.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] s = line.split(" ");
			int uid = Integer.valueOf(s[0]);
			ArrayList<Integer> list = new ArrayList<>();
			user_jaccard.put(uid, list);
			for (int i = 1; i < s.length; i++) {
				user_jaccard.get(uid).add(Integer.valueOf(s[i]));
			}
		}
		br.close();
	}

	public void getFromDataBase() throws Exception {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select user_id,time,location_id,latitude,longitude,content,geohash from checkintwi";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		while (rs.next()) {
			int user_id = rs.getInt(1);
			int hour = Integer.valueOf(rs.getString(2).split(" ")[1].split(":")[0]);
			int loca_id = rs.getInt(3);
			double lat = rs.getDouble(4);
			double log = rs.getDouble(5);
			String content = rs.getString(6);
			String geohash = rs.getString(7);
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
				Point point = new Point(lat, log, loca_id);
				location_ll.put(loca_id, point);
				location_content.put(loca_id, content);
			}
			location_geohash.put(loca_id, geohash);
		}
	}

	public void user_AllPoint() {
		for (int i = 0; i <= 61408; i++) {
			if (user_jaccard.containsKey(i)) {
				ArrayList<Integer> list = user_jaccard.get(i);
				int count = 0;
				for (int id : list) {
					if (user_allpoints.containsKey(i)) {
						user_allpoints.get(i).addAll(user_location.get(id));
					} else {
						TreeSet<Integer> set = new TreeSet<>();
						set.addAll(user_location.get(id));
						user_allpoints.put(i, set);
					}
					if (count++ > 20) {
						break;
					}
				}
				for (int id : list) {
					if (user_simSeg.containsKey(i)) {
						user_simSeg.get(i).addAll(user_poiseg.get(id));
					} else {
						HashSet<ArrayList<Integer>> set = new HashSet<>();
						set.addAll(user_poiseg.get(id));
						user_simSeg.put(i, set);
					}
				}
			}
		}
	}

	public void exploreTextual(String content, int userid, String token) {
		postlist.clear();
		HashMap<Integer, String> map = new HashMap<>();
		for (int loca : user_allpoints.get(userid)) {
			// TFIDF.get_tfidf(location_content.get(loca), "\\|");
			// double value = TFIDF.get_tfidf4test(content, "\\|");
			map.put(loca, location_content.get(loca));
		}

		HashMap<Integer, Double> hashMap = (HashMap<Integer, Double>) sortByValue(TFIDF2.count(map, content, token));

		double max = 0;
		double min = 0;
		for (Entry<Integer, Double> entry : hashMap.entrySet()) {
			max = Math.max(max, entry.getValue());
			min = Math.min(min, entry.getValue());
		}

		for (int i = 0; i < iter; i++) {
			HashSet<Integer> set = new HashSet<>();
			// System.out.println((max - min) / 6 * (iter - i - 1) + min);
			postlistValue.put(i, (max - min) / 6 * (iter - i - 1) + min);
			for (Entry<Integer, Double> entry : hashMap.entrySet()) {
				if (i == 0) {
					if (entry.getValue() >= postlistValue.get(i)) {
						set.add(entry.getKey());
					}
				} else {
					if (entry.getValue() >= postlistValue.get(i) && entry.getValue() < postlistValue.get(i - 1)) {
						set.add(entry.getKey());
					}
				}
			}
			postlist.put(i, set);
		}

	}

	public void exploreSpatial(String geohash, double lat, double log) {
		gridindex.clear();

		HashSet<Integer> set = new HashSet<>();
		String pattern = geohash.substring(0, 8 - iter) + "(\\w*)";
		// System.out.println(pattern);
		for (Entry<Integer, String> entry : location_geohash.entrySet()) {
			if (Pattern.matches(pattern, entry.getValue())) {
				set.add(entry.getKey());
			}
		}

		Point point = new Point(lat, log, -1);
		for (int i : set) {
			Dmax = Math.max(Dmax, getDistance(location_ll.get(i), point));
		}
		System.out.println(Dmax);
		for (int i = 0; i < iter; i++) {
			HashSet<Integer> hs = new HashSet<>();
			gridindexValue.put(i, (Dmax - (Dmax / iter) * (i + 1)) / Dmax);
			for (int j : set) {
				if (i == 0) {
					if ((Dmax - getDistance(location_ll.get(j), point)) / Dmax > gridindexValue.get(i))
						hs.add(j);
				} else {
					if ((Dmax - getDistance(location_ll.get(j), point)) / Dmax >= gridindexValue.get(i)
							&& (Dmax - getDistance(location_ll.get(j), point)) / Dmax < gridindexValue.get(i - 1)) {
						hs.add(j);
					}
				}
			}
			gridindex.put(i, hs);
		}
	}

	public HashSet<Integer> iteratorExplore(int iter) {
		HashSet<Integer> set = new HashSet<>();
		return set;
	}

	public void result(int uid) throws Exception {
		int it = 0;
		HashSet<Integer> rs = new HashSet<>();
		HashSet<Integer> rankIt = new HashSet<>();

		// while (it < iter) {
		rankIt.addAll(postlist.get(it));
		rankIt.addAll(gridindex.get(it));
		System.out.println(rankIt);
		HashSet<ArrayList<Integer>> Ctra = new HashSet<>();
		for (ArrayList<Integer> l : user_simSeg.get(uid)) {
			for (int i : rankIt) {
				if (l.contains(i)) {
					Ctra.add(l);
					break;
				}
			}
		}
		System.out.println(Ctra);

		// it++;
		// }
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> rank) {
		List<Map.Entry<K, V>> list = new ArrayList<>(rank.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {

			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				// TODO Auto-generated method stub
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public double getDistance(Point a, Point b) {
		Double R = new Double(6371);
		Double dlat = (b.getX() - a.getX()) * Math.PI / 180;
		Double dlon = (b.getY() - a.getY()) * Math.PI / 180;
		Double aDouble = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.cos(a.getX() * Math.PI / 180)
				* Math.cos(b.getX() * Math.PI / 180) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
		Double cDouble = 2 * Math.atan2(Math.sqrt(aDouble), Math.sqrt(1 - aDouble));
		double d = Math.round((R * cDouble) * 1000);
		return d;
	}

	public static void main(String[] args) throws Exception {
		TA ta = new TA();
		ta.init();
		// content
		ta.loadJaccard();
		POISegment poiSegment = new POISegment();
		ta.user_poiseg = poiSegment.dealPOISeg(ta.timeInterval);
		ta.user_AllPoint();
		ta.exploreSpatial("dqcm6fvj", 38.90773, -76.86443);
		for (Entry<Integer, HashSet<Integer>> entry : ta.gridindex.entrySet()) {

			System.out.println(entry.getKey() + " " + entry.getValue());
		}

		ta.exploreTextual("food|court|ideas|meatballs|self|checkout|swedish|furniture|home|", 33, "\\|");
		// for (Entry<Integer, HashSet<Integer>> entry : ta.postlist.entrySet()) {
		//
		// System.out.println(entry.getKey() + " " + entry.getValue());
		// }

		ta.result(33);

		// location

	}
}

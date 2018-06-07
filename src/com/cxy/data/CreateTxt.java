package com.cxy.data;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

public class CreateTxt {
	static TreeMap<Integer, TreeMap<Integer, Integer>> check = new TreeMap<>();
	static TreeMap<Integer, HashMap<Integer, Integer>> check2 = new TreeMap<>();
	static TreeMap<Integer, HashMap<Integer, Integer>> count = new TreeMap<>();

	public static void dealTT(int flag) throws Exception {
		BufferedWriter bwTrain = null;
		BufferedWriter bwTest = null;
		if (flag == 1) {
			bwTrain = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("C:\\Users\\HeyGo\\Desktop\\Foursquare\\Foursquare\\Foursquare_train.txt")));
			bwTest = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("C:\\Users\\HeyGo\\Desktop\\Foursquare\\Foursquare\\Foursquare_test.txt")));
			for (Entry<Integer, HashMap<Integer, Integer>> entry : count.entrySet()) {
				int size = (int) (entry.getValue().size() * 0.7);
				int index = 0;
				for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
					if (index < size) {
						bwTrain.write(entry.getKey() + "\t" + entry2.getKey() + "\t" + entry2.getValue() + "\n");
					} else {
						bwTest.write(entry.getKey() + "\t" + entry2.getKey() + "\t" + entry2.getValue() + "\n");
					}
					index++;
					bwTrain.flush();
					bwTest.flush();
				}
			}
			bwTest.close();
			bwTrain.close();
		} else {
			bwTrain = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("C:\\Users\\HeyGo\\Desktop\\Foursquare\\Foursquare\\Foursquare_train.txt")));
			bwTest = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("C:\\Users\\HeyGo\\Desktop\\Foursquare\\Foursquare\\Foursquare_test.txt")));
			for (Entry<Integer, HashMap<Integer, Integer>> entry : check2.entrySet()) {
				int size = (int) (entry.getValue().size() * 0.7);
				int index = 0;
				// TreeMap<Integer, Integer> train = new TreeMap<>();
				// TreeMap<Integer, Integer> test = new TreeMap<>();
				for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
					if (index < size) {
						bwTrain.write(entry.getKey() + "\t" + entry2.getValue() + "\t" + entry2.getKey() / 3600 + "\n");
						// if (train.containsKey(entry2.getValue())) {
						// // train.put(entry2.getValue(), train.get(entry2.getValue()) + 1);
						//
						// } else {
						// train.put(entry2.getValue(), 1);
						// }
					} else {
						bwTest.write(entry.getKey() + "\t" + entry2.getValue() + "\t" + entry2.getKey() / 3600 + "\n");
						// if (test.containsKey(entry2.getValue())) {
						// test.put(entry2.getValue(), test.get(entry2.getValue()) + 1);
						// } else {
						// test.put(entry2.getValue(), 1);
						// }
					}
					index++;
				}
				// for (Entry<Integer, Integer> entry3 : train.entrySet()) {
				// bwTrain.write(entry.getKey() + "\t" + entry3.getKey() + "\t" +
				// entry3.getValue() + "\n");
				// }
				// for (Entry<Integer, Integer> entry3 : test.entrySet()) {
				// bwTest.write(entry.getKey() + "\t" + entry3.getKey() + "\t" +
				// entry3.getValue() + "\n");
				// }
				bwTrain.flush();
				bwTest.flush();
			}
			bwTest.close();
			bwTrain.close();
		}
	}

	public static void deal() throws Exception {
		Connection connection = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select user_id,location_id,time from checkinnew";
		BufferedWriter bwCheck = null;
		try {
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			bwCheck = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					"C:\\Users\\HeyGo\\Desktop\\Foursquare\\Foursquare\\Foursquare_checkins.txt")));
			while (rs.next()) {
				int user_id = rs.getInt(1);
				int location_id = rs.getInt(2);
				String time = rs.getString(3);
				if (check.containsKey(user_id)) {
					TreeMap<Integer, Integer> map = check.get(user_id);
					map.put(getMi(time), location_id);
				} else {
					TreeMap<Integer, Integer> map = new TreeMap<>();
					map.put(getMi(time), location_id);
					check.put(user_id, map);
				}
				if (check2.containsKey(user_id)) {
					HashMap<Integer, Integer> map = check2.get(user_id);
					map.put(getMi(time), location_id);
				} else {
					HashMap<Integer, Integer> map = new HashMap<>();
					map.put(getMi(time), location_id);
					check2.put(user_id, map);
				}
				if (count.containsKey(user_id)) {
					HashMap<Integer, Integer> map = count.get(user_id);
					if (map.containsKey(location_id)) {
						map.put(location_id, map.get(location_id) + 1);
					} else {
						map.put(location_id, 1);
					}
				} else {
					HashMap<Integer, Integer> map = new HashMap<>();
					map.put(location_id, 1);
					count.put(user_id, map);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rs.close();
			ps.close();
			connection.close();
		}
		for (Entry<Integer, TreeMap<Integer, Integer>> entry : check.entrySet()) {
			for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
				bwCheck.write(entry.getKey() + "\t" + entry2.getValue() + "\t" + entry2.getKey() + "\n");
			}
		}
		bwCheck.flush();
		bwCheck.close();
	}

	public static void dealAll() throws Exception {
		HashSet<Integer> user = new HashSet<>();
		HashSet<Integer> location = new HashSet<>();
		TreeMap<Integer, String[]> locacoo = new TreeMap<>();
		HashMap<String, Integer> contentIndex = new HashMap<>();
		TreeMap<Integer, HashSet<String>> usercont = new TreeMap<>();
		Connection connection = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select user_id,longitude,latitude,content,location_id from checkinnew";
		BufferedWriter bwSize = null;
		BufferedWriter bwCoo = null;
		BufferedWriter bwCate = null;

		try {
			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			bwSize = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					"C:\\Users\\HeyGo\\Desktop\\Foursquare\\Foursquare\\Foursquare_data_size.txt")));
			bwCate = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					"C:\\Users\\HeyGo\\Desktop\\Foursquare\\Foursquare\\Foursquare_poi_categories.txt")));
			bwCoo = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					"C:\\Users\\HeyGo\\Desktop\\Foursquare\\Foursquare\\Foursquare_poi_coos.txt")));
			int index = 0;
			while (rs.next()) {
				int user_id = rs.getInt(1);
				user.add(user_id);
				int location_id = rs.getInt(5);
				location.add(location_id);
				if (!locacoo.containsKey(location_id)) {
					String[] coo = new String[2];
					coo[0] = rs.getString(3);
					coo[1] = rs.getString(2);
					locacoo.put(location_id, coo);
				}
				String[] contents = rs.getString(4).split(",");
				for (String s : contents) {
					if (!contentIndex.containsKey(s)) {
						contentIndex.put(s, index++);
					}
					if (usercont.containsKey(location_id)) {
						HashSet<String> set = usercont.get(location_id);
						set.add(s);
					} else {
						HashSet<String> set = new HashSet<>();
						set.add(s);
						usercont.put(location_id, set);
					}
				}
			}
			bwSize.write(user.size() + "\t" + location.size() + "\t" + contentIndex.size());
			bwSize.flush();
			bwSize.close();
			for (Entry<Integer, String[]> entry : locacoo.entrySet()) {
				bwCoo.write(entry.getKey() + "\t" + entry.getValue()[0] + "\t" + entry.getValue()[1] + "\n");
			}
			bwCoo.flush();
			bwCoo.close();
			for (Entry<Integer, HashSet<String>> entry : usercont.entrySet()) {
				HashSet<String> set = entry.getValue();
				for (String s : set) {
					bwCate.write(entry.getKey() + "\t" + contentIndex.get(s) + "\n");
				}
			}
			bwCate.flush();
			bwCate.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			rs.close();
			ps.close();
			connection.close();
		}
	}

	public static int getMi(String time) {
		String[] t = time.split(" ")[1].split(":");
		int hour = Integer.valueOf(t[0]);
		int min = Integer.valueOf(t[1]);
		int sec = Integer.valueOf(t[2]);
		return hour * 60 * 60 + min * 60 + sec;
	}

	public static void main(String[] args) throws Exception {
		deal();
		dealAll();
		dealTT(0);
	}
}

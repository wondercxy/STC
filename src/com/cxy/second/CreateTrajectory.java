package com.cxy.second;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.regex.Pattern;

import com.cxy.data.GetConnection;

public class CreateTrajectory {
	HashMap<Integer, HashMap<String, ArrayList<Integer>>> user_day_loca = new HashMap<>();
	HashMap<Integer, String> loca_geohash = new HashMap<>();
	HashMap<Integer, String> loca_content = new HashMap<>();
	TreeMap<Integer, Integer> hour_count = new TreeMap<>();

	HashMap<Integer, ArrayList<Integer>> loca_hour = new HashMap<>();
	HashMap<Integer, ArrayList<Integer>> tra_loca = new HashMap<>();

	TreeMap<Integer, HashMap<String, ArrayList<Integer>>> result = new TreeMap<>();

	public void getData() throws Exception {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select user_id,time,location_id,geohash,content from checkintwi";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		while (rs.next()) {
			int uid = rs.getInt(1);
			String time = rs.getString(2);
			String day = time.split(" ")[0];
			int hour = Integer.valueOf(time.split(" ")[1].split(":")[0]);
			int lid = rs.getInt(3);
			String ghash = rs.getString(4);
			String content = rs.getString(5);
			if (user_day_loca.containsKey(uid)) {
				HashMap<String, ArrayList<Integer>> map = user_day_loca.get(uid);
				if (map.containsKey(day)) {
					map.get(day).add(lid);
				} else {
					ArrayList<Integer> list = new ArrayList<>();
					list.add(lid);
					map.put(day, list);
				}
			} else {
				HashMap<String, ArrayList<Integer>> map = new HashMap<>();
				ArrayList<Integer> list = new ArrayList<>();
				list.add(lid);
				map.put(day, list);
				user_day_loca.put(uid, map);
			}
			if (!loca_geohash.containsKey(lid)) {
				loca_geohash.put(lid, ghash);
			}
			if (!loca_content.containsKey(lid)) {
				loca_content.put(lid, content);
			}
			if (loca_hour.containsKey(lid)) {
				loca_hour.get(lid).add(hour);
			} else {
				ArrayList<Integer> list = new ArrayList<>();
				list.add(hour);
				loca_hour.put(lid, list);
			}
			if (hour_count.containsKey(hour)) {
				int c = hour_count.get(hour);
				hour_count.put(hour, c + 1);
			} else {
				hour_count.put(hour, 0);
			}
		}
		System.out.println("Done");
		BufferedWriter bw = new BufferedWriter(new FileWriter("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\hourCount.txt"));
		for (Entry<Integer, Integer> entry : hour_count.entrySet()) {
			bw.write(entry.getKey() + " " + entry.getValue() + "\n");
		}
		bw.write("\n");
		bw.flush();
		bw.close();
		ps.close();
		rs.close();
		con.close();
	}

	public void getTrajectory() throws Exception {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select tra_id,location_id from trajectory where time is null";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		while (rs.next()) {
			int traid = rs.getInt(1);
			int lid = rs.getInt(2);
			if (tra_loca.containsKey(traid))
				tra_loca.get(traid).add(lid);
			else {
				ArrayList<Integer> list = new ArrayList<>();
				list.add(lid);
				tra_loca.put(traid, list);
			}
		}
		System.out.println(tra_loca.size());
		rs.close();
		ps.close();
		con.close();
	}

	public void createTime() throws SQLException {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		String sql = "update trajectory set time=? where tra_id=? and location_id=?";
		ps = con.prepareStatement(sql);
		for (Entry<Integer, ArrayList<Integer>> entry : tra_loca.entrySet()) {
			int tid = entry.getKey();
			System.out.println(tid);
			for (int l : entry.getValue()) {
				ArrayList<Integer> hour = loca_hour.get(l);
				int h = hour.get(new Random().nextInt(hour.size()));
				// System.out.println(tid + " " + l + " " + h);
				ps.setInt(1, h);
				ps.setInt(2, tid);
				ps.setInt(3, l);
				ps.executeUpdate();
			}
		}
		ps.close();
		con.close();
	}

	public void create() {
		for (Entry<Integer, HashMap<String, ArrayList<Integer>>> entry : user_day_loca.entrySet()) {
			int uid = entry.getKey();
			for (Entry<String, ArrayList<Integer>> entry2 : entry.getValue().entrySet()) {
				String day = entry2.getKey();
				ArrayList<Integer> list = entry2.getValue();

				if (list.size() >= 2) {
					// System.out.println(list);
					list = createTra(list);
					// System.out.println(list);
					if (result.containsKey(uid)) {
						result.get(uid).put(day, list);
					} else {
						HashMap<String, ArrayList<Integer>> map = new HashMap<>();
						map.put(day, list);
						result.put(uid, map);
					}
				}
			}
		}
		// System.out.println("Done");
		// for (Entry<Integer, HashMap<String, ArrayList<Integer>>> entry :
		// result.entrySet())
		// System.out.println(entry.getKey() + "" + entry.getValue());
	}

	public void insert() throws Exception {
		int id = 1;
		int tra_id = 1;
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		String sql = "insert  into trajectory(id,user_id,tra_id,location_id) values(?,?,?,?)";
		ps = con.prepareStatement(sql);
		for (Entry<Integer, HashMap<String, ArrayList<Integer>>> entry : result.entrySet()) {
			int uid = entry.getKey();
			for (Entry<String, ArrayList<Integer>> entry2 : entry.getValue().entrySet()) {
				for (int l : entry2.getValue()) {
					ps.setInt(1, id++);
					ps.setInt(2, uid);
					ps.setInt(3, tra_id);
					ps.setInt(4, l);
					ps.execute();
				}
				tra_id++;
			}
		}
	}

	public ArrayList<Integer> createTra(ArrayList<Integer> list) {
		Random random = new Random();
		int len = random.nextInt(10) + 1;
		while (len < 3)
			len = random.nextInt(10) + 1;
		if (list.size() < len) {
			int loop = len - list.size();
			while (loop-- > 0) {
				int lid = list.get(list.size() - 1);
				int candidateLid = getRandomNearLoca(lid);
				list.add(candidateLid);
			}
		}
		return list;
	}

	public int getRandomNearLoca(int lid) {
		String geohash = loca_geohash.get(lid);
		String pattern = geohash.substring(0, 8 - 5) + "(\\w*)";
		ArrayList<Integer> candidates = new ArrayList<>();
		for (Entry<Integer, String> entry : loca_geohash.entrySet()) {
			if (Pattern.matches(pattern, entry.getValue()))
				candidates.add(entry.getKey());
		}
		return candidates.get(new Random().nextInt(candidates.size()));
	}

	public static void main(String[] args) throws Exception {
		// for (int i = 0; i < 100; i++)
		// createTra();
		CreateTrajectory c = new CreateTrajectory();
		c.getData();
		c.getTrajectory();
		c.createTime();
		// c.create();Ëæ»ú´´Ôì¹ì¼£
		// c.insert();
	}
}

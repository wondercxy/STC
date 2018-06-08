package com.cxy.second;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.cxy.data.GetConnection;

public class Jaccard {
	HashMap<Integer, HashSet<Integer>> user_location = new HashMap<>();
	HashMap<Integer, TreeMap<Double, Integer>> result = new HashMap<>();

	public void jaccard() throws Exception {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select user_id,location_id from checkinnew";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		while (rs.next()) {
			int user_id = rs.getInt(1);
			int location_id = rs.getInt(2);
			if (user_location.containsKey(user_id)) {
				HashSet<Integer> set = user_location.get(user_id);
				set.add(location_id);
				user_location.put(user_id, set);
			} else {
				HashSet<Integer> set = new HashSet<>();
				set.add(location_id);
				user_location.put(user_id, set);
			}
		}
	}

	public HashMap<Integer, TreeMap<Double, Integer>> getSim() throws Exception {
		jaccard();
		for (Entry<Integer, HashSet<Integer>> entry : user_location.entrySet()) {
			int u1 = entry.getKey();
			for (Entry<Integer, HashSet<Integer>> entry2 : user_location.entrySet()) {
				int u2 = entry2.getKey();
				if (u1 != u2) {
					HashSet<Integer> l1 = entry.getValue();
					HashSet<Integer> l2 = entry2.getValue();
					HashSet<Integer> l = new HashSet<>();
					l.addAll(l1);
					l.addAll(l2);
					int size = l.size();
					l.clear();
					l.addAll(l1);
					l.retainAll(l2);
					int sum = l.size();
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
		// for (Entry<Integer, TreeMap<Double, Integer>> entry : result.entrySet()) {
		// int c = 0;
		// String text = entry.getKey() + " ";
		// for (Entry<Double, Integer> e : entry.getValue().descendingMap().entrySet())
		// {
		// if (c++ < 20) {
		// text += e.getValue() + "-" + e.getKey() + " ";
		// }
		// }
		// System.out.println(text);
		// }
		return result;
	}

	public void write(int number) throws Exception {
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\jaccard.txt")));

		for (Entry<Integer, TreeMap<Double, Integer>> entry : result.entrySet()) {
			int count = 0;
			String text = entry.getKey() + " ";
			for (Entry<Double, Integer> e : entry.getValue().descendingMap().entrySet()) {
				text += e.getValue();
				if (count++ > number)
					break;
			}
			bw.write(text);
		}
		bw.flush();
		bw.close();
	}

	public static void main(String[] args) throws Exception {
		Jaccard jaccard = new Jaccard();
		// jaccard.jaccard();
		jaccard.getSim();
		jaccard.write(20);
	}
}

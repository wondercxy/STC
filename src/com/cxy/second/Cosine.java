package com.cxy.second;
/*
 * 利用余弦相似度计算用户的相似度，也是通过用户访问的地点计算
 */

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.cxy.data.GetConnection;

public class Cosine {
	Map<Integer, int[]> vectorMap = new HashMap<Integer, int[]>();
	int[][] vector;

	HashMap<Integer, ArrayList<Integer>> user_loca = new HashMap<>();
	TreeMap<Integer, HashMap<Integer, Double>> result = new TreeMap<>();
	HashMap<Integer, Integer> indexLoca = new HashMap<>();
	HashMap<Integer, Integer> locaIndex = new HashMap<>();
	HashMap<Integer, Integer> indexUser = new HashMap<>();
	HashMap<Integer, Integer> userIndex = new HashMap<>();

	public void statistic(ArrayList<Integer> user1, ArrayList<Integer> user2) {
		int[] tempArray = null;
		for (int i : user1) {
			if (vectorMap.containsKey(i)) {
				vectorMap.get(i)[0]++;
			} else {
				tempArray = new int[2];
				tempArray[0] = 1;
				tempArray[1] = 0;
				vectorMap.put(i, tempArray);
			}
		}
		for (int i : user2) {
			if (vectorMap.containsKey(i)) {
				vectorMap.get(i)[1]++;
			} else {
				tempArray = new int[2];
				tempArray[0] = 0;
				tempArray[1] = 1;
				vectorMap.put(i, tempArray);
			}
		}
	}

	public void getUserLoca() throws Exception {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select user_id,location_id from checkintwitrain";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		int index = 0;
		int indexU = 0;
		while (rs.next()) {
			int uid = rs.getInt(1);
			int lid = rs.getInt(2);
			if (user_loca.containsKey(uid)) {
				user_loca.get(uid).add(lid);
			} else {
				ArrayList<Integer> list = new ArrayList<>();
				list.add(lid);
				user_loca.put(uid, list);
			}
			if (!indexLoca.containsKey(lid)) {
				indexLoca.put(lid, index);
				locaIndex.put(index++, lid);
			}
			if (!indexUser.containsKey(uid)) {
				indexUser.put(uid, indexU);
				userIndex.put(indexU++, uid);
			}
		}
		System.out.println(indexLoca.size());
		vector = new int[user_loca.size()][indexLoca.size()];
		for (Entry<Integer, ArrayList<Integer>> entry : user_loca.entrySet()) {
			int u = entry.getKey();
			int uposi = indexUser.get(u);
			for (int i : entry.getValue()) {
				int lposi = indexLoca.get(i);
				vector[uposi][lposi]++;
				// if (u == 0)
				// System.out.println(i + " " + lposi);
			}
		}
	}

	// 求余弦相似度
	public double sim(int i, int j) {
		double result = 0;
		result = pointMulti(i, j) / sqrtMulti(i, j);
		vectorMap.clear();
		return result;
	}

	private double sqrtMulti(int i, int j) {
		double result = 0;
		result = squares(i, j);
		result = Math.sqrt(result);
		return result;
	}

	// 求平方和
	private double squares(int i, int j) {
		double result1 = 0;
		double result2 = 0;
		for (int p = 0; p < indexLoca.size(); p++) {
			result1 += vector[i][p] * vector[i][p];
			result2 += vector[j][p] * vector[j][p];
		}

		return result1 * result2;
	}

	// 点乘法
	private double pointMulti(int i, int j) {
		double result = 0;
		for (int p = 0; p < indexLoca.size(); p++) {
			result += vector[i][p] * vector[j][p];
		}
		return result;
	}

	public void count(int num) throws Exception {
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\cosinetwi.txt")));
		getUserLoca();
		for (Entry<Integer, ArrayList<Integer>> en1 : user_loca.entrySet()) {
			System.out.println(en1.getKey());
			for (Entry<Integer, ArrayList<Integer>> en2 : user_loca.entrySet()) {
				if (en1.getKey() != en2.getKey()) {
					int i = indexUser.get(en1.getKey());
					int j = indexUser.get(en2.getKey());
					// System.out.println(i + " " + j);
					double sim = sim(i, j);
					if (sim != 0.0)
						if (result.containsKey(en1.getKey())) {
							result.get(en1.getKey()).put(en2.getKey(), sim);
						} else {
							HashMap<Integer, Double> map = new HashMap<>();
							map.put(en2.getKey(), sim);
							result.put(en1.getKey(), map);
						}
				}
			}
		}
		for (Entry<Integer, HashMap<Integer, Double>> entry : result.entrySet()) {
			int count = 0;
			String text = entry.getKey() + " ";
			for (Entry<Integer, Double> e : entry.getValue().entrySet()) {
				text += e.getKey() + " ";
				if (count++ > num)
					break;
			}
			bw.write(text + "\n");
		}
		bw.flush();
		bw.close();
		for (Entry<Integer, HashMap<Integer, Double>> entry : result.entrySet()) {

			HashMap<Integer, Double> map = entry.getValue();
			map = (HashMap<Integer, Double>) TA.sortByValue(map);
			System.out.println(entry.getKey() + " " + map);
		}
	}

	public static void main(String[] args) throws Exception {
		Cosine cosine = new Cosine();
		cosine.count(20);
	}
}

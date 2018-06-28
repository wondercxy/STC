package com.cxy.second;
/*
 * 利用余弦相似度以及加上时间影响计算用户的相似度，也是通过用户访问的地点计算
 */

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.cxy.data.GetConnection;

public class CosineTime {
	// Map<Integer, int[]> vectorMap = new HashMap<Integer, int[]>();
	// short[][][] vector2;

	// HashMap<Integer, HashMap<Integer, ArrayList<Integer>>> user_loca = new
	// HashMap<>();
	HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> user_time_loca = new HashMap<>();
	TreeMap<Integer, HashMap<Integer, Double>> result = new TreeMap<>();
	// HashMap<Integer, Integer> indexLoca = new HashMap<>();
	// HashMap<Integer, Integer> locaIndex = new HashMap<>();
	// HashMap<Integer, Integer> indexUser = new HashMap<>();
	// HashMap<Integer, Integer> userIndex = new HashMap<>();

	public void getUserLoca() throws Exception {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select user_id,location_id,time from checkintwitrain";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		// int index = 0;
		// int indexU = 0;
		while (rs.next()) {
			int uid = rs.getInt(1);
			int lid = rs.getInt(2);
			int hour = Integer.valueOf(rs.getString(3).split(" ")[1].split(":")[0]);
			if (user_time_loca.containsKey(uid)) {
				if (user_time_loca.get(uid).containsKey(hour)) {
					if (user_time_loca.get(uid).get(hour).containsKey(lid)) {
						int c = user_time_loca.get(uid).get(hour).get(lid) + 1;
						user_time_loca.get(uid).get(hour).put(lid, c);
					} else {
						HashMap<Integer, Integer> map = new HashMap<>();
						map.put(lid, 1);
						user_time_loca.get(uid).put(hour, map);
					}
				} else {
					HashMap<Integer, HashMap<Integer, Integer>> map = user_time_loca.get(uid);
					HashMap<Integer, Integer> m = new HashMap<>();
					m.put(lid, 1);
					map.put(hour, m);
					user_time_loca.put(uid, map);
				}
			} else {
				HashMap<Integer, HashMap<Integer, Integer>> map = new HashMap<>();
				HashMap<Integer, Integer> m = new HashMap<>();
				m.put(lid, 1);
				map.put(hour, m);
				user_time_loca.put(uid, map);
			}
			// if (user_loca.containsKey(uid)) {
			// if (user_loca.get(uid).containsKey(hour)) {
			// user_loca.get(uid).get(hour).add(lid);
			//
			// } else {
			// HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();
			// ArrayList<Integer> list = new ArrayList<>();
			// list.add(lid);
			// map.put(hour, list);
			// user_loca.put(uid, map);
			// }
			// } else {
			// HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();
			// ArrayList<Integer> list = new ArrayList<>();
			// list.add(lid);
			// map.put(hour, list);
			// user_loca.put(uid, map);
			// }
			// if (!indexLoca.containsKey(lid)) {
			// indexLoca.put(lid, index);
			// locaIndex.put(index++, lid);
			// }
			// if (!indexUser.containsKey(uid)) {
			// indexUser.put(uid, indexU);
			// userIndex.put(indexU++, uid);
			// }
		}
		// System.out.println(indexLoca.size());
		// vector2 = new short[user_loca.size()][24][indexLoca.size()];
		// for (Entry<Integer, HashMap<Integer, ArrayList<Integer>>> entry :
		// user_loca.entrySet()) {
		// int u = entry.getKey();
		// int uposi = indexUser.get(u);
		// for (Entry<Integer, ArrayList<Integer>> en : entry.getValue().entrySet()) {
		// int h = en.getKey();
		// for (int l : en.getValue()) {
		// int lposi = indexLoca.get(l);
		// vector2[uposi][h][lposi]++;
		// }
		// // if (u == 0)
		// // System.out.println(i + " " + lposi);
		// }
		// }
	}

	// 求余弦相似度
	public double sim(HashMap<Integer, HashMap<Integer, Integer>> i, HashMap<Integer, HashMap<Integer, Integer>> j) {
		double result = 0;
		result = pointMulti(i, j) / sqrtMulti(i, j);
		// vectorMap.clear();
		return result;
	}

	private double sqrtMulti(HashMap<Integer, HashMap<Integer, Integer>> i,
			HashMap<Integer, HashMap<Integer, Integer>> j) {
		double result = 0;
		result = squares(i, j);
		result = Math.sqrt(result);
		return result;
	}

	// 求平方和
	private double squares(HashMap<Integer, HashMap<Integer, Integer>> i,
			HashMap<Integer, HashMap<Integer, Integer>> j) {
		double result1 = 0;
		double result2 = 0;
		for (Entry<Integer, HashMap<Integer, Integer>> entry : i.entrySet()) {
			for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
				result1 += entry2.getValue() * entry2.getValue();
			}
		}
		for (Entry<Integer, HashMap<Integer, Integer>> entry : j.entrySet()) {
			for (Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
				result2 += entry2.getValue() * entry2.getValue();
			}
		}
		// for (int h = 0; h < 24; h++) {
		// for (int p = 0; p < indexLoca.size(); p++) {
		// result1 += vector2[i][h][p] * vector2[i][h][p];
		// result2 += vector2[j][h][p] * vector2[j][h][p];
		// }
		// }

		return result1 * result2;
	}

	// 点乘法
	private double pointMulti(HashMap<Integer, HashMap<Integer, Integer>> i,
			HashMap<Integer, HashMap<Integer, Integer>> j) {
		double result = 0;
		for (Entry<Integer, HashMap<Integer, Integer>> entry : i.entrySet()) {
			int hour = entry.getKey();
			if (j.containsKey(hour)) {
				for (Entry<Integer, Integer> en2 : entry.getValue().entrySet()) {
					if (j.get(hour).containsKey(en2.getKey())) {
						result += i.get(hour).get(en2.getKey()) * j.get(hour).get(en2.getKey());
					}
				}
			}
		}
		// for (int h = 0; h < 24; h++) {
		// for (int p = 0; p < indexLoca.size(); p++) {
		// result += vector2[i][h][p] * vector2[j][h][p];
		// }
		// }
		return result;
	}

	public void count(int num) throws Exception {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\cosinetwiTime.txt")));
		getUserLoca();
		for (Entry<Integer, HashMap<Integer, HashMap<Integer, Integer>>> en1 : user_time_loca.entrySet()) {
			System.out.println(en1.getKey());
			for (Entry<Integer, HashMap<Integer, HashMap<Integer, Integer>>> en2 : user_time_loca.entrySet()) {
				if (en1.getKey() != en2.getKey()) {
					HashMap<Integer, HashMap<Integer, Integer>> i = en1.getValue();
					HashMap<Integer, HashMap<Integer, Integer>> j = en2.getValue();
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
		CosineTime cosine = new CosineTime();
		cosine.count(100);
		// cosine.getUserLoca();
	}
}

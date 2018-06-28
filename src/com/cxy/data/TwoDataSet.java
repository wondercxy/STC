package com.cxy.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

public class TwoDataSet {
	public static HashMap<Integer, HashSet<Integer>> count = new HashMap<>();

	public static void solve() throws Exception {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select id,user_id from checkintwi";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		while (rs.next()) {
			int id = rs.getInt(1);
			int uid = rs.getInt(2);
			if (count.containsKey(uid))
				count.get(uid).add(id);
			else {
				HashSet<Integer> set = new HashSet<>();
				set.add(id);
				count.put(uid, set);
			}
		}
		con.close();
	}

	public static void delete() throws Exception {
		Connection con1 = GetConnection.getCon();
		Connection con2 = GetConnection.getCon();
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		String sql1 = "delete from checkintwipredict where id=?";
		String sql2 = "delete from checkintwitrain where id=?";
		ps1 = con1.prepareStatement(sql1);
		ps2 = con2.prepareStatement(sql2);
		for (Entry<Integer, HashSet<Integer>> entry : count.entrySet()) {
			System.out.println(entry.getKey());
			int size = (int) (entry.getValue().size() * 0.7);
			int index = 0;
			for (int i : entry.getValue()) {
				if (index < size) {
					ps1.setInt(1, i);
					ps1.execute();
				} else {
					ps2.setInt(1, i);
					ps2.execute();
				}
				index++;
			}
		}
	}

	public static void main(String[] args) throws Exception {
		solve();
		delete();
	}
}

package com.cxy.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;

public class DeleteTwiLessTen {
	public static void delete() throws Exception {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT location_id FROM `checkintwi`  GROUP BY location_id HAVING COUNT(location_id)<10";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		HashSet<Integer> set = new HashSet<>();
		while (rs.next()) {
			int loca = rs.getInt(1);
			set.add(loca);
		}
		String sql2 = "delete from checkintwi where location_id=?";
		ps = con.prepareStatement(sql2);
		for (int l : set) {
			ps.setInt(1, l);
			ps.execute();
			System.out.println(l);
		}
	}

	public static void deleteUser() throws Exception {
		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "SELECT user_id FROM `checkintwi`  GROUP BY user_id HAVING COUNT(user_id)<10";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		HashSet<Integer> set = new HashSet<>();
		while (rs.next()) {
			int loca = rs.getInt(1);
			set.add(loca);
		}
		String sql2 = "delete from checkintwi where user_id=?";
		ps = con.prepareStatement(sql2);
		for (int l : set) {
			ps.setInt(1, l);
			ps.execute();
			System.out.println(l);
		}
	}

	public static void main(String[] args) throws Exception {
		// delete();
		deleteUser();
	}
}

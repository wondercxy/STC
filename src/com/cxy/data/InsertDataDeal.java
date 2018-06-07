package com.cxy.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertDataDeal {
	static Connection con = GetConnection.getCon();

	public static void insertAll() throws Exception {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\checkin.txt")));
		String line = null;
		int id = 0;
		while ((line = reader.readLine()) != null) {
			String[] strings = line.split("--");
			int userid = Integer.valueOf(strings[0]);
			double latitude = Double.valueOf(strings[1]);
			double longtitude = Double.valueOf(strings[2]);
			int locaid = Integer.valueOf(strings[3]);
			String time = strings[4];
			String content = strings[5];
			checkin(userid, latitude, longtitude, locaid, time, content);
			if (id++ % 1000 == 0)
				System.out.println(id);
		}
		reader.close();
	}

	public static void checkin(int userid, double latitude, double longtitude, int locaid, String time,
			String content) {

		PreparedStatement ps = null;
		String sql = "insert into checkinnew(user_id,latitude,longitude,location_id,time,content) values"
				+ "(?,?,?,?,?,?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, userid);
			ps.setDouble(2, latitude);
			ps.setDouble(3, longtitude);
			ps.setInt(4, locaid);
			ps.setString(5, time);
			ps.setString(6, content);
			ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		insertAll();
	}
}

package com.cxy.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class InsertTwitter {
	public void insertAll() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				"F:\\WorkSpace\\JIM\\JIM\\data\\Twitter-Foursquare-CombinedDataset\\data\\checkins.txt")));
		String line = reader.readLine();
		while (line != null) {
			String[] strings = line.split("	");
			int userid = Integer.valueOf(strings[0]);
			int twid = Integer.valueOf(strings[1]);
			float latitude = Float.valueOf(strings[2]);
			float longtitude = Float.valueOf(strings[3]);
			String time = strings[4];
			int placeid = Integer.valueOf(strings[5]);
			String content = strings[6];
			checkin(userid, twid, latitude, longtitude, time, placeid, content);
			line = reader.readLine();
		}
		reader.close();
	}

	Connection con = GetConnection.getCon();

	public void checkin(int userid, int twid, float latitude, float longtitude, String time, int placeid,
			String content) {
		PreparedStatement ps = null;
		String sql = "insert into checkintwi(user_id,tw_id,latitude,longitude,time,location_id,content) values"
				+ "(?,?,?,?,?,?,?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, userid);
			ps.setInt(2, twid);
			ps.setFloat(3, latitude);
			ps.setFloat(4, longtitude);
			ps.setString(5, time);
			ps.setInt(6, placeid);
			ps.setString(7, content);
			ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		InsertTwitter insertCheckin = new InsertTwitter();
		insertCheckin.insertAll();
	}
}

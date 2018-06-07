package com.cxy.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class InsertFoursquare {
	static Connection con = GetConnection.getCon();

	public static void insertAll() throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(
				"F:\\Workspace\\JIM\\JIM\\data\\Foursquare-JiHui-Content-Network\\CA Dataset\\checkin_CA_venues.txt")));
		String line = null;
		int id = 0;
		while ((line = reader.readLine()) != null) {
			String[] strings = line.split("	");
			int userid = Integer.valueOf(strings[0]);
			String time = timeGMT(strings[1]);
			String venueid = strings[2];
			String venuename = strings[3].substring(1, strings[3].length() - 1);
			String[] loca = location(strings[4].substring(1, strings[4].length() - 1));
			double latitude = 0d;
			double longtitude = 0d;
			String city = "";
			String state = "";
			String country = "";
			String content = "";
			if (loca[0].length() != 0)
				latitude = Double.valueOf(loca[0]);
			else
				continue;
			if (loca[1].length() != 0)
				longtitude = Double.valueOf(loca[1]);
			else
				continue;
			if (loca[2].length() != 0)
				city = loca[2];
			else
				continue;
			if (loca[3].length() != 0)
				state = loca[3];
			else
				continue;
			if (loca.length == 5 && loca[4].length() != 0)
				country = loca[4];
			else
				continue;
			if (strings[5] != "" && strings[5].length() != 2)
				content = strings[5].substring(1, strings[5].length() - 2);
			else
				continue;
			// System.out.println(userid);
			// System.out.println(time);
			// System.out.println(venueid);
			// System.out.println(venuename);
			// System.out.println(latitude);
			// System.out.println(longtitude);
			// System.out.println(city + " " + state + " " + country);
			// System.out.println(content);
			// break;
			checkin(userid, time, venueid, venuename, longtitude, latitude, city, state, country, content);
			if (id++ % 1000 == 0)
				System.out.println(id);
		}
		reader.close();
	}

	public static void checkin(int userid, String time, String venueid, String venuename, double longtitude,
			double latitude, String city, String state, String country, String content) {
		PreparedStatement ps = null;
		String sql = "insert into checkinfour(user_id,time,venue_id,venue_name,longitude,latitude,city,state,country,content) values"
				+ "(?,?,?,?,?,?,?,?,?,?)";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, userid);
			ps.setString(2, time);
			ps.setString(3, venueid);
			ps.setString(4, venuename);
			ps.setDouble(5, longtitude);
			ps.setDouble(6, latitude);
			ps.setString(7, city);
			ps.setString(8, state);
			ps.setString(9, country);
			ps.setString(10, content);
			ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String[] location(String lo) {
		String[] l = lo.split(",", 5);
		return l;
	}

	public static String timeGMT(String t) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM ddHH:mm:ss '+0000' yyyy", Locale.US);
		Date date = null;
		try {
			date = sdf.parse(t);
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String tt = sdf.format(date);
			return tt;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws Exception {
		// System.out.println(timeGMT("Sat Jul 30 20:15:24 +0000 2011"));
		insertAll();
	}
}

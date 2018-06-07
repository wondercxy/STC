package com.cxy.data;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateRegionid {
	static Connection con = GetConnection.getCon();

	public static void update() throws IOException {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream("F:\\AnacondaWork\\cluster\\kmeans.txt")));
		String line = "";
		int id = 1;
		while ((line = reader.readLine()) != null) {
			int region_id = Integer.valueOf(line);
			String sql = "update checkinnew set region=? where id=? ";
			PreparedStatement ps = null;
			try {
				ps = con.prepareStatement(sql);
				ps.setInt(1, region_id);
				ps.setInt(2, id++);
				ps.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		reader.close();
	}

	public static void main(String[] args) {
		try {
			update();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

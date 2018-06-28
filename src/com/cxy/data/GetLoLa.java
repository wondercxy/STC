package com.cxy.data;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GetLoLa {
	public static void getLL() {
		Connection con = null;
		String sql = "select longitude,latitude from checkintwi";
		PreparedStatement ps = null;
		ResultSet rs = null;
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream("F:\\AnacondaWork\\cluster\\allpointsTwi.txt")));
			con = GetConnection.getCon();
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				bw.write(rs.getString(1) + " " + rs.getString(2) + "\n");
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		getLL();
	}
}

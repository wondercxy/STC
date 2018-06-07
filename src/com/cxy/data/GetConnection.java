package com.cxy.data;
import java.sql.Connection;
import java.sql.DriverManager;

public class GetConnection {
	public static Connection getCon() {
		Connection connection = null;
		String driver = "com.mysql.jdbc.Driver";
		String username = "root";
		String password = "cxy930822";
		String url = "jdbc:mysql://localhost:3306/foursqaure";
		try {
			Class.forName(driver);
			connection = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return connection;
	}
}

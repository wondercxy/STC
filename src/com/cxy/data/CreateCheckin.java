package com.cxy.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreateCheckin {
	public static void getCheckin(int user_id, int spl) throws IOException {
		Connection con = GetConnection.getCon();
		String sql = "select user_id,latitude,longitude,location_id,time,content,region_id from checkinfour where user_id=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		String train = "F:\\WorkSpace\\JIM\\JIM\\input\\TRM\\checkinfile4.txt";
		String predict = "F:\\WorkSpace\\JIM\\JIM\\input\\TRM\\predictfile4.txt";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, user_id);
			rs = ps.executeQuery();
			int index = 0;
			System.out.println(user_id);
			while (rs.next()) {
				String tmp = rs.getInt(1) + "--" + rs.getDouble(2) + "--" + rs.getDouble(3) + "--" + rs.getInt(4) + "--"
						+ rs.getString(5) + "--" + rs.getString(6) + "--" + rs.getInt(7);
				if (index < spl)
					writeFileContent(train, tmp);
				else {
					writeFileContent(predict, tmp);
				}
				index++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (con != null)
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public static boolean writeFileContent(String filepath, String newstr) throws IOException {
		Boolean bool = false;
		String filein = newstr + "\r\n";// 新写入的行，换行
		String temp = "";

		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			File file = new File(filepath);// 文件路径(包括文件名称)
			// 将文件读入输入流
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			StringBuffer buffer = new StringBuffer();

			// 文件原有内容
			for (@SuppressWarnings("unused")
			int i = 0; (temp = br.readLine()) != null; i++) {
				buffer.append(temp);
				// 行与行之间的分隔符 相当于“\n”
				buffer = buffer.append(System.getProperty("line.separator"));
			}
			buffer.append(filein);

			fos = new FileOutputStream(file);
			pw = new PrintWriter(fos);
			pw.write(buffer.toString().toCharArray());
			pw.flush();
			bool = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			// 不要忘记关闭
			if (pw != null) {
				pw.close();
			}
			if (fos != null) {
				fos.close();
			}
			if (br != null) {
				br.close();
			}
			if (isr != null) {
				isr.close();
			}
			if (fis != null) {
				fis.close();
			}
		}
		return bool;
	}

	public static List<Integer> getUser() {
		Connection con = GetConnection.getCon();
		List<Integer> list = new ArrayList<>();
		String sql = "select user_id from checkinfour group by user_id ";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while (rs.next()) {
				list.add(rs.getInt(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (con != null)
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return list;
	}

	public static int getSplit(int userid) {
		Connection con = GetConnection.getCon();
		String sql = "select count(user_id) from checkinfour where user_id=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, userid);
			rs = ps.executeQuery();
			while (rs.next()) {
				count = rs.getInt(1);
				// System.out.println(count);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (con != null)
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return (int) (count * 0.7);
	}

	public static void main(String[] args) throws IOException {
		List<Integer> list = getUser();
		for (int i : list) {
			// System.out.println(getSplit(i));
			getCheckin(i, getSplit(i));
		}
	}
}

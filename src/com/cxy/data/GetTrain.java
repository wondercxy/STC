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

public class GetTrain {
	static Connection con = GetConnection.getCon();

	public static void getTrain(int user_id, int spl) throws IOException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select user_id,location_id,content,region_id from checkinfour where user_id=?";
		String pathTrain = "F:\\WorkSpace\\JIM\\JIM\\input\\train5JIM.txt";
		String pathTest = "F:\\WorkSpace\\JIM\\JIM\\input\\JIMTEST5.txt";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, user_id);
			rs = ps.executeQuery();
			int index = 0;
			while (rs.next()) {
				String tmp = rs.getInt(1) + "-" + rs.getInt(2) + "-" + rs.getString(3) + "-" + rs.getInt(4);
				if (index < spl) {
					System.out.println(user_id);
					writeFileContent(pathTrain, tmp);
				} else {
					writeFileContent(pathTest, tmp);
				}
				index++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static boolean writeFileContent(String filepath, String newstr) throws IOException {
		Boolean bool = false;
		String filein = newstr + "\r\n";// ��д����У�����
		String temp = "";

		FileInputStream fis = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			File file = new File(filepath);// �ļ�·��(�����ļ�����)
			// ���ļ�����������
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			StringBuffer buffer = new StringBuffer();

			// �ļ�ԭ������
			for (@SuppressWarnings("unused")
			int i = 0; (temp = br.readLine()) != null; i++) {
				buffer.append(temp);
				// ������֮��ķָ��� �൱�ڡ�\n��
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
			// ��Ҫ���ǹر�
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
		List<Integer> list = new ArrayList<>();
		String sql = "select user_id from checkinfour group by user_id";
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
		}
		return list;
	}

	public static int getSplit(int userid) {
		String sql = "select count(user_id) from checkinfour where user_id=?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		int count = 0;
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, userid);
			rs = ps.executeQuery();
			while (rs.next())
				count = rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (int) (count * 0.7);
	}

	public static void main(String[] args) throws IOException {
		List<Integer> list = getUser();
		for (int i : list) {
			getTrain(i, getSplit(i));
		}
	}
}

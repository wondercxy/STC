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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DealData {
	public static List<Integer> removeLocation() throws Exception {
		Connection con = GetConnection.getCon();
		List<Integer> location = new ArrayList<>();

		String sql = "SELECT location_id,count(location_id) FROM `checkinfour`  GROUP BY location_id HAVING COUNT(location_id)>=10";
		PreparedStatement ps = null;
		ResultSet rs = null;

		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		while (rs.next()) {
			int locaid = rs.getInt(1);
			location.add(locaid);
		}

		rs.close();
		ps.close();
		con.close();

		System.out.println("Location end");
		return location;
	}

	public static List<Integer> removeUser() throws Exception {
		Connection con = GetConnection.getCon();
		List<Integer> user = new ArrayList<>();

		String sql = "SELECT user_id,count(user_id) FROM `checkinfour`  where user_id>2000 GROUP BY user_id HAVING COUNT(user_id)>=10 ";
		PreparedStatement ps = null;
		ResultSet rs = null;

		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();

		while (rs.next()) {
			int uid = rs.getInt(1);
			user.add(uid);
		}

		rs.close();
		ps.close();
		con.close();

		System.out.println("User end");

		return user;
	}

	public static Map<Integer, List<String>> getCheck() throws Exception {
		Map<Integer, List<String>> map = new HashMap<>();

		List<Integer> user = removeUser();
		List<Integer> loca = removeLocation();

		Connection con = GetConnection.getCon();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String sql = "select user_id,latitude,longitude,location_id,time,content,region_id from checkinfour where user_id=?";
		for (int i = 0; i < user.size(); i++) {
			int uid = user.get(i);
			ps = con.prepareStatement(sql);
			ps.setInt(1, uid);
			rs = ps.executeQuery();
			while (rs.next()) {
				int lid = rs.getInt(4);
				if (!loca.contains(lid))
					continue;
				else {
					String tmp = rs.getInt(1) + "--" + rs.getDouble(2) + "--" + rs.getDouble(3) + "--" + rs.getInt(4)
							+ "--" + rs.getString(5) + "--" + rs.getString(6) + "--" + rs.getInt(7);
					if (map.containsKey(uid)) {
						List<String> list = map.get(uid);
						list.add(tmp);
						map.put(uid, list);
					} else {
						List<String> list = new ArrayList<>();
						list.add(tmp);
						map.put(uid, list);
					}
				}
			}
		}

		System.out.println("Check end");
		return map;
	}

	public static void write() throws Exception {
		Map<Integer, List<String>> map = getCheck();

		String check = "F:\\WorkSpace\\JIM\\JIM\\input\\deal\\checkin2.txt";
		String predict = "F:\\WorkSpace\\JIM\\JIM\\input\\deal\\predict2.txt";

		for (Entry<Integer, List<String>> entry : map.entrySet()) {
			// System.out.println(entry.getKey());
			if (entry.getValue().size() >= 10) {
				List<String> list = entry.getValue();
				for (int i = 0; i < list.size(); i++) {
					writeFileContent(check, list.get(i));
					if (i % 2 == 0)
						writeFileContent(predict, list.get(i));
				}
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

	public static void main(String[] args) {
		try {
			write();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

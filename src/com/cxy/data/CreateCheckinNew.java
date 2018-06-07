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

public class CreateCheckinNew {
	public static void getCheckin() throws IOException {
		Connection con = GetConnection.getCon();
		String sql = "select user_id,latitude,longitude,location_id,time,content,region from checkinnew";
		PreparedStatement ps = null;
		ResultSet rs = null;
		String train = "F:\\WorkSpace\\JIM\\JIM\\input\\deal\\checkin2.txt";
		String predict = "F:\\WorkSpace\\JIM\\JIM\\input\\deal\\predict2.txt";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			int index = 0;
			while (rs.next()) {
				String tmp = rs.getInt(1) + "--" + rs.getDouble(2) + "--" + rs.getDouble(3) + "--" + rs.getInt(4) + "--"
						+ rs.getString(5) + "--" + rs.getString(6) + "--" + rs.getInt(7);
				writeFileContent(train, tmp);
				if (index % 2 == 1) {
					writeFileContent(predict, tmp);
				}
				index++;
				if (index % 1000 == 0)
					System.out.println(index);
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

	public static void main(String[] args) throws IOException {
		getCheckin();
	}
}

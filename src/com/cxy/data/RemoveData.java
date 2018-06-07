package com.cxy.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

public class RemoveData {
	public static void remove() throws IOException {
		BufferedReader br1 = new BufferedReader(
				new InputStreamReader(new FileInputStream("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\aa.txt")));
		BufferedReader br2 = new BufferedReader(
				new InputStreamReader(new FileInputStream("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\TEST.txt")));
		BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream("F:\\WorkSpace\\JIM\\JIM\\input\\deal\\JIMTEST.txt")));

		HashMap<Integer, Double> map = new HashMap<>();

		String line = br1.readLine();

		while (line != null) {
			String[] strings = line.split(" ");
			map.put(Integer.parseInt(strings[0]), Double.parseDouble(strings[3]));
			line = br1.readLine();
		}
		System.out.println(map);
		br1.close();
		line = br2.readLine();
		while (line != null) {
			String[] strings = line.split("--");
			int id = Integer.parseInt(strings[0]);
			if (!(map.get(id) < 0.05)) {
				bw.write(line + "\n");
			}
			line = br2.readLine();
		}
		bw.flush();
		bw.close();
		br2.close();
	}

	public static void main(String[] args) {
		try {
			remove();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

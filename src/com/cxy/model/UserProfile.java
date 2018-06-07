package com.cxy.model;

import java.util.HashMap;

public class UserProfile {
	int index = 0;
	int user_id;
	Point point;
	HashMap<Integer, Integer[]> items = new HashMap<>();

	public UserProfile(int userid, Point point) {
		user_id = userid;
		this.point = point;
	}

	public void addItem(int item) {
		Integer[] rt = new Integer[3];
		rt[0] = item;
		items.put(index++, rt);
	}

	public int getItem(int j) {
		return items.get(j)[0];
	}

	public int getZ(int locationid) {
		return items.get(locationid)[2];
	}

	public int getX(int locationid) {
		return items.get(locationid)[1];
	}

	public void setX(int locationid, int region) {
		Integer[] rt = items.get(locationid);
		rt[1] = region;
	}

	public void setZ(int locationid, int topic) {
		Integer[] rt = items.get(locationid);
		rt[2] = topic;
	}
}
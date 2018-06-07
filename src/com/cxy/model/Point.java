package com.cxy.model;

public class Point {
	double x;
	double y;
	int id;

	public Point() {

	}

	public Point(double x, double y, int lid) {
		this.x = x;
		this.y = y;
		this.id = lid;
	}

	public int getLid() {
		return id;
	}

	public void setLid(int lid) {
		this.id = lid;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}
}

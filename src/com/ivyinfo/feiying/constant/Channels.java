package com.ivyinfo.feiying.constant;

public enum Channels {
	video(0),
	movie(1),
	series(2),
	news(3),
	fun(4),
	music(5),
	sports(6),
	fashion(7),
	entertainment(8),
	variety(9);
	
	private int value;
	
	private Channels(int value) {
		this.value = value;
	}
	
	public int value() {
		return value;
	}
}

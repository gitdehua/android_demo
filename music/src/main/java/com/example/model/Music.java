package com.example.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Music {
	private int id;
	private String name;
	private String path;
	private String[] singer;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getSinger() {
		return singer;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setSinger(String[] singer) {
		this.singer = singer;
	}

	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<>();
		map.put("name", name);
		map.put("path", path);
		map.put("singer", Arrays.toString(singer));
		return map;
	}

	@Override
	public String toString() {
		return "Music [id=" + id + ", name=" + name + ", path=" + path + ", singer=" + Arrays.toString(singer) + "]";
	}

}

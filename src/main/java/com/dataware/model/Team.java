package com.dataware.model;

import java.util.List;

public class Team {
	private int id;
	private String name;
	private List<Member> members;
	
	public Team() {}

	
	public Team(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}


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


	public List<Member> getMembers() {
		return members;
	}


	public void setMembers(List<Member> members) {
		this.members = members;
	}
	
	

}

package com.dolplay.xhcache.assets.domain;

import java.util.Date;

public class User {
	private Long id;
	private String name;
	private String gender;
	private Date birthday;
	private String description;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof User)) {
			return false;
		}
		User user = (User) obj;
		return user.id == id && user.name.equals(name)
				&& user.gender.equals(gender)
				&& user.description.equals(description)
				&& user.birthday.equals(birthday);
	}

}
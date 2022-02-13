package com.rest.mysql.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {

	private String name;
	private String lastName;
	private String birthday;
	private String gender;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	@Override
	public String toString() {
		return "UserInfo [name=" + name + ", lastName=" + lastName + ", birthday=" + birthday + ", gender=" + gender
				+ "]";
	}
	
	public static UserInfo updateUserInfo(UserInfo actualData, UserInfo newData) {
		
		if (newData.getName() != null) {
			actualData.setName(newData.getName());
		}
		if (newData.getLastName() != null) {
			actualData.setLastName(newData.getLastName());
		}
		if (newData.getBirthday() != null) {
			actualData.setBirthday(newData.getBirthday());
		}
		if (newData.getGender() != null) {
			actualData.setGender(newData.getGender());
		}
		
		return actualData;
	}

}

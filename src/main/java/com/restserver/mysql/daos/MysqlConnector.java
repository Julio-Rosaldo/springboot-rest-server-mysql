package com.restserver.mysql.daos;

import java.util.List;

import com.restserver.mysql.entities.User;

public interface MysqlConnector {

	public List<User> getUsers();
	public User getByEmail(String id);
	public User getByUsername(String id);
	public int postUser(User user);
	public boolean deleteUser(String id);
}

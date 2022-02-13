package com.rest.mysql.daos;

import com.rest.mysql.entities.ResponseData;
import com.rest.mysql.entities.ResponseListData;
import com.rest.mysql.entities.User;

public interface UserTemplate {

	public ResponseListData listUsers();

	public ResponseListData listUsersByName(String name);

	public ResponseData getUser(String id);

	public ResponseData createUser(User user);

	public ResponseData updateUser(String id, User user);

	public ResponseData deleteUser(String id);
}

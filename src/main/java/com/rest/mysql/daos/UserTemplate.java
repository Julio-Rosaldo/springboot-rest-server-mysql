package com.rest.mysql.daos;

import com.rest.mysql.entities.ResponseData;
import com.rest.mysql.entities.ResponseListData;
import com.rest.mysql.entities.User;

public interface UserTemplate {

	public ResponseListData listUsers(Long page, Long pageSize, String email, String name);

	public ResponseData getUser(String id);
	
	public boolean userExists(String id);

	public ResponseData createUser(String id, User user);

	public ResponseData updateUser(String id, User user, boolean rollback);
	
	public ResponseData deleteUsers();

	public ResponseData deleteUser(String id);
}

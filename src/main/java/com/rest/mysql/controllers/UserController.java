package com.rest.mysql.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rest.mysql.daos.UserTemplate;
import com.rest.mysql.entities.ResponseData;
import com.rest.mysql.entities.ResponseListData;
import com.rest.mysql.entities.User;

@RestController
public class UserController {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	@Autowired
	UserTemplate userTemplate;

	@GetMapping(value = "/users")
	public ResponseEntity<ResponseListData> listUsers(@RequestParam(name = "page", required = false) Long page,
			@RequestParam(name = "pageSize", required = false) Long pageSize,
			@RequestParam(name = "email", required = false) String email,
			@RequestParam(name = "userInfo.name", required = false) String userInfoName) {

		ResponseListData data = userTemplate.listUsers(page, pageSize, email, userInfoName);

		HttpStatus status = null;
		if (data.getError() != null) {
			status = data.getError().getStatus();
		} else if (data.getData() == null || data.getData().isEmpty()) {
			status = HttpStatus.NO_CONTENT;
		} else {
			status = HttpStatus.OK;
		}

		ResponseEntity<ResponseListData> response = new ResponseEntity<ResponseListData>(data, null, status);
		return response;
	}

	@GetMapping(value = "/users/{id}")
	public ResponseEntity<ResponseData> getUser(@PathVariable(name = "id", required = true) String id) {

		LOGGER.info(id);

		ResponseData data = userTemplate.getUser(id);

		HttpStatus status = null;
		if (data.getError() != null) {
			status = data.getError().getStatus();
		} else {
			status = HttpStatus.OK;
		}

		ResponseEntity<ResponseData> response = new ResponseEntity<ResponseData>(data, null, status);
		return response;
	}

	@PostMapping(value = "/users")
	public ResponseEntity<ResponseData> createUser(@RequestHeader Map<String, String> headers,
			@RequestBody(required = true) User payload) {

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Custom-Header", "foo");

		ResponseData data = userTemplate.createUser(payload);

		HttpStatus status = null;
		if (data.getError() != null) {
			status = data.getError().getStatus();
		} else {
			status = HttpStatus.CREATED;
		}

		ResponseEntity<ResponseData> response = new ResponseEntity<ResponseData>(data, responseHeaders, status);
		return response;
	}

	@PatchMapping(value = "/users/{id}")
	public ResponseEntity<ResponseData> updateUser(@PathVariable(name = "id", required = true) String id,
			@RequestBody(required = true) User payload) {

		ResponseData data = userTemplate.updateUser(id, payload, false);

		HttpStatus status = null;
		if (data.getError() != null) {
			status = data.getError().getStatus();
		} else {
			status = HttpStatus.OK;
		}

		ResponseEntity<ResponseData> response = new ResponseEntity<ResponseData>(data, null, status);
		return response;
	}

	@DeleteMapping(value = "/users")
	public ResponseEntity<ResponseData> deleteUsers() {
		ResponseData data = userTemplate.deleteUsers();

		HttpStatus status = null;
		if (data.getError() != null) {
			status = data.getError().getStatus();
		} else {
			status = HttpStatus.NO_CONTENT;
		}

		ResponseEntity<ResponseData> response = new ResponseEntity<ResponseData>(data, null, status);
		return response;
	}

	@DeleteMapping(value = "/users/{id}")
	public ResponseEntity<ResponseData> deleteUser(@PathVariable(name = "id", required = true) String id) {
		ResponseData data = userTemplate.deleteUser(id);

		HttpStatus status = null;
		if (data.getError() != null) {
			status = data.getError().getStatus();
		} else {
			status = HttpStatus.NO_CONTENT;
		}

		ResponseEntity<ResponseData> response = new ResponseEntity<ResponseData>(data, null, status);
		return response;
	}

}

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

	private static final String NOT_FOUND_CODE = "EmptyResultDataAccessException";
	private static final String DB_ACCESS_CODE = "DataAccessException";

	@Autowired
	UserTemplate userTemplate;

	@GetMapping(value = "/users")
	public ResponseEntity<ResponseListData> listUsers(
			@RequestParam(name = "userInfo.name", required = false) String userInfoName) {

		LOGGER.info(userInfoName);

		ResponseListData data = null;
		if (userInfoName != null) {
			data = userTemplate.listUsersByName(userInfoName);
		} else {
			data = userTemplate.listUsers();
		}

		HttpStatus status = null;
		if (data.getError() != null) {
			if (data.getError().getCode().equals(DB_ACCESS_CODE)) {
				status = HttpStatus.SERVICE_UNAVAILABLE;
			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}
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
			if (data.getError().getCode().equals(NOT_FOUND_CODE)) {
				status = HttpStatus.NOT_FOUND;
			} else if (data.getError().getCode().equals(DB_ACCESS_CODE)) {
				status = HttpStatus.SERVICE_UNAVAILABLE;
			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}
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
		responseHeaders.add("Content-Type", "application/json");

		ResponseData data = userTemplate.createUser(payload);

		HttpStatus status = null;
		if (data.getError() != null) {
			if (data.getError().getCode().equals("DuplicateKeyException")) {
				status = HttpStatus.BAD_REQUEST;
			} else if (data.getError().getCode().equals(DB_ACCESS_CODE)) {
				status = HttpStatus.SERVICE_UNAVAILABLE;
			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		} else {
			status = HttpStatus.CREATED;
		}

		ResponseEntity<ResponseData> response = new ResponseEntity<ResponseData>(data, responseHeaders, status);
		return response;
	}

	@PatchMapping(value = "/users/{id}")
	public ResponseEntity<ResponseData> updateUser(@PathVariable(name = "id", required = true) String id,
			@RequestBody(required = true) User payload) {

		ResponseData data = userTemplate.updateUser(id, payload);

		HttpStatus status = null;
		if (data.getError() != null) {
			if (data.getError().getCode().equals(NOT_FOUND_CODE)) {
				status = HttpStatus.NOT_FOUND;
			} else if (data.getError().getCode().equals(DB_ACCESS_CODE)) {
				status = HttpStatus.SERVICE_UNAVAILABLE;
			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		} else {
			status = HttpStatus.OK;
		}

		ResponseEntity<ResponseData> response = new ResponseEntity<ResponseData>(data, null, status);
		return response;
	}

	@DeleteMapping(value = "/users/{id}")
	public ResponseEntity<ResponseData> deleteUser(@PathVariable(name = "id", required = true) String id) {
		ResponseData data = userTemplate.deleteUser(id);

		HttpStatus status = null;
		if (data.getError() != null) {
			if (data.getError().getCode().equals(NOT_FOUND_CODE)) {
				status = HttpStatus.NOT_FOUND;
			} else if (data.getError().getCode().equals(DB_ACCESS_CODE)) {
				status = HttpStatus.SERVICE_UNAVAILABLE;
			} else {
				status = HttpStatus.INTERNAL_SERVER_ERROR;
			}
		} else {
			status = HttpStatus.NO_CONTENT;
		}

		ResponseEntity<ResponseData> response = new ResponseEntity<ResponseData>(data, null, status);
		return response;
	}

}

package com.restserver.controllers;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.restserver.mysql.daos.MysqlConnector;
import com.restserver.mysql.entities.User;


@RestController
public class UserController {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	MysqlConnector mysqlConnector;
	
	@GetMapping(value = "/getUsers")
	public List<User> listar() {
		return mysqlConnector.getUsers();
	}
	
	@GetMapping(value = "/email/{id}")
	public User getByEmail(@PathVariable(value = "id") String id) {
		return mysqlConnector.getByEmail(id);
	}
	
	@GetMapping(value = "/username/{id}")
	public User getBuUsarname(@PathVariable(value = "id") String id) {
		return mysqlConnector.getByUsername(id);
	}
	
	@PostMapping(value = "/formPostUser")
	public ResponseEntity<String> formPostUser(@RequestHeader Map<String, String> headers, @RequestParam String body){
		User user = new User();

		LOGGER.info("Headers: {}", headers);
		LOGGER.info("Body: {}", body);
		
		HttpHeaders responseHeaders = new HttpHeaders();
	    headers.put("Custom-Header", "foo");
		
		try {
			user = new ObjectMapper().readValue(body, User.class);
		}
		catch (InvalidFormatException e) {
			LOGGER.info("Formato invalido de parametro: {}", e);
			return new ResponseEntity<String>("{Formato de parametro invalido}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		catch (JsonProcessingException e) {
			LOGGER.info("Ocurrio un error en la conversion: {}", e);
		}		
		
		LOGGER.info("Objeto: {}", user);		
		int result = mysqlConnector.postUser(user);
		
		ResponseEntity<String> response = null;
		if(result == 1) {
			new ResponseEntity<User>(user, responseHeaders, HttpStatus.CREATED);
		}
		else if(result == -1) {
			response = new ResponseEntity<String>("{user or email already exists}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		else {
			response = new ResponseEntity<String>("{internal server error}", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;
	}
	
	@PostMapping(value = "/postUser")
	public ResponseEntity<String> postUser(@RequestHeader Map<String, String> headers, @RequestBody String body){
		User user = new User();

		LOGGER.info("Headers: {}", headers);
		LOGGER.info("Body: {}", body);
		
		HttpHeaders responseHeaders = new HttpHeaders();
		MediaType customMediaType = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
		responseHeaders.setContentType(customMediaType);
	    headers.put("Custom-Header", "foo");
		
		try {
			user = new ObjectMapper().readValue(body, User.class);
		}
		catch (InvalidFormatException e) {
			LOGGER.info("Formato invalido de parametro: {}", e);
			return new ResponseEntity<String>("{Formato de parametro invalido}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		catch (JsonProcessingException e) {
			LOGGER.info("Ocurrio un error en la conversion: {}", e);
		}		
		
		LOGGER.info("Objeto: {}", user);
		int result = mysqlConnector.postUser(user);
		
		ResponseEntity<String> response = null;
		if(result == 1) {
			response = new ResponseEntity<String>(body, responseHeaders, HttpStatus.CREATED);
		}
		else if(result == -1) {
			response = new ResponseEntity<String>("{\n\"message\": \"user or email already exists\"\n}", responseHeaders, HttpStatus.BAD_REQUEST);
		}
		else {
			response = new ResponseEntity<String>("{\n\"message\": \"internal server error\"\n}", responseHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		return response;
	}
}

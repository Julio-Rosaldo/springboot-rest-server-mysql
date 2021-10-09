package com.restserver.mysql.daos;

import java.sql.Timestamp;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.restserver.mysql.entities.User;

@Service
public class MysqlConnectorImpl implements MysqlConnector {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MysqlConnectorImpl.class);
	
	/*
	@Autowired
	private JdbcTemplate jdbcTemplate;
	*/
	
	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	private User createUser(Map<String, Object> map) {	
		User user = new User();
        
        user.setEmail((String)map.get("email"));
        user.setPassword((String)map.get("password"));
        user.setUsername((String)map.get("username"));
        user.setRoles(Collections.singletonList((String)map.get("roles")));
        
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String auxLastchange = dateTimeFormat.format((Timestamp)map.get("lastchange"));
        user.setLastchange(auxLastchange);
        
        user.setName((String)map.get("name"));
        user.setLastname((String)map.get("lastname"));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		String auxBirthday = dateFormat.format((Date)map.get("birthday"));
        user.setBirthday(auxBirthday);
        
        LOGGER.info("Los datos son: {}", user);
        return user;
	}
	
	@Override
	public List<User> getUsers() {
		LOGGER.info("Iniciando getUsers");
		
		List<User> listUser = new ArrayList<>();
		
		try  {	      
	        List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList("select * from users", new HashMap<String, Object>());
	        LOGGER.info(result.toString());
	       
	        for (Map<String, Object> map : result) {
		        listUser.add(createUser(map));
	        }
		}
		catch(EmptyResultDataAccessException e) {
        	LOGGER.info("No se encontro ningun resultado");
        }
		
		LOGGER.info("Terminando getUsers");
		return listUser;
	}

	@Override
	public User getByEmail(String id) {
		Map<String, Object> query = new HashMap<>();
        query.put("email", id);
        
        Map<String, Object> result = null;
        
        try {
        	result = namedParameterJdbcTemplate.queryForMap("select * from users where email = :email", query);
            LOGGER.info(result.toString());
        }
        catch(EmptyResultDataAccessException e) {
        	LOGGER.info("No se encontro ningun resultado para: {}", id);
        }
        
		return result == null ? null : createUser(result);
	}
	
	@Override
	public User getByUsername(String id) {
		Map<String, Object> query = new HashMap<>();
        query.put("username", id);
        
        Map<String, Object> result = null;
        
        try {
        	result = namedParameterJdbcTemplate.queryForMap("select * from users where username = :username", query);
            LOGGER.info(result.toString());
        }
        catch(EmptyResultDataAccessException e) {
        	LOGGER.info("No se encontro ningun resultado para: {}", id);
        }
        
		return result == null ? null : createUser(result);
	}
	
	private static final String POST = "INSERT INTO users (email, password, username, roles, lastchange, name, lastname, birthday) "
			+ "VALUES (:email, :password, :username, :roles, :lastchange, :name, :lastname, :birthday)";

	@Override
	public int postUser(User user) {
		LOGGER.info("Terminando postUser");
		
		//Validar timestamp
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		String auxLastchange = null;
		try {
			Timestamp lastchange = Timestamp.valueOf(user.getLastchange().replace('T', ' ').replace('Z', ' '));
			auxLastchange = dateTimeFormat.format(lastchange);
		} catch (NumberFormatException e) {
			LOGGER.info("Formato de fecha incorrecto");
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		Date birthday = dateFormat.parse(user.getBirthday(), new ParsePosition(0));
		String auxBirthday = dateFormat.format(birthday);
		
		
		LOGGER.info("El ultimo cambio es: {}", auxLastchange);
		LOGGER.info("El cumpleaños es: {}", auxBirthday);
		
		int result = 0;
		if (auxLastchange != null && auxBirthday != null) {
			Map<String, Object> query = new HashMap<>();
	        query.put("username", user.getUsername());
	        query.put("email", user.getEmail());
	        query.put("password", user.getPassword());
	        query.put("roles", user.getRoles().get(0));
	        
	        query.put("lastchange", auxLastchange);
	        query.put("name", user.getName());
	        query.put("lastname", user.getLastname());
	        query.put("birthday", auxBirthday);
	        
	        LOGGER.info("El mapa del query es: {}", query.toString());

			try {
	        	result = namedParameterJdbcTemplate.update(POST, query);
	            LOGGER.info("El resultado del registro fue: {}", result);
	        }
			catch (DuplicateKeyException e) {
				LOGGER.info("El registro ya existe: {}", e);
				result = -1;
			}
			catch (DataAccessException e) {
				LOGGER.info("El registro fallo: {}", e);
			}
		}		
		
		LOGGER.info("Terminando postUser");
		return result;
	}

	@Override
	public boolean deleteUser(String id) {
		// TODO Auto-generated method stub
		return false;
	}
}

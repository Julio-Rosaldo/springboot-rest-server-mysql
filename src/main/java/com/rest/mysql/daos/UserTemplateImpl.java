package com.rest.mysql.daos;

import java.sql.Date;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import com.rest.mysql.entities.ResponseData;
import com.rest.mysql.entities.Error;
import com.rest.mysql.entities.Pagination;
import com.rest.mysql.entities.References;
import com.rest.mysql.entities.ResponseListData;
import com.rest.mysql.entities.User;
import com.rest.mysql.entities.UserInfo;

@Repository
public class UserTemplateImpl implements UserTemplate {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserTemplateImpl.class);

	private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

	@Autowired
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	private User createUserObject(Map<String, Object> map) {

		User user = new User();
		user.setId((String) map.get("id"));
		user.setEmail((String) map.get("email"));
		user.setIsActive((boolean) map.get("is_active"));

		String roles = (String) map.get("roles");
		if (roles != null && !roles.isEmpty()) {
			user.setRoles(Arrays.asList(roles.split(",")));
		}

		UserInfo userInfo = new UserInfo();
		userInfo.setName((String) map.get("name"));
		userInfo.setLastName((String) map.get("last_name"));

		Date date = (Date) map.get("birthday");
		if (date != null) {
			userInfo.setBirthday(FORMATTER.format(date));
		}

		userInfo.setGender((String) map.get("gender"));

		user.setUserInfo(userInfo);

		return user;
	}

	private MapSqlParameterSource createUserMap(User user) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();

		parameters.addValue("email", user.getEmail(), Types.VARCHAR);

		String roles = null;
		if (user.getRoles() != null) {
			StringBuilder stringBuilder = new StringBuilder();
			for (String role : user.getRoles()) {
				if (!role.isEmpty()) {
					stringBuilder.append(",");
					stringBuilder.append(role);
				}
			}
			if (stringBuilder.length() > 0) {
				roles = stringBuilder.toString().substring(1);
			}
		}
		parameters.addValue("roles", roles, Types.VARCHAR);

		parameters.addValue("is_active", user.getIsActive(), Types.BOOLEAN);

		String name = null;
		String last_name = null;
		String birthday = null;
		String gender = null;
		if (user.getUserInfo() != null) {
			name = user.getUserInfo().getName();
			last_name = user.getUserInfo().getLastName();
			birthday = user.getUserInfo().getBirthday();
			gender = user.getUserInfo().getGender();
		}
		parameters.addValue("name", name, Types.VARCHAR);
		parameters.addValue("last_name", last_name, Types.VARCHAR);
		parameters.addValue("birthday", birthday, Types.VARCHAR);
		parameters.addValue("gender", gender, Types.VARCHAR);

		return parameters;
	}

	private static final String COUNT_ALL = "SELECT COUNT(*) AS COUNT FROM users ";
	private static final String FIND_ALL = "SELECT * FROM users ";
	private static final String WHERE = "WHERE ";
	private static final String AND = "AND ";
	private static final String EMAIL_CRITERIA = "email = :email ";
	private static final String NAME_CRITERIA = "name = :name ";
	private static final String PAGINATION = "LIMIT :limit OFFSET :offset";

	@Override
	public ResponseListData listUsers(Long page, Long pageSize, String email, String name) {
		ResponseListData data = new ResponseListData();

		page = page == null ? 0L : page;
		pageSize = pageSize == null ? 10L : pageSize;

		// Prepare query builder
		StringBuilder criteriaBuilder = new StringBuilder();
		Map<String, Object> mapQuery = new HashMap<>();

		// Add optional parameter
		if (email != null) {
			criteriaBuilder.append(AND);
			criteriaBuilder.append(EMAIL_CRITERIA);
			mapQuery.put("email", email);
		}

		// Add optional parameter
		if (name != null) {
			criteriaBuilder.append(AND);
			criteriaBuilder.append(NAME_CRITERIA);
			mapQuery.put("name", name);
		}

		// Build query root
		String countQuery = COUNT_ALL;
		String findQuery = FIND_ALL;

		// Add criteria if exists
		if (!criteriaBuilder.isEmpty()) {
			countQuery = countQuery.concat(WHERE).concat(criteriaBuilder.toString().substring(4));
			findQuery = findQuery.concat(WHERE).concat(criteriaBuilder.toString().substring(4));
		}

		// Finally, add pagination criteria
		findQuery = findQuery.concat(PAGINATION);
		mapQuery.put("limit", pageSize);
		mapQuery.put("offset", page * pageSize);

		try {
			Long totalElements = namedParameterJdbcTemplate.queryForObject(countQuery, mapQuery, Long.class);

			List<Map<String, Object>> listMap = namedParameterJdbcTemplate.queryForList(findQuery, mapQuery);

			List<Object> listUsers = new ArrayList<>();
			for (Map<String, Object> map : listMap) {
				listUsers.add(createUserObject(map));
			}

			if (!listUsers.isEmpty()) {
				Double totalPagesAux = Math.ceil(totalElements.doubleValue() / pageSize.doubleValue());
				Long totalPages = totalPagesAux.longValue();

				Long lastPage = page.equals(totalPages - 1L) ? null : (totalPages - 1L);
				Long nextPage = page.equals(totalPages - 1L) ? null : (page + 1L);
				Long previousPage = page.equals(0L) ? null : (page - 1L);

				References references = new References();
				references.setLastPage(lastPage);
				references.setNextPage(nextPage);
				references.setPreviousPage(previousPage);

				Pagination pagination = new Pagination();
				pagination.setReferences(references);
				pagination.setPage(page);
				pagination.setTotalPages(totalPages);
				pagination.setTotalElements(totalElements);
				pagination.setPageSize(pageSize);

				data.setPagination(pagination);
			}

			data.setData(listUsers);
		} catch (DataAccessException e) {
			Error error = new Error();
			error.setName(e.getClass().getSimpleName());
			error.setMessage(e.getMessage());
			error.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
			data.setError(error);
		}

		LOGGER.info("Data: {}", data);
		return data;
	}

	private static final String FIND_BY_ID = "SELECT * FROM users WHERE id = :id;";

	@Override
	public ResponseData getUser(String id) {
		ResponseData data = new ResponseData();

		Map<String, Object> query = new HashMap<>();
		query.put("id", id);

		try {
			Map<String, Object> result = namedParameterJdbcTemplate.queryForMap(FIND_BY_ID, query);
			data.setData(createUserObject(result));
		} catch (EmptyResultDataAccessException e) {
			Error error = new Error();
			error.setName(e.getClass().getSimpleName());
			error.setMessage(e.getMessage());
			error.setStatus(HttpStatus.NOT_FOUND);
			data.setError(error);
		} catch (DataAccessException e) {
			Error error = new Error();
			error.setName(e.getClass().getSimpleName());
			error.setMessage(e.getMessage());
			error.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
			data.setError(error);
		}

		return data;
	}

	private static final String GENERATE_UUID = "SELECT UUID() AS ID;";
	private static final String CREATE_USER = "INSERT INTO users (id, email, roles, is_active, name, last_name, birthday, gender)"
			+ " VALUES (:id, :email, :roles, :is_active, :name, :last_name, :birthday, :gender);";

	@Override
	public ResponseData createUser(User user) {
		ResponseData data = new ResponseData();

		MapSqlParameterSource query = createUserMap(user);
		try {
			String id = namedParameterJdbcTemplate.queryForObject(GENERATE_UUID, new HashMap<>(), String.class);
			query.addValue("id", id, Types.VARCHAR);

			int result = namedParameterJdbcTemplate.update(CREATE_USER, query);
			if (result == 1) {
				return getUser(id);
			} else {

			}
		} catch (DuplicateKeyException e) {
			Error error = new Error();
			error.setName(e.getClass().getSimpleName());
			error.setMessage(e.getMessage());
			error.setStatus(HttpStatus.BAD_REQUEST);
			data.setError(error);
		} catch (DataAccessException e) {
			Error error = new Error();
			error.setName(e.getClass().getSimpleName());
			error.setMessage(e.getMessage());
			error.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
			data.setError(error);
		}

		return data;
	}

	private static final String UPDATE_USER = "UPDATE users SET email = IFNULL(:email, email), roles = IFNULL(:roles, roles),"
			+ " is_active = IFNULL(:is_active, is_active), name = IFNULL(:name, name), last_name = IFNULL(:last_name, last_name),"
			+ " birthday = IFNULL(:birthday, birthday), gender = IFNULL(:gender, gender) WHERE id = :id;";

	@Override
	@Transactional(rollbackFor = { DataAccessException.class })
	public ResponseData updateUser(String id, User user, boolean rollback) {
		ResponseData data = new ResponseData();

		MapSqlParameterSource query = createUserMap(user);
		query.addValue("id", id, Types.VARCHAR);

		try {
			int result = namedParameterJdbcTemplate.update(UPDATE_USER, query);
			if (rollback) {
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				throw new DataAccessException("Operation rolled back") {
					private static final long serialVersionUID = 6981224632546830983L;
				};
			}
			else if (result == 1) {
				return getUser(id);
			} else {
				throw new EmptyResultDataAccessException(1);
			}
		} catch (EmptyResultDataAccessException e) {
			Error error = new Error();
			error.setName(e.getClass().getSimpleName());
			error.setMessage(e.getMessage());
			error.setStatus(HttpStatus.NOT_FOUND);
			data.setError(error);
		} catch (DataAccessException e) {
			Error error = new Error();
			error.setName(e.getClass().getSimpleName());
			error.setMessage(e.getMessage());
			error.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
			data.setError(error);
		}

		return data;
	}

	private static final String DELETE_USER = "DELETE FROM users WHERE id = :id;";

	@Override
	public ResponseData deleteUser(String id) {
		ResponseData data = new ResponseData();

		Map<String, Object> query = new HashMap<>();
		query.put("id", id);

		try {
			int result = namedParameterJdbcTemplate.update(DELETE_USER, query);
			if (result != 1) {
				throw new EmptyResultDataAccessException(1);
			}
		} catch (EmptyResultDataAccessException e) {
			Error error = new Error();
			error.setName(e.getClass().getSimpleName());
			error.setMessage(e.getMessage());
			error.setStatus(HttpStatus.NOT_FOUND);
			data.setError(error);
		} catch (DataAccessException e) {
			Error error = new Error();
			error.setName(e.getClass().getSimpleName());
			error.setMessage(e.getMessage());
			error.setStatus(HttpStatus.SERVICE_UNAVAILABLE);
			data.setError(error);
		}

		return data;
	}

}

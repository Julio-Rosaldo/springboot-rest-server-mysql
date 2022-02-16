package com.rest.mysql.daos;

import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.rest.mysql.entities.Pagination;
import com.rest.mysql.entities.References;
import com.rest.mysql.entities.User;
import com.rest.mysql.entities.UserInfo;

public class UserConvertionHelper {

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(UserConvertionHelper.class);

	private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

	private UserConvertionHelper() {
	};
	
	public static Pagination createPagination(Long page, Long pageSize, Long totalElements) {
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

		return pagination;
	}

	public static User createUserObject(Map<String, Object> map) {
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

		LocalDateTime localDateTime = (LocalDateTime) map.get("birthday");
		//LOGGER.info("Fecha de salida con formato: {}", localDateTime);
		if (localDateTime != null) {
			// Convierto la fecha al timezone del sistema tomando como referencia el
			// timezone en UTC
			Date birthday = Date.from(localDateTime.atZone(UTC_ZONE).toInstant());
			//LOGGER.info("Fecha de salida: {}", birthday);
			userInfo.setBirthday(birthday);
		}

		userInfo.setGender((String) map.get("gender"));

		user.setUserInfo(userInfo);

		return user;
	}

	public static MapSqlParameterSource createUserMap(User user) {
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
		LocalDateTime birthday = null;
		String gender = null;
		if (user.getUserInfo() != null) {
			name = user.getUserInfo().getName();
			last_name = user.getUserInfo().getLastName();

			// Inicio conversion de fecha

			Date date = user.getUserInfo().getBirthday();
			//LOGGER.info("Fecha de entrada: {}", date);

			// Convierto la fecha del timezone del sistema tomando como referencia el
			// timezone en UTC
			birthday = date.toInstant().atZone(UTC_ZONE).toLocalDateTime();
			//LOGGER.info("Fecha de entrada con formato: {}", birthday);

			// Termina conversion de fecha

			gender = user.getUserInfo().getGender();
		}
		parameters.addValue("name", name, Types.VARCHAR);
		parameters.addValue("last_name", last_name, Types.VARCHAR);
		parameters.addValue("birthday", birthday, Types.TIMESTAMP);
		parameters.addValue("gender", gender, Types.VARCHAR);

		return parameters;
	}
}

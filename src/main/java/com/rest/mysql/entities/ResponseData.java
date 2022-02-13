package com.rest.mysql.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseData {

	private User data;
	private ResponseError error;

	public User getData() {
		return data;
	}

	public void setData(User data) {
		this.data = data;
	}

	public ResponseError getError() {
		return error;
	}

	public void setError(ResponseError error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return "ResponseData [data=" + data + ", error=" + error + "]";
	}

}

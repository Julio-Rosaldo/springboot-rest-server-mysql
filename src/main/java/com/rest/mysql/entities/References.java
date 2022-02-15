package com.rest.mysql.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class References {

	private Long lastPage;
	private Long nextPage;
	private Long previousPage;

	public Long getLastPage() {
		return lastPage;
	}

	public void setLastPage(Long lastPage) {
		this.lastPage = lastPage;
	}

	public Long getNextPage() {
		return nextPage;
	}

	public void setNextPage(Long nextPage) {
		this.nextPage = nextPage;
	}

	public Long getPreviousPage() {
		return previousPage;
	}

	public void setPreviousPage(Long previousPage) {
		this.previousPage = previousPage;
	}

	@Override
	public String toString() {
		return "References [lastPage=" + lastPage + ", nextPage=" + nextPage + ", previousPage=" + previousPage + "]";
	}

}

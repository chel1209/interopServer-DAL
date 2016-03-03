package com.diversityarrays.dal.entity;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Table;



/**
 * @author Raul Hernandez T.
 * @date 2/24/2016 
 */

@Table(name="CsvFile")
@EntityTag("CsvFile")
public class CsvFile extends DalEntity{
	
	@Column(name="colNumHeaders")
	private Integer colNumHeaders;
	
	@Column(name="headers")
	private Map<String,String> headers     = new HashMap<String, String>();
	
	@Column(name="traitValues")
	private Map<String,String> traitValues = new HashMap<String, String>();
	
	public CsvFile(){
		this.colNumHeaders = 7;
	}
	
	public CsvFile(Integer headersNumSize){
		this.colNumHeaders = headersNumSize;
	}

	public Integer getColNumHeaders() {
		return colNumHeaders;
	}

	public void setColNumHeaders(Integer colNumHeaders) {
		this.colNumHeaders = colNumHeaders;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, String> getTraitValues() {
		return traitValues;
	}

	public void setTraitValues(Map<String, String> traitValues) {
		this.traitValues = traitValues;
	}
	
	


}

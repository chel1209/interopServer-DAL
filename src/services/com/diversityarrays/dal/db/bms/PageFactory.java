package com.diversityarrays.dal.db.bms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import net.pearcan.json.JsonMap;
import net.pearcan.json.JsonParser;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.ColumnNameMapping;
import com.diversityarrays.dal.entity.DalEntity;
import com.diversityarrays.dal.entity.Page;
import com.diversityarrays.dal.entity.Site;
import com.diversityarrays.dal.entity.Trial;
import com.diversityarrays.dal.ops.FilteringTerm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;

public class PageFactory implements SqlEntityFactory<Page> {

	private List<Object> environments;
	private int index = 0;
	private boolean pending = false;

	private static final int OBSOLETE = 1;

	public PageFactory() {
	}

	@Override
	public void close() throws IOException {
		// Nothing to do
	}

	@Override
	public String createCountQuery(String filterClause) throws DalDbException {
		return BMSApiDataConnection.getLocationsCall(1,BMSApiDataConnection.BMS_MAX_PAGE_SIZE);
	}

	@Override
	public String createGetQuery(String id, String filterClause)
			throws DalDbException {
		throw new UnsupportedOperationException();
	}

	public String createListTermsQuery(String id, int firstRecord,
			int nRecords, String filterClause) throws DalDbException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String createPagedListQuery(int firstRecord, int nRecords,
			String filterClause) throws DalDbException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Page createEntity(ResultSet rs) throws DalDbException {
		throw new UnsupportedOperationException();
	}

	public Page createEntity(JsonMap jsonMap) throws DalDbException {
		Page result = new Page();
		if(jsonMap.get("pageNumber")!=null){
			result.setPageNumber(Integer.valueOf((String)jsonMap.get("pageNumber")));
			result.setPageSize(Integer.valueOf((String)jsonMap.get("pageSize")));
			result.setTotalResults(Integer.valueOf((String)jsonMap.get("totalResults")));
			result.setTotalPages(Integer.valueOf((String)jsonMap.get("totalPages")));
		}
		return result;
	}

	public String createListStudiesURL(String filterClause) {
		throw new UnsupportedOperationException();
	}

	public String createListStudiesDetailsURL(String id) {
		throw new UnsupportedOperationException();
	}

	public void processDetails(DalEntity entity, BufferedReader reader)
			throws DalDbException {
		throw new UnsupportedOperationException();
	}

	public void processTrial(DalEntity entity, BufferedReader reader)
			throws DalDbException {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return the pending
	 */
	public boolean isPending() {
		return pending;
	}

	/**
	 * @param pending
	 *            the pending to set
	 */
	public void setPending(boolean pending) {
		this.pending = pending;
	}

	@Override
	public String createPagedListQuery(int firstRecord, int nRecords,
			String filterClause, int pageNumber) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

}
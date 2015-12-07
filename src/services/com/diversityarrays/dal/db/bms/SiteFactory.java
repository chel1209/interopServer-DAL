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
import com.diversityarrays.dal.entity.Site;
import com.diversityarrays.dal.entity.Trial;
import com.diversityarrays.dal.ops.FilteringTerm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;

public class SiteFactory implements SqlEntityFactory<Site> {

	private List<Object> environments;
	private int index = 0;
	private boolean pending = false;

	private static final int OBSOLETE = 1;

	static private final ColumnNameMapping COLUMN_NAME_MAPPING;

	static {
		// Ensure the EntityColumn initializers get called !
		new Trial();

		COLUMN_NAME_MAPPING = new ColumnNameMapping(Site.class) {

			@Override
			public FilteringTerm createReplacement(FilteringTerm term,
					String xqueryColumn) throws DalDbException {
				// if (! "GenotypeAliasLang".equalsIgnoreCase(term.columnName))
				// {
				return super.createReplacement(term, xqueryColumn);
				// }

				/*
				 * String queryColumn = "is_obsolote"; if (term.multiple) { //
				 * TODO support IN (see TODO in FilteringTerm for splitting out
				 * values) throw new DalDbException("operator " + term.operator
				 * + " not yet supported for " + term.columnName); }
				 * 
				 * if (FilteringTerm.OPERATOR_LIKE.equals(term.operator)) {
				 * throw new DalDbException("operator " + term.operator +
				 * " not yet supported for " + term.columnName); }
				 * 
				 * // Only normal "comparison" operators now ... try { // Let's
				 * handle a numeric value specially. // Not sure if I should
				 * really do this but... Integer.parseInt(term.value); return
				 * new FilteringTerm("nstat " + term.operator + term.value); }
				 * catch (NumberFormatException e) {
				 * 
				 * // TODO handle quotes in term.value
				 * 
				 * if (matching.isEmpty()) { throw new
				 * DalDbException("No matching NSTAT values for expression: " +
				 * term.rawExpression); }
				 * 
				 * StringBuilder expr = new StringBuilder(queryColumn); if
				 * (matching.size() == 1) {
				 * expr.append(' ').append(term.operator
				 * ).append(' ').append(matching.get(0).value); } else {
				 * expr.append(" IN "); String sep = " ("; for (NamesNSTAT nn :
				 * matching) { expr.append(sep).append(nn.value); sep = ","; }
				 * expr.append(')'); } FilteringTerm result = new
				 * FilteringTerm(expr.toString()); if (result.error != null) {
				 * throw new DalDbException("Internal error: " + result.error +
				 * " for expression '" + expr.toString() + "'"); } return null;
				 * }
				 */
			}

		};

		COLUMN_NAME_MAPPING.addColumn(Site.SITE_ID, "id")
				.addColumn(Site.SITE_NAME, "name")
				.addColumn(Site.SITE_ACRONYM, "abbreviation")
				.addColumn(Site.SITE_TYPE_ID, "id")
				.addColumn(Site.SITE_TYPE_NAME, "name")
				.addColumn(Site.LATITUDE, "latitude")
				.addColumn(Site.LONGITUDE, "longitude");
	}

	public SiteFactory() {
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
			String filterClause,int pageNumber) throws DalDbException {
		return BMSApiDataConnection.getLocationsCall(pageNumber,nRecords);
	}

	@Override
	public Site createEntity(ResultSet rs) throws DalDbException {
		Site result = new Site();

		try {

			result.setSiteId(rs.getInt("site_id"));
			result.setSiteName(rs.getString("name"));
			result.setSiteAcronym(rs.getString("abbreviation"));
			result.setSiteTypeId(rs.getInt("id"));
			result.setSiteTypeName(rs.getString("name"));
			result.setLatitude(rs.getDouble("latitude"));
			result.setLongitude(rs.getDouble("longitude"));
		} catch (SQLException e) {
			throw new DalDbException(e);
		}

		return result;
	}

	public Site createEntity(JsonMap jsonMap) throws DalDbException {
		Site result = null;
		List<Object> list = (List<Object>)jsonMap.get("pageResults");
		if(list.size()>index){
			result = new Site();
			JsonMap map = (JsonMap)list.get(index);
			result.setSiteId(Integer.valueOf((String)map.get("id")));
			result.setSiteName((String)map.get("name"));
			result.setSiteAcronym((String)map.get("abbreviation"));
			JsonMap typeMap = (JsonMap)map.get("locationType");
			result.setSiteTypeId(Integer.valueOf((String)typeMap.get("id")));
			result.setSiteTypeName((String)typeMap.get("name"));
			if((String)map.get("latitude")!=null){
				result.setLatitude(Double.valueOf((String)map.get("latitude")));
			}
			if((String)map.get("longitude")!=null){
				result.setLongitude(Double.valueOf((String)map.get("longitude")));
			}
			index++;
			pending = true;
		}else{
			index = 0;
			pending = false;
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
			String filterClause) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

}
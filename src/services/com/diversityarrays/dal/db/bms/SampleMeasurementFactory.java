package com.diversityarrays.dal.db.bms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import net.pearcan.json.JsonMap;
import net.pearcan.json.JsonParser;

import com.diversityarrays.dal.db.BufferedReaderEntityIterator;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.ColumnNameMapping;
import com.diversityarrays.dal.entity.DAL_Trait;
import com.diversityarrays.dal.entity.GeneralType;
import com.diversityarrays.dal.entity.ItemUnit;
import com.diversityarrays.dal.entity.SampleMeasurement;
import com.diversityarrays.dal.entity.SystemUser;
import com.diversityarrays.dal.entity.Trait;
import com.diversityarrays.dal.entity.TrialTrait;
import com.diversityarrays.dal.entity.TrialUnit;
import com.diversityarrays.dal.ops.FilteringTerm;

public class SampleMeasurementFactory implements SqlEntityFactory<SampleMeasurement> {
	
	private static final int OBSOLETE = 1;
	private String url;
	private boolean pending = false;
	private SystemUser systemUser;
	private HttpPost post;
	private UserFactory userFactory;
	
	static private final ColumnNameMapping COLUMN_NAME_MAPPING;
	
	static {
		// Ensure the EntityColumn initializers get called !
		new SampleMeasurement();
		
		COLUMN_NAME_MAPPING = new ColumnNameMapping(SampleMeasurement.class) {

			@Override
			public FilteringTerm createReplacement(FilteringTerm term, String xqueryColumn) throws DalDbException {
			//	if (! "GenotypeAliasLang".equalsIgnoreCase(term.columnName)) {
					return super.createReplacement(term, xqueryColumn);
				//}

				/*String queryColumn = "is_obsolote";
				if (term.multiple) {
					// TODO support IN (see TODO in FilteringTerm for splitting out values)
					throw new DalDbException("operator " + term.operator + " not yet supported for " + term.columnName);
				}

				if (FilteringTerm.OPERATOR_LIKE.equals(term.operator)) {
					throw new DalDbException("operator " + term.operator + " not yet supported for " + term.columnName);
				}

				// Only normal "comparison" operators now ...
				try {
					// Let's handle a numeric value specially.
					// Not sure if I should really do this but...
					Integer.parseInt(term.value);
					return new FilteringTerm("nstat " + term.operator + term.value);j
				} catch (NumberFormatException e) {

					// TODO handle quotes in term.value

					if (matching.isEmpty()) {
						throw new DalDbException("No matching NSTAT values for expression: " + term.rawExpression);
					}

					StringBuilder expr = new StringBuilder(queryColumn);
					if (matching.size() == 1) {
						expr.append(' ').append(term.operator).append(' ').append(matching.get(0).value);
					}
					else {
						expr.append(" IN ");
						String sep = " (";
						for (NamesNSTAT nn : matching) {
							expr.append(sep).append(nn.value);
							sep = ",";
						}
						expr.append(')');
					}
					FilteringTerm result = new FilteringTerm(expr.toString());
					if (result.error != null) {
						throw new DalDbException("Internal error: " + result.error + " for expression '" + expr.toString() + "'");
					}
					return null;
				}*/
			}
			
		};
		
		COLUMN_NAME_MAPPING
			.addColumn(SampleMeasurement.TRIAL_UNIT_ID, "observationId")
			.addColumn(SampleMeasurement.TRAIT_VALUE, "measurementValue");	
	}
	
	public SampleMeasurementFactory() {
	}

	@Override
	public void close() throws IOException {
		// Nothing to do
	}
	
	@Override
	public String createCountQuery(String filterClause) throws DalDbException {
		StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM cvterm");
		sb.append(" WHERE (is_obsolete!=").append(NamesNSTAT.DELETED.value).append(")");
		
		// TODO test filterClause field name translation
		if (filterClause != null) {
			sb.append(" AND ( ").append(COLUMN_NAME_MAPPING.translate(filterClause)).append(" )");
		}
		return sb.toString();
	}

	@Override
	public String createGetQuery(String id, String filterClause) throws DalDbException {
		StringBuilder sb = new StringBuilder(
				"SELECT cvterm_id, name, definition, is_obsolete FROM cvterm");
		sb.append(" WHERE (is_obsolete!=").append(OBSOLETE).append(")");
		sb.append(" AND (cvterm_id=").append(id).append(")");
		
		// TODO test filterClause field name translation
		if (filterClause != null) {
			sb.append(" AND ( ").append(COLUMN_NAME_MAPPING.translate(filterClause)).append(" )");
		}
		return sb.toString();
	}
	
	public String createListTermsQuery(String id, 
			int firstRecord,
			int nRecords, 
			String filterClause) throws DalDbException 
	{
		StringBuilder sb = new StringBuilder(
				"SELECT cvterm_id, name, definition, is_obsolete FROM cvterm");
		sb.append(" WHERE (is_obsolete!=").append(OBSOLETE).append(")");
		sb.append(" AND (cvterm_id=").append(id).append(")");
		
		// TODO test filterClause field name translation
		if (filterClause != null) {
			sb.append(" AND ( ").append(COLUMN_NAME_MAPPING.translate(filterClause)).append(" )");
		}
		
		if (nRecords > 0) {
			sb.append(" LIMIT ").append(nRecords)
			.append(" OFFSET ").append(firstRecord);
		}
		
		return sb.toString();
	}

	
	@Override
	public String createPagedListQuery(int firstRecord, int nRecords, String filterClause) throws DalDbException {
		
		if (firstRecord < 0 || nRecords <= 0) {
			throw new IllegalArgumentException("firstRecord="+firstRecord+"; nRecords="+nRecords);
		}
		
		StringBuilder sb = new StringBuilder("SELECT cvterm_id, name, definition, is_obsolete FROM cvterm");
		sb.append(" WHERE (is_obsolete!=").append(OBSOLETE).append(")");

		// TODO test filterClause field name translation
		if (filterClause != null) {
			sb.append(" AND ( ").append(COLUMN_NAME_MAPPING.translate(filterClause)).append(" )");
		}
		
		sb.append(" LIMIT ").append(nRecords)
			.append(" OFFSET ").append(firstRecord);
		
		return sb.toString();
	}

	@Override
	public SampleMeasurement createEntity(ResultSet rs) throws DalDbException {
		SampleMeasurement result = new SampleMeasurement();
			
		return result;
	}
	
	public void createEntity(TrialUnit trialUnit, JsonMap jsonMap, List<TrialTrait> trialTraits) throws DalDbException {
		List<Object> list = (List)jsonMap.get("measurements");
		TraitFactory traitFactory = new TraitFactory();
		List<SampleMeasurement> sampleMeasurements = new ArrayList<SampleMeasurement>();
		for(Object map: list){
			SampleMeasurement result = new SampleMeasurement();
			result.setTrialUnitId(Integer.valueOf((String)jsonMap.get("uniqueIdentifier")));
			trialUnit.setTrialUnitId(result.getTrialUnitId());
			trialUnit.setTreatmentId(Integer.valueOf((String)jsonMap.get("environmentNumber")));
			if(((String)jsonMap.get("replicationNumber"))!=null){
				trialUnit.setReplicateNumber(Integer.valueOf((String)jsonMap.get("replicationNumber")));
			}
			result.setTrait(traitFactory.createEntity((JsonMap)((JsonMap)map).get("measurementIdentifier")));
			trialUnit.setTrialUnitNote((String)jsonMap.get("seedSource"));
			trialUnit.setUnitPositionText((String)jsonMap.get("enrtyNumber"));
			if((String)jsonMap.get("plotNumber")!=null){
				trialUnit.setUnitPositionId(Integer.valueOf((String)jsonMap.get("plotNumber")));
			}
			sampleMeasurements.add(result);
		}
		trialUnit.setSampleMeasurements(sampleMeasurements);
	}
	
	public void createEntity(ItemUnit itemUnit, JsonMap jsonMap) throws DalDbException {
		GeneralType result = new GeneralType();
		JsonMap map = (JsonMap)jsonMap.get("dataType");
		if(map!=null){
			result.setTypeId(new Integer((String)map.get("id")));
			result.setTypeName((String)map.get("name"));
			itemUnit.setGeneralType(result);
		}
		
	}
	
	public String createURL(String id){
		return BMSApiDataConnection.getObservationsCall(id);
	}

	@Override
	public SampleMeasurement createEntity(JsonMap jsonMap) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<Object> getObservationsMap(String url) throws DalDbException{
		this.url = url;
		CloseableHttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		BufferedReader bufferedReader;
		
		if(userFactory == null){
			userFactory = new UserFactory();
		}
		
		if(systemUser == null){
			post = new HttpPost(userFactory.createLoginQuery());
			
			StringEntity httpEntity = null;
			try{
				httpEntity = new StringEntity("username=" + BMSApiDataConnection.BMS_USER + "&password=" + BMSApiDataConnection.BMS_PASSWORD);
				httpEntity.setContentType("application/x-www-form-urlencoded");
			}catch(UnsupportedEncodingException uee){
				System.out.println("Exception when setting login parameters" + uee);
				throw new DalDbException(uee);
			}catch(Exception e){
				System.out.println("Exception when setting login parameters" + e);
				throw new DalDbException(e);
			}
			
			if(httpEntity!=null){
				post.setEntity(httpEntity);
			}
					
			try{
				HttpResponse loginResponse = client.execute(post);
				bufferedReader = new BufferedReader(new InputStreamReader(loginResponse.getEntity().getContent()));
				BufferedReaderEntityIterator<SystemUser> entityIterator = new BufferedReaderEntityIterator<SystemUser>(bufferedReader, userFactory);
				entityIterator.readLine();
				systemUser = entityIterator.nextEntity();				
			}catch(ClientProtocolException cpex){
				throw new DalDbException("Protocol error: " + cpex);
			}catch(IOException ioex){
				throw new DalDbException("Input/Output error when executing request: " + ioex);
			}catch(Exception ex){
				throw new DalDbException("Exception: " + ex);
			}
		}
		
		request.addHeader(BMSApiDataConnection.TOKEN_HEADER, systemUser.getPasswordSalt());		
		
		try{
			HttpResponse response = client.execute(request);
			bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			JsonParser parser = new JsonParser(bufferedReader.readLine());
			
			return parser.getListResult();
		}catch(ClientProtocolException cpex){
			throw new DalDbException("Protocol error: " + cpex);
		}catch(IOException ioex){
			throw new DalDbException("Input/Output error when executing request: " + ioex);
		}catch(Exception ex){
			throw new DalDbException("Exception: " + ex);
		} 
	}

	/**
	 * @return the pending
	 */
	public boolean isPending() {
		return pending;
	}

	/**
	 * @param pending the pending to set
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

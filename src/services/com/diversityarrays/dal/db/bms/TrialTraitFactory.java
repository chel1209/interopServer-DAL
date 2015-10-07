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
import com.diversityarrays.dal.entity.Trial;
import com.diversityarrays.dal.entity.TrialTrait;
import com.diversityarrays.dal.ops.FilteringTerm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;

public class TrialTraitFactory implements SqlEntityFactory<Trial> {
	
	private static final int OBSOLETE = 1;
	private boolean pending;
	
	static private final ColumnNameMapping COLUMN_NAME_MAPPING;
	
	static {
		// Ensure the EntityColumn initializers get called !
		new Trial();
		
		COLUMN_NAME_MAPPING = new ColumnNameMapping(Trial.class) {

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
					return new FilteringTerm("nstat " + term.operator + term.value);
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
			.addColumn(Trial.TRIAL_NOTE, "objective")
			.addColumn(Trial.TRIAL_NAME, "name")
			.addColumn(Trial.TRIAL_ACRONYM, "title")
			.addColumn(Trial.TRIAL_ID, "id")
		;	
	}
	
	public TrialTraitFactory() {
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
	public Trial createEntity(ResultSet rs) throws DalDbException {
		Trial result = new Trial();
		
	
		try {
	
			result.setTrialId(rs.getInt("cvterm_id"));
			result.setTrialName(rs.getString("name"));
			result.setTrialNote(rs.getString("objective"));		
		} catch (SQLException e) {
			throw new DalDbException(e);
		}
		
		return result;
	}
	
	public void createEntity(Trial trial,JsonMap jsonMap) throws DalDbException {
		
		System.out.println(jsonMap.get("id"));
		List<TrialTrait> trialTraits = new ArrayList<TrialTrait>();			
		List<Object> traits = (List)jsonMap.get("traits");
		if(traits != null){
			for(Object map:traits){
				TrialTrait result = new TrialTrait();
				result.setTrialId(trial.getTrialId());
				result.setTraitId(Integer.valueOf((String)((JsonMap)map).get("id")));
				result.setTraitName((String)((JsonMap)map).get("name"));
				trialTraits.add(result);
			}
			trial.setTrialTraits(trialTraits);
		}
	}
	
	public String createListStudiesURL(String filterClause){
		return BMSApiDataConnection.getListStudiesCall(filterClause);
	}
	
	public String createListStudiesDetailsURL(String id){
		return BMSApiDataConnection.getListStudiesDetails(id);
	}
	
	public void processDetails(DalEntity entity, BufferedReader reader) throws DalDbException {
		try{
			String line = reader.readLine();
			
			if(line != null){
				JsonParser parser = new JsonParser(line);
				List<Object> generalInfo = (List)parser.getMapResult().get("generalInfo");
				System.out.println("Trial::" + ((Trial)entity).getTrialId() + "generalInfo" + generalInfo);
				for(Object map:generalInfo){
					if(((JsonMap)map).get("name").equals("PI_NAME")){
						((Trial)entity).setTrialManagerName((String)((JsonMap)map).get("value"));
						System.out.println(" PINAME:: " + ((Trial)entity).getTrialManagerName());
					}else{
						if(((JsonMap)map).get("name").equals("LOCATION_NAME")){
							((Trial)entity).setTrialManagerName((String)((JsonMap)map).get("value"));
							System.out.println(" Location:: " + ((Trial)entity).getSiteName());
						}
					}
				}
				
				//((Trial)entity).setTrialManagerName();
				//System.out.println("::::ENTITY::::" + ((Trial)entity).getTrialManagerName());
				//((Trial)entity).setTrialLocation(generalInfo.get("LOCATION_NAME"));
				//System.out.println("::::ENTITY::::" + ((Trial)entity).getTrialLocation());
			}
			
			
		}catch(ParseException peex){
			throw new DalDbException("Error parsing json: " + peex);
		}catch(IOException ioex){
			throw new DalDbException("Input/Output error while processing details: " + ioex);
		}catch(Exception ex){
			throw new DalDbException("Error iterating buffered reader: " + ex);
		}
	}
	
	public void processTrial(DalEntity entity, BufferedReader reader) throws DalDbException {
		try{
			String line = reader.readLine();
			if(line != null){
				JsonParser parser = new JsonParser(line);
				((Trial)entity).setTrialManagerName((String)parser.getMapResult().get("PI_NAME"));
				((Trial)entity).setSiteName((String)parser.getMapResult().get("LOCATION_NAME"));
			}
			
			
		}catch(ParseException peex){
			throw new DalDbException("Error parsing json: " + peex);
		}catch(IOException ioex){
			throw new DalDbException("Input/Output error while processing details: " + ioex);
		}catch(Exception ex){
			throw new DalDbException("Error iterating buffered reader: " + ex);
		}		
	}

	@Override
	public Trial createEntity(JsonMap jsonMap) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
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
	
}
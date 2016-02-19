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
import com.diversityarrays.dal.ops.FilteringTerm;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;

public class TrialFactory implements SqlEntityFactory<Trial> {

	private List<Object> environments;
	private int index = 0;
	private boolean pending = false;
	private String programUniqueId = null;
	private String location = null;
	private String season = null;
	private boolean processTraits;
	private boolean processPlots;

	private static final int OBSOLETE = 1;

	static private final ColumnNameMapping COLUMN_NAME_MAPPING;

	static {
		// Ensure the EntityColumn initializers get called !
		new Trial();

		COLUMN_NAME_MAPPING = new ColumnNameMapping(Trial.class) {

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

		COLUMN_NAME_MAPPING.addColumn(Trial.TRIAL_NOTE, "objective")
				.addColumn(Trial.TRIAL_NAME, "name")
				.addColumn(Trial.TRIAL_ACRONYM, "title")
				.addColumn(Trial.TRIAL_ID, "id");
	}

	public TrialFactory() {
	}

	@Override
	public void close() throws IOException {
		// Nothing to do
	}

	/*@Override
	public String createCountQuery(String filterClause) throws DalDbException {
		splitFilterClause(filterClause);
		return BMSApiDataConnection.getTrialSearchCall(programUniqueId,null,location,season);
	}*/
	
	@Override
	public String createCountQuery(String filterClause) throws DalDbException {
		StringBuilder sb = new StringBuilder("select count(1) from group_member gm,group_study gs where gm.user_id =").append(filterClause).append(" and gm.group_id = gs.group_id;");
	
		return sb.toString();
	}
	
	

	@Override
	public String createGetQuery(String id, String filterClause)
			throws DalDbException {
		StringBuilder sb = new StringBuilder(
				"SELECT cvterm_id, name, definition, is_obsolete FROM cvterm");
		sb.append(" WHERE (is_obsolete!=").append(OBSOLETE).append(")");
		sb.append(" AND (cvterm_id=").append(id).append(")");

		// TODO test filterClause field name translation
		if (filterClause != null) {
			sb.append(" AND ( ")
					.append(COLUMN_NAME_MAPPING.translate(filterClause))
					.append(" )");
		}
		return sb.toString();
	}

	public String createListTermsQuery(String id, int firstRecord,
			int nRecords, String filterClause) throws DalDbException {
		StringBuilder sb = new StringBuilder(
				"SELECT cvterm_id, name, definition, is_obsolete FROM cvterm");
		sb.append(" WHERE (is_obsolete!=").append(OBSOLETE).append(")");
		sb.append(" AND (cvterm_id=").append(id).append(")");

		// TODO test filterClause field name translation
		if (filterClause != null) {
			sb.append(" AND ( ")
					.append(COLUMN_NAME_MAPPING.translate(filterClause))
					.append(" )");
		}

		if (nRecords > 0) {
			sb.append(" LIMIT ").append(nRecords).append(" OFFSET ")
					.append(firstRecord);
		}

		return sb.toString();
	}

	@Override
	public String createPagedListQuery(int firstRecord, int nRecords,
			String filterClause) throws DalDbException {

		if (firstRecord < 0 || nRecords <= 0) {
			throw new IllegalArgumentException("firstRecord=" + firstRecord
					+ "; nRecords=" + nRecords);
		}

		StringBuilder sb = new StringBuilder("select project_id from group_member gm, group_study gs where gm.user_id = ").append(filterClause).append(" and gm.group_id = gs.group_id");
		
		//CCB validar la implementación de la paginación
		sb.append(" LIMIT ").append(nRecords).append(" OFFSET ").append(firstRecord);

		return sb.toString();
	}

	@Override
	public Trial createEntity(ResultSet rs) throws DalDbException {
		Trial result = new Trial();

		try {

			result.setTrialId(rs.getInt(1));
		} catch (SQLException e) {
			throw new DalDbException(e);
		} catch (Exception e) {
			throw new DalDbException(e);
		} 

		return result;
	}

	public Trial createEntity(JsonMap jsonMap) throws DalDbException {
		Trial result = new Trial();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		System.out.println(jsonMap.get("id"));
		result.setTrialId(new Integer((String) jsonMap.get("id")));
		result.setTrialName((String) jsonMap.get("name"));
		result.setTrialNote((String) jsonMap.get("objective"));
		result.setTrialAcronym((String) jsonMap.get("title"));

		if(((String) jsonMap.get("startDate")).length()>0 && !((String)jsonMap.get("startDate")).equals("null")){
			try {
				result.setTrialStartDate(new Date(formatter.parse(
						(String) jsonMap.get("startDate")).getTime()));
			} catch (ParseException pe) {
				throw new DalDbException("Error parsing Start Date"
						+ pe.getMessage());
			} catch (Exception e) {
				throw new DalDbException("Error parsing Start Date"
						+ e.getMessage());
			}
		}

		if (((String) jsonMap.get("endDate")).length() > 0
				&& !jsonMap.get("endDate").equals("null")) {
			try {
				result.setTrialEndDate(new Date(formatter.parse(
						(String) jsonMap.get("endDate")).getTime()));
			} catch (ParseException pe) {
				throw new DalDbException("Error parsing End Date"
						+ pe.getMessage());
			} catch (Exception e) {
				throw new DalDbException("Error parsing End Date"
						+ e.getMessage());
			}
		}

		List<Object> generalInfo = (List) jsonMap.get("generalInfo");
		if (generalInfo != null) {
			//System.out.println("Trial::" + ((Trial) result).getTrialId()
			//		+ "generalInfo" + generalInfo);

			for (Object map : generalInfo) {
				if (((JsonMap) map).get("name").equals("PI_NAME")) {
					((Trial) result)
							.setTrialManagerName((String) ((JsonMap) map)
									.get("value"));
					continue;
				} else {
					if (((JsonMap) map).get("name").equals("LOCATION_NAME")) {
						((Trial) result).setSiteName((String) ((JsonMap) map)
								.get("value"));
						continue;
					} else {
						if (((JsonMap) map).get("name").equals("PI_NAME_ID")) {
							((Trial) result).setTrialManagerId(Integer
									.valueOf((String) ((JsonMap) map)
											.get("value")));
							continue;
						} else {
							if (((JsonMap) map).get("name")
									.equals("STUDY_TYPE")) {
								((Trial) result)
										.setTrialTypeName((String) ((JsonMap) map)
												.get("value"));
								continue;
							} else {
								if (((JsonMap) map).get("name").equals(
										"LOCATION_ID")) {
									if(((String) ((JsonMap)map).get("value")).length() > 0 && !((String) ((JsonMap)map).get("value")).equals("null")){
										((Trial) result).setSiteNameID((Integer) ((JsonMap) map).get("value"));
										continue;
									}
								}
							}
						}
					}
				}
			}

		}

		if(processTraits){
			TrialTraitFactory trialTraitFactory = new TrialTraitFactory();
			trialTraitFactory.createEntity(result, jsonMap);
		}

		if (environments == null) {
			environments = (List) jsonMap.get("environments");
		}
	
		if(environments != null){
			if (environments.size() > 0) {
				//System.out.println("Trial::" + ((Trial) result).getTrialId()
					//	+ "environment details" + environments);
	
				if (index < environments.size()) {
					
					System.out.println("<<<<<<<<<INDEX>>>>>>>>>" + index);
					
					JsonMap map = (JsonMap) environments.get(index);
					
					index++;
					pending = true;
					
					List<Object> environmentDetails = (List) ((JsonMap) map)
							.get("environmentDetails");
					if (environmentDetails != null && environmentDetails.size() > 0) {
						for (Object mapDetails : environmentDetails) {
							if (((JsonMap) mapDetails).get("name").equals(
									"EXPT_DESIGN")) {
								((Trial) result)
										.setDesignTypeName((String) ((JsonMap) mapDetails)
												.get("value"));
								continue;
							} else {
								if (((JsonMap) mapDetails).get("name").equals(
										"LOCATION_NAME")) {
									((Trial) result)
											.setSiteName((String) ((JsonMap) mapDetails)
													.get("value"));
									continue;
								} else {
									if (((JsonMap) mapDetails).get("name").equals(
											"LOCATION_NAME_ID")) {
										((Trial) result)
												.setSiteNameID(Integer
														.valueOf((String) ((JsonMap) mapDetails)
																.get("value")));
										continue;
									}
								}
							}
						}
					}
					if(index == environments.size()){
						pending = false;
						environments = null;
						index = 0;
					}
					
					if(processPlots){
						TrialUnitFactory trialUnitFactory = new TrialUnitFactory();
						trialUnitFactory.createEntity(result, jsonMap);
					}
					
					if(((Trial)result).getSiteName() != null && ((Trial)result).getSiteNameID() != null){
						return result;
					}
				}
			}else{
				environments = null;
			}
		}

		return result;
	}

	public String createListStudiesURL(String filterClause) {
		splitFilterClause(filterClause);
		return BMSApiDataConnection.getTrialSearchCall(programUniqueId,null,location,season);
	}

	public String createListStudiesDetailsURL(String id) {
		return BMSApiDataConnection.getListStudiesDetails(id);
	}

	public void processDetails(DalEntity entity, BufferedReader reader)
			throws DalDbException {
		try {
			String line = reader.readLine();
			JsonParser parser = null;

			if (line != null) {
				parser = new JsonParser(line);
				List<Object> generalInfo = (List) parser.getMapResult().get("generalInfo");
				//System.out.println("Trial::" + ((Trial) entity).getTrialId() + "generalInfo" + generalInfo);
				for (Object map : generalInfo) {
					if (((JsonMap) map).get("name").equals("PI_NAME")) {
						((Trial) entity)
								.setTrialManagerName((String) ((JsonMap) map)
										.get("value"));
						//System.out.println(" PINAME:: "
						//		+ ((Trial) entity).getTrialManagerName());
					} else {
						if (((JsonMap) map).get("name").equals("LOCATION_NAME")) {
							((Trial) entity)
									.setSiteName((String) ((JsonMap) map)
											.get("value"));
							//System.out.println(" Location:: "
							//		+ ((Trial) entity).getSiteName());
						}
					}
				}

			}
			
			entity = getEnvironments(parser.getMapResult(), (Trial)entity);
			
		} catch (ParseException peex) {
			throw new DalDbException("Error parsing json: " + peex);
		} catch (IOException ioex) {
			throw new DalDbException(
					"Input/Output error while processing details: " + ioex);
		} catch (Exception ex) {
			throw new DalDbException("Error iterating buffered reader: " + ex);
		}
	}

	public void processTrial(DalEntity entity, BufferedReader reader)
			throws DalDbException {
		try {
			String line = reader.readLine();
			if (line != null) {
				JsonParser parser = new JsonParser(line);
				((Trial) entity).setTrialManagerName((String) parser
						.getMapResult().get("PI_NAME"));
				((Trial) entity).setSiteName((String) parser.getMapResult()
						.get("LOCATION_NAME"));
			}

		} catch (ParseException peex) {
			throw new DalDbException("Error parsing json: " + peex);
		} catch (IOException ioex) {
			throw new DalDbException(
					"Input/Output error while processing details: " + ioex);
		} catch (Exception ex) {
			throw new DalDbException("Error iterating buffered reader: " + ex);
		}
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
	
	public Trial getEnvironments(JsonMap jsonMap, Trial entity) throws DalDbException {
		
		if (environments == null) {
			environments = (List) jsonMap.get("environments");
		}
	
		if(environments != null){
			if (environments.size() > 0) {
				//System.out.println("Trial::" + ((Trial) entity).getTrialId()
				//		+ "environment details" + environments);
	
				if (index < environments.size()) {
					
					JsonMap map = (JsonMap) environments.get(index);
					
					index++;
					pending = true;
					
					List<Object> environmentDetails = (List) ((JsonMap) map)
							.get("environmentDetails");
					if (environmentDetails != null && environmentDetails.size() > 0) {
						for (Object mapDetails : environmentDetails) {
							if (((JsonMap) mapDetails).get("name").equals(
									"EXPT_DESIGN")) {
								((Trial) entity)
										.setDesignTypeName((String) ((JsonMap) mapDetails)
												.get("value"));
							} else {
								if (((JsonMap) mapDetails).get("name").equals(
										"LOCATION_NAME")) {
									((Trial) entity)
											.setSiteName((String) ((JsonMap) mapDetails)
													.get("value"));
								} else {
									if (((JsonMap) mapDetails).get("name").equals(
											"LOCATION_ID")) {
										((Trial) entity)
												.setSiteNameID(Integer
														.valueOf((String) ((JsonMap) mapDetails)
																.get("value")));
									}
								}
							}
						}
					}
					if(index == environments.size()){
						pending = false;
						environments = null;
						index = 0;
					}
					
					/*TrialUnitFactory trialUnitFactory = new TrialUnitFactory();
					trialUnitFactory.createEntity(entity, jsonMap);*/
					
					if(((Trial)entity).getSiteName() != null && ((Trial)entity).getSiteNameID() != null){
						entity = new Trial(entity);
						return entity;
					}
				}
			}
		}
		
		return entity;
		
	}
	
	private void splitFilterClause(String filterClause){
		//String filterClauseSplit = filterClause.split("=")[1];
		if(filterClause.contains("%26")){
			String[] splitArray = filterClause.split("%26");
			for(String arrayContents : splitArray){
				//System.out.println("ArrayContents>>>>" + arrayContents);
				if(arrayContents.contains("ProjectId")){
					programUniqueId = splitArray[1];
				}
				if(arrayContents.contains("SiteId")){
					location = splitArray[1];
				}
				if(arrayContents.contains("Season")){
					season = splitArray[1].replaceAll(" ", "%20");
				}				
			}
		}else{
			if(filterClause.contains("%3D")){
				String[] splitArray = filterClause.split("%3D");
				for(String arrayContents : splitArray){
					if(arrayContents.contains("ProjectId")){
						programUniqueId = splitArray[1];
					}
					if(arrayContents.contains("SiteId")){
						location = splitArray[1];
					}
					if(arrayContents.contains("Season")){
						season = splitArray[1].replaceAll(" ", "%20");
					}
				}
			}else{
				String[] splitArray = filterClause.split("=");
				for(String arrayContents : splitArray){
					if(arrayContents.contains("ProjectId")){
						programUniqueId = splitArray[1];
					}
					if(arrayContents.contains("SiteId")){
						location = splitArray[1];
					}
					if(arrayContents.contains("Season")){
						season = splitArray[1].replaceAll(" ", "%20");
					}
				}
			}
		}
	}
	
	public void setProcessTraits(boolean processTraits){
		this.processTraits = processTraits;
	}
	
	public void setProcessPlots(boolean processPlots){
		this.processPlots = processPlots;
	}

}
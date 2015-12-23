package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.ColumnNameMapping;
import com.diversityarrays.dal.entity.GeneralType;
import com.diversityarrays.dal.entity.ItemUnit;
import com.diversityarrays.dal.ops.FilteringTerm;

public class GeneralTypeFactory implements SqlEntityFactory<GeneralType> {
	
	private static final int OBSOLETE = 1;
	private boolean pending = false;
	
	static private final ColumnNameMapping COLUMN_NAME_MAPPING;
	
	static {
		// Ensure the EntityColumn initializers get called !
		new GeneralType();
		
		COLUMN_NAME_MAPPING = new ColumnNameMapping(GeneralType.class) {

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
			.addColumn(GeneralType.TYPE_ID, "cvtermid")
			.addColumn(GeneralType.TYPE_NAME, "name")
			.addColumn(GeneralType.TYPE_NOTE, "definition")
			.addColumn(GeneralType.IS_TYPE_ACTIVE, "is_obsolete")
		;	
	}
	
	public GeneralTypeFactory() {
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
	public GeneralType createEntity(ResultSet rs) throws DalDbException {
		GeneralType result = new GeneralType();
		
	
		try {
	
			result.setTypeId(rs.getInt("cvterm_id"));
			result.setTypeName(rs.getString("name"));
			result.setTypeNote(rs.getString("definition"));		
			result.setTypeActive(rs.getBoolean("is_obsolete"));
		} catch (SQLException e) {
			throw new DalDbException(e);
		}
		
		return result;
	}
	
	public GeneralType createEntity(JsonMap jsonMap) throws DalDbException {
		GeneralType result = new GeneralType();
		result.setTypeId(new Integer((String)jsonMap.get("id")));
		System.out.println("TypeId: " + result.getTypeId());
		result.setTypeName((String)jsonMap.get("name"));
		System.out.println("Name: " + result.getTypeName());
		result.setTypeNote((String)jsonMap.get("description"));
		System.out.println("Description: " + result.getTypeNote());
		System.out.println("Is Active? " + result.isTypeActive());
		//result.setTypeActive(jsonMap.get("is_obsolete"));
		
		return result;
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
	
	public String createListTermsURL(String id){
		return BMSApiDataConnection.getGeneralTypeCall(id);
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

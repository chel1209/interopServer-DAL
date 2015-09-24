package com.diversityarrays.dal.db.bms;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.ColumnNameMapping;
import com.diversityarrays.dal.entity.DAL_Trait;
import com.diversityarrays.dal.entity.DalEntity;
import com.diversityarrays.dal.entity.Trial;
import com.diversityarrays.dal.ops.FilteringTerm;

public class TraitFactory implements SqlEntityFactory<DAL_Trait> {
	
	private boolean pending = false;
	private static final int OBSOLETE = 1;
	
	static private final ColumnNameMapping COLUMN_NAME_MAPPING;
	
	static {
		// Ensure the EntityColumn initializers get called !
		new Trial();
		
		COLUMN_NAME_MAPPING = new ColumnNameMapping(DAL_Trait.class) {

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
			.addColumn(DAL_Trait.TRAIT_ID, "objective")
			.addColumn(DAL_Trait.TRAIT_NAME, "name")
		;	
	}
	
	public TraitFactory() {
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
	public DAL_Trait createEntity(ResultSet rs) throws DalDbException {
		DAL_Trait result = new DAL_Trait();
		
		try {
	
			result.setTraitId(rs.getInt("traitId"));
			result.setTraitName(rs.getString("traitName"));	
		} catch (SQLException e) {
			throw new DalDbException(e);
		}
		
		return result;
	}
	
	public DAL_Trait createEntity(JsonMap jsonMap) throws DalDbException {
		DAL_Trait result = new DAL_Trait();

		result.setTraitId((Integer)jsonMap.get("traitId"));
		result.setTraitName((String)jsonMap.get("traitName"));	
		
		return result;
	}
	
	public String createListStudiesURL(String filterClause){
		return null;
	}
	
	public String createListStudiesDetailsURL(String id){
		return null;
	}
	
	public void processDetails(DalEntity entity, BufferedReader reader) throws DalDbException {

	}
	
	public void processTrial(DalEntity entity, BufferedReader reader) throws DalDbException {
	
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
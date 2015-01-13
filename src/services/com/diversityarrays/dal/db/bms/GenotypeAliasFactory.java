/*
 * dalserver-interop library - implementation of DAL server for interoperability
 * Copyright (C) 2015  Diversity Arrays Technology
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.ColumnNameMapping;
import com.diversityarrays.dal.entity.GenotypeAlias;
import com.diversityarrays.dal.ops.FilteringTerm;

public class GenotypeAliasFactory implements SqlEntityFactory<GenotypeAlias> {
	
	static private final ColumnNameMapping COLUMN_NAME_MAPPING;
	
	static {
		// Ensure the EntityColumn initializers get called !
		new GenotypeAlias();
		
		COLUMN_NAME_MAPPING = new ColumnNameMapping(GenotypeAlias.class) {

			@Override
			public FilteringTerm createReplacement(FilteringTerm term, String xqueryColumn) throws DalDbException {
				if (! "GenotypeAliasLang".equalsIgnoreCase(term.columnName)) {
					return super.createReplacement(term, xqueryColumn);
				}

				String queryColumn = "nstat";
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
					List<NamesNSTAT> matching = findMatchingNamesNSTAT(term);
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
					return result;
				}
			}

			private List<NamesNSTAT> findMatchingNamesNSTAT(FilteringTerm term) {
				ScriptEngineManager factory = new ScriptEngineManager();
				ScriptEngine engine = factory.getEngineByName("JavaScript");

				List<NamesNSTAT> matching = new ArrayList<NamesNSTAT>();

				for (NamesNSTAT nn : NamesNSTAT.values()) {
					if (nn.isLanguage()) {
						String lang = nn.language.replaceAll("\'", "\\'");
						String valu = term.value.replaceAll("\'", "\\'");
						
						String expr;
						if ("=".equals(term.operator)) {
							expr =  "'" + lang + "' == '"+ valu + "'";
						}
						else {
							expr =  "'" + lang + "' " + term.operator + " '"+ valu + "'";
						}
				        
				        try {
							Object eval = engine.eval(expr);
							if (eval instanceof Boolean && ((Boolean) eval).booleanValue()) {
								matching.add(nn);
							}
						} catch (ScriptException se) {
							System.err.println(term.toString() + ": " + se.getMessage());
						}

					}
				}
				return matching;
			}
			
		};
		
		COLUMN_NAME_MAPPING
			.addColumn(GenotypeAlias.GENOTYPE_ALIAS_ID,     "nid")
			.addColumn(GenotypeAlias.GENOTYPE_ALIAS_NAME,   "nval")
			.addColumn(GenotypeAlias.GENOTYPE_ID,           "gid")
			.addColumn(GenotypeAlias.GENOTYPE_ALIAS_TYPE,   "ntype")
			.addColumn(GenotypeAlias.GENOTYPE_ALIAS_STATUS, "nstat")
			.addColumn(GenotypeAlias.GENOTYPE_ALIAS_LANG,   "*SPECIAL*")
		;	
	}
	
	public GenotypeAliasFactory() {
	}

	@Override
	public void close() throws IOException {
		// Nothing to do
	}
	
	@Override
	public String createCountQuery(String filterClause) throws DalDbException {
		StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM NAMES");
		sb.append(" WHERE (nstat!=").append(NamesNSTAT.DELETED.value).append(")");
		
		// TODO test filterClause field name translation
		if (filterClause != null) {
			sb.append(" AND ( ").append(COLUMN_NAME_MAPPING.translate(filterClause)).append(" )");
		}
		return sb.toString();
	}

	@Override
	public String createGetQuery(String id, String filterClause) throws DalDbException {
		StringBuilder sb = new StringBuilder(
				"SELECT nid, gid, ntype, nstat, nval FROM NAMES");
		sb.append(" WHERE (nstat!=").append(NamesNSTAT.DELETED.value).append(")");
		sb.append(" AND (nid=").append(id).append(")");
		
		// TODO test filterClause field name translation
		if (filterClause != null) {
			sb.append(" AND ( ").append(COLUMN_NAME_MAPPING.translate(filterClause)).append(" )");
		}
		return sb.toString();
	}
	
	public String createListAliasQuery(String id, 
			int firstRecord,
			int nRecords, 
			String filterClause) throws DalDbException 
	{
		StringBuilder sb = new StringBuilder(
				"SELECT nid, gid, ntype, nstat, nval FROM NAMES");
		sb.append(" WHERE (nstat!=").append(NamesNSTAT.DELETED.value).append(")");
		sb.append(" AND (nid=").append(id).append(")");
		
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
		
		StringBuilder sb = new StringBuilder("SELECT nid, gid, ntype, nstat, nval FROM NAMES");
		sb.append(" WHERE (nstat!=").append(NamesNSTAT.DELETED.value).append(")");

		// TODO test filterClause field name translation
		if (filterClause != null) {
			sb.append(" AND ( ").append(COLUMN_NAME_MAPPING.translate(filterClause)).append(" )");
		}
		
		sb.append(" LIMIT ").append(nRecords)
			.append(" OFFSET ").append(firstRecord);
		
		return sb.toString();
	}

	@Override
	public GenotypeAlias createEntity(ResultSet rs) throws DalDbException {
		GenotypeAlias result = new GenotypeAlias();
		
	
		try {
			int nid = rs.getInt("nid");
			int gid = rs.getInt("gid");
			
			int ntype = rs.getInt("ntype");
			int nstat = rs.getInt("nstat");
			
			String nval = rs.getString("nval");

			String genotypeAliasLang = null;
			NamesNSTAT nnstat = NamesNSTAT.lookupByValue(nstat);
			if (nnstat != null && nnstat.isLanguage()) {
				genotypeAliasLang = nnstat.language;
			}
			
			result.setGenotypeAliasId(nid);
			result.setGenotypeAliasName(nval);
			result.setGenotypeId(gid);
			
			result.setGenotypeAliasType(ntype);
			result.setGenotypeAliasStatus(nstat);
			result.setGenotypeAliasLang(genotypeAliasLang);
		} catch (SQLException e) {
			throw new DalDbException(e);
		}
		
		return result;
	}

}

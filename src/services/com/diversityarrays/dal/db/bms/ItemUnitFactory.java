package com.diversityarrays.dal.db.bms;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.pearcan.json.JsonMap;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.ColumnNameMapping;
import com.diversityarrays.dal.entity.GeneralType;
import com.diversityarrays.dal.entity.ItemUnit;
import com.diversityarrays.dal.ops.FilteringTerm;

public class ItemUnitFactory implements SqlEntityFactory<ItemUnit> {
	
	private static final int OBSOLETE = 1;
	private boolean pending;
	
	static private final ColumnNameMapping COLUMN_NAME_MAPPING;
	
	static {
		new ItemUnit();
		
		COLUMN_NAME_MAPPING = new ColumnNameMapping(ItemUnit.class) {

			@Override
			public FilteringTerm createReplacement(FilteringTerm term, String xqueryColumn) throws DalDbException {
				
					return super.createReplacement(term, xqueryColumn);

			}
			
		};
		
		COLUMN_NAME_MAPPING
			.addColumn(ItemUnit.ITEM_UNIT_ID, "cvtermid")
			.addColumn(ItemUnit.ITEM_UNIT_NAME, "name")
			.addColumn(ItemUnit.ITEM_UNIT_NOTE, "definition")
			//.addColumn(ItemUnit.UNIT_TYPE_ID, "dataType.id")
			//.addColumn(ItemUnit.UNIT_TYPE_NAME, "dataType.name")
		;	
	}
	
	public ItemUnitFactory() {
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
	public ItemUnit createEntity(ResultSet rs) throws DalDbException {
		ItemUnit result = new ItemUnit();

		try {
	
			result.setItemUnitId(rs.getInt("cvterm_id"));
			result.setItemUnitName(rs.getString("name"));
			result.setItemUnitNote(rs.getString("definition"));
		} catch (SQLException e) {
			throw new DalDbException(e);
		}
		
		return result;
	}
	
	public ItemUnit createEntity(JsonMap jsonMap) throws DalDbException {

		ItemUnit result = new ItemUnit();
		result.setItemUnitId(new Integer((String)jsonMap.get("id")));
		result.setItemUnitName((String)jsonMap.get("name"));
		result.setItemUnitNote((String)jsonMap.get("description"));
		GeneralTypeFactory generalTypeFactory = new GeneralTypeFactory();
		generalTypeFactory.createEntity(result,jsonMap);
		
		return result;
	}
	
	public String createListTermsURL(String id){
		return "http://teamnz.leafnode.io/bmsapi/ontology/maize/scales/" + id;
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
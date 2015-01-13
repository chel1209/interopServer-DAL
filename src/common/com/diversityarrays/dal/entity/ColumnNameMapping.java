package com.diversityarrays.dal.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.ops.Filtering;
import com.diversityarrays.dal.ops.FilteringTerm;

/**
 * Provides a mapping between the column names of a DalEntity and the
 * column names in the SQL query result. This facilitates translation
 * of the column names in Filtering expressions provided by the
 * client that must be evaluated as part of the database query.
 * <p>
 * If you need to override the behaviour of a particular FilteringTerm,
 * you should replace the <code>createReplacement()</code> method
 * (which can always fallback to <code>super.createReplacement()</code>
 * if you find that is necessary.
 * @author brian
 *
 */
public class ColumnNameMapping {
	
	/**
	 * The DalEntity class that this ColumnNameMapping is defined for.
	 */
	protected final Class<? extends DalEntity> entityClass;
	
	protected final Map<String,String> entityColumnToQueryColumn = new HashMap<String, String>();

	protected final Map<String,EntityColumn> entityColumnByQueryColumnName = new HashMap<String, EntityColumn>();

	public ColumnNameMapping(Class<? extends DalEntity> entityClass) {
		this.entityClass = entityClass;
	}
	
	/**
	 * Add a mapping between the given entity and query column names.
	 * @param entityColumnName is the name of the column from the DalEntity
	 * @param queryColumnName is the name of the column in the SQL query
	 * @return this ColumnNameMapping so that fluent style can be used
	 */
	public ColumnNameMapping addColumn(EntityColumn entityColumn, String queryColumnName) {

		String entityColumnName = entityColumn.getColumnName();
		if (! entityClass.equals(entityColumn.getEntityClass())) {
			throw new IllegalArgumentException("EntityColumn " + entityColumn + " is not for " + entityClass.getName());
		}

//		EntityColumn ec = entityColumnByColumnName.get(entityColumnName.toLowerCase());
//		if (ec == null) {
//			throw new IllegalArgumentException("Column " + entityColumnName + " is not a valid column for " + entityClass.getName());
//		}
		
		entityColumnByQueryColumnName.put(queryColumnName, entityColumn);
		
		entityColumnToQueryColumn.put(entityColumnName.toLowerCase(), queryColumnName);
		return this;
	}
	
	/**
	 * Create a new FilteringTerm that will be used in place of the input <code>term</code>
	 * using the specified <code>queryColumn</code>.
	 * @param originalTerm
	 * @param queryColumn
	 * @return FilteringTerm
	 * @throws DalDbException
	 */
	public FilteringTerm createReplacement(FilteringTerm originalTerm, String queryColumn) throws DalDbException {
		return new FilteringTerm(originalTerm, queryColumn);
	}
	
	/**
	 * Translate the input <code>filterClause</code> into a new one that uses the
	 * <code>queryColumn</code> names as specified by the <code>entityColumnToQueryColumn</code>
	 * map. Replacement FilteringTerm's are created using <code>createReplacement()</code> which
	 * may be overridden to handle special cases.
	 * @param filterClause
	 * @return String
	 * @throws DalDbException
	 */
	public String translate(String filterClause) throws DalDbException {
		Filtering filtering = new Filtering(filterClause);
		
		if (filtering.error != null) {
			throw new DalDbException(filtering.error);
		}
		
		List<FilteringTerm> translated = new ArrayList<FilteringTerm>();
		for (FilteringTerm term : filtering.filteringTerms) {
			
			String queryColumn = entityColumnToQueryColumn.get(term.columnName.toLowerCase());
			if (queryColumn == null) {
				throw new DalDbException("Invalid column name: '" + term.columnName + "'");
			}
			
			FilteringTerm replacement = createReplacement(term, queryColumn);
			translated.add(replacement);
		}
		
		String result = Filtering.buildExpression(translated, entityColumnByQueryColumnName);
		return result;
	}

}
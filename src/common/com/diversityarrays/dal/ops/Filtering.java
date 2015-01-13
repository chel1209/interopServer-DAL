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
package com.diversityarrays.dal.ops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.diversityarrays.dal.entity.EntityColumn;

public class Filtering {

	static public void main(String[] args) {

		String[] tests = {
				"SpecimenId=4&ScaleId=90",
//				"value IN (1,2) & other <3",
//				"value LIKE '%rt'",
//				"value IN (1)",
//				"value IN (a)",
//				"value IN (a,b)",
//				"value IN ('a')",
//				"value IN ('a','b')",
//				"value IN ('ad','bc')",
//

		};
		
		for (String t : tests) {
			System.out.println("TEST: "+t);
			System.out.println("  RAW:\t"+new Filtering(t));
			if (t.indexOf(" IN ")<0) {
				System.out.println("  TRIM:\t"+new Filtering(t.replaceAll(" *", "")));
			}
		}
	}

	public final String expression;
	public final boolean match;
	public final List<FilteringTerm> filteringTerms;
	public final Set<String> columnNames;
	public final String error;
	
	
	public Filtering(String expr) {
		
		this.expression = expr==null ? null : expr.trim();
		if (expression!=null && ! expression.isEmpty()) {


			List<FilteringTerm> terms = new ArrayList<FilteringTerm>();

			FilteringTerm term = new FilteringTerm(expression);

			Set<String> set = new HashSet<String>();
			while (term.match && term.remainder!=null) {
				terms.add(term);
				set.add(term.columnName);
				term = new FilteringTerm(term.remainder);
			}

			match = term.match;
			if (term.match) {
				error = null;

				terms.add(term);
				set.add(term.columnName);

				filteringTerms = Collections.unmodifiableList(terms);

				columnNames = Collections.unmodifiableSet(set);
			}
			else {
				error = term.error;
				filteringTerms = Collections.emptyList();
				columnNames = Collections.emptySet();
			}
		}
		else {
			match = false;
			error = null;
			filteringTerms = Collections.emptyList();
			columnNames = Collections.emptySet();
		}
	}

	@Override
	public String toString() {
		if (match) {
			if (filteringTerms.size()==1) {
				return filteringTerms.get(0).getFilterExpression(null);
			}
			
			StringBuilder sb = new StringBuilder();
			
			String sep = "(";
			for (FilteringTerm term : filteringTerms) {
				sb.append(sep).append(term.getFilterExpression(null));
				sep = ") AND (";
			}
			sb.append(")");
			return sb.toString();
		}
		else {
			return "*** No Match for: "+expression;
		}
	}

	/**
	 * Return null if not match or empty.
	 * @param map 
	 * @return
	 */
	public String buildExpression(Map<String, EntityColumn> columnByName) {
		if (match) {
			return buildExpression(filteringTerms, columnByName);
		}
		return null;
	}
	
	static public String buildExpression(List<FilteringTerm> terms, Map<String, EntityColumn> columnByName) {
		if (terms.size()==1) {
			return terms.get(0).getFilterExpression(columnByName);
		}
		
		StringBuilder sb = new StringBuilder();
		
		String sep = "(";
		for (FilteringTerm term : terms) {
			sb.append(sep).append(term.getFilterExpression(columnByName));
			sep = ") AND (";
		}
		sb.append(")");
		return sb.toString();
	}
	
}
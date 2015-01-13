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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.diversityarrays.dal.db.DbUtil;
import com.diversityarrays.dal.entity.EntityColumn;

public class FilteringTerm {
	
	static public final String OPERATOR_LIKE = "LIKE";

	static public final String OPERATOR_IN = "IN";

	static private Set<String> COMPARISON_OPS = new HashSet<String>(Arrays.asList("< <= = >= > != <>".split(" ")));

	/**
	 * true if the leading part (before '&') is a valid Filtering expression
	 * (i.e. fieldname OP operands)
	 */
	boolean match;
	/**
	 * Non-null if there is non-blank text after an & character.
	 */
	public String remainder;

	/**
	 * Non-null if error.
	 */
	public String error;
	
	/**
	 * true if the value is quoted, false if "integer"
	 */
	public boolean quoted;
	
	
	/**
	 * text before the operator
	 */
	public String columnName;

	public String operator;
	
	/**
	 * text after the operator and until the EOL or &
	 * and has any quotes removed
	 */
	public String value;
	
	
	public String rawExpression;

	public boolean multiple;
	
	
	public FilteringTerm(FilteringTerm term, String queryColumn) {
		this.columnName = queryColumn;

		this.match = term.match;
		this.operator = term.operator;
		this.quoted = term.quoted;
		this.value = term.value;
		this.remainder = term.remainder;
	}
	
	public FilteringTerm(String expr) {
		this.rawExpression = expr.trim();
		
		tryWord_Operator_Operand();
		if (! match) {
			tryIN();
			if (! match) {
				tryLIKE();
				
				// TODO "-EQ (i,j,k)
			}
		}	
		
		if (match) {
			if (remainder!=null) {
				remainder = remainder.trim();
				if (remainder.length()<=0) {
					remainder = null;
				}
			}
		}
		else {
			remainder = null;
		}
	}


	public String getFilterExpression(Map<String, EntityColumn> columnByName) {
		if (! match) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder(columnName);
		sb.append(' ').append(operator).append(' ');
		EntityColumn c = columnByName==null ? null : columnByName.get(columnName);
		if (c==null) {
			sb.append(value);
		}
		else if (c.getSqlDataType().isNumeric()) {
			// note that quotes have been removed
			if (multiple) {
				sb.append('(').append(value).append(')');
			}
			else {
				sb.append(value);
			}
		}
		else {
			// add quotes for non-numerics
			if (multiple) {
				// We didn't remove quotes from 'multiple' args
				sb.append('(').append(value).append(')');
			}
			else {
				sb.append('\'').append(DbUtil.doubleUpSingleQuote(value)).append('\'');	
			}
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		if (match) {
			return columnName + " $ " + operator + " $ " + value + (quoted ? " *QUOTED*" : "") + (remainder==null ? "" : " >>>&"+remainder+"<<<");
		}
		else {
			return "*** No Match for: "+rawExpression;
		}
	}
	
	private void tryLIKE() {
		Pattern p = Pattern.compile("(\\w+)\\s+(LIKE) *('([^']+)')\\s*(&(.*))?$", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(rawExpression);
		if (m.matches()) {
			columnName = m.group(1);
			operator = OPERATOR_LIKE; // m.group(2);
			value = m.group(4).trim();
			
			remainder = m.group(6);
			
			quoted = true;

			if (value.indexOf('%')>=0 || value.indexOf('_')>=0) {
				match = true;
			}
			else {
				error = "No '%' or '_' in LIKE parameter";
			}
		}
	}
	
	/**
	 * Check for expressions of the form <code><i>columnName</i> IN <b>(</b> <i>value-list</i> <b>)</b></code>
	 * where <i>value-list</i> is a comma-separated list of values which are all either numeric or quoted strings.
	 */
	private void tryIN() {
		Pattern p = Pattern.compile("(\\w+)\\s+(IN) *\\(([^)]+)\\)\\s*(&(.*))?$", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(rawExpression);
		if (m.matches()) {
			columnName = m.group(1);
			operator = OPERATOR_IN; // m.group(2);
			value = m.group(3).trim();
			
			remainder = m.group(5);

			// TODO split out the multiple values to handle quoting better
			multiple = true;

			// Now check the operands, comma-separated list of
			// unquoted integers or quoted strings
			Pattern qlist = Pattern.compile("'[^']+'(\\s*,\\s*'[^']+')*$");
			if (qlist.matcher(value).matches()) {
				// Yup. quoted values
				match = true;
				quoted = true;
			}
			else {
				// Unquoted values?
				Pattern uqlist = Pattern.compile("[\\d]+(\\s*,\\s*[\\d]+)*$");
				if (uqlist.matcher(value).matches()) {
					match = true;
				}
				else {
					error = "Invalid value list after 'IN'";
				}
			}
			
		}
	}
	
	private void tryWord_Operator_Operand() {
		Pattern p = Pattern.compile("(\\w+) *([=<>!]+)([^&]*)(&(.*))?$");

		Matcher m = p.matcher(rawExpression); // RELOPS_PATTERN.matcher(expression);
		if (m.matches()) {
			columnName = m.group(1);
			operator = m.group(2);
			value = m.group(3).trim();
			
			remainder = m.group(5);

			if (! COMPARISON_OPS.contains(operator)) {
				error = "Invalid operator: '"+operator+"'";
			}
			else {
				if (value.matches("^'.*'$")) {
					quoted = true;
					value = value.substring(1, value.length()-1);
					match = true;
				}
				else {
					quoted = false;
					try {
						Double.parseDouble(value);
						match = true;
					}
					catch (NumberFormatException e) {
						error = "Invalid operand: '"+value+"'";
					}
				}
			}
		}
	}

}

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
package com.diversityarrays.dal.db;

import static java.util.Locale.ENGLISH;

import java.util.ArrayList;
import java.util.Collection;

public interface DalVocabulary {

	Collection<String> getTableNameStems();

	Collection<String> getExceptionWords(String input);

	Collection<String> getColumnNameStems();

	static public class Util {

		
		static public String capitaliseFirstChar(String input) {
			if (input==null || input.length()<=0)
				return "";		
			if (input.length()<2) {
				return input.toUpperCase();
			}
			return input.substring(0, 1).toUpperCase(ENGLISH) + input.substring(1);
		}


		/**
		 * Convert all stems in the input to Camel-case using the table name stems.
		 * @param input
		 * @param tableStems 
		 * @return a String
		 */
		static public String convertTableNameToCamelCase(String input, DalVocabulary db) {
			StringBuilder sb = new StringBuilder();
			for (String p : splitUsingStems(input, db.getTableNameStems(), false, db)) {
				sb.append(capitaliseFirstChar(p));
			}
			return sb.toString();
		}

		/**
		 * Convert all stems in the input to Camel-case using the column name stems.
		 * @param input
		 * @param db 
		 * @return a String
		 */
		static public String convertColumnNameToCamelCase(String input, DalVocabulary db) {
			StringBuilder sb = new StringBuilder();
			for (String p : splitUsingStems(input, db.getColumnNameStems(), true, db)) {
				sb.append(capitaliseFirstChar(p));
			}
			return sb.toString();
		}

		/**
		 * Convert the stems in the input to headlessCamelCase using the table name stems.
		 * @param input
		 * @param db 
		 * @return a String
		 */
		static public String convertTableNameTo_headlessCamelName(String input, DalVocabulary db) {
			return convertTo_headlessCamelCase(input, db.getTableNameStems(), false, db);
		}

		/**
		 * Convert the stems in the input to headlessCamelCase using the column name stems.
		 * @param input
		 * @param db 
		 * @return a String
		 */
		static public String convertColumnNameTo_headlessCamelName(String input, DalVocabulary db) {
			return convertTo_headlessCamelCase(input, db.getColumnNameStems(), false, db);
		}

		static private String convertTo_headlessCamelCase(String input, Collection<String> stems, boolean checkForIs, DalVocabulary db) {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (String p : splitUsingStems(input, stems, checkForIs, db)) {
				if (first) {
					sb.append(p.toLowerCase());
					first = false;
				}
				else {
					sb.append(capitaliseFirstChar(p));
				}
			}
			return sb.toString();
		}


		static private Collection<String> splitUsingStems(String input, Collection<String> stems, boolean checkForIs, DalVocabulary db)
		{

			Collection<String> parts = db.getExceptionWords(input);
			if (parts==null) {
				parts = new ArrayList<String>();
				boolean first = true;
				String todo = input;
				while (! todo.isEmpty()) {
					String stem = null;
					boolean foundIs = false;
					if (first && checkForIs && todo.startsWith("is")) {
						stem = findMatchingStem(todo.substring(2), stems);
						if (stem!=null) {
							foundIs = true;
							stem = "is";
						}
					}
					else {
						stem = findMatchingStem(todo, stems);
					}


					if (stem==null) {
						parts.add(todo);
						break;
					}

					if (! foundIs) {
						parts.add(stem);
					}
					todo = todo.substring(stem.length());

					first = false;
				}
			}

			return parts;
		}


		static private String findMatchingStem(String input, Collection<String> stems) {
			String result = null;
			for (String stem : stems) {
				if (input.startsWith(stem)) {
					result = stem;
					break;
				}
			}
			return result;
		}

	}
}

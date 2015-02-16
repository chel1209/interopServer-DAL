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
package com.diversityarrays.dal.server;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections15.Closure;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.diversityarrays.dal.ops.Filtering;
import com.diversityarrays.dal.ops.FilteringTerm;

public class TestFiltering {

	private static final boolean SHOULD_PASS = true;
	private static final boolean SHOULD_FAIL = false;


	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	static private final String[] IS_ACTIVE_TESTS = {
		"IsActive < 1",
		"IsActive <= 1",
		"IsActive = 1",
		"IsActive >= 1",
		"IsActive > 1",
		"IsActive != 1",
		"IsActive <> 1",
	};
	
	static private final String[] LIKE_TESTS = {
		"value LIKE '%rt'",
		"value LIKE 'rt%'",
		"value LIKE 'r%t'",
		"value LIKE '%rt%'",

		"value LIKE '_rt'",
		"value LIKE 'rt_'",
		"value LIKE 'r_t'",
		"value LIKE '_rt_'",

		"value LIKE '%r_t'",
		"value LIKE 'r_t%'",
		"value LIKE '%r_t%'",
	};
	
	static private final String[] IN_TESTS_SHOULD_PASS = {
		"value IN (1)",
		"value IN ('a')",
		"value IN ('a','b')",
		"value IN ('ad','bc')",
	};

	static private final String[] IN_TESTS_SHOULD_FAIL = {
		"value IN (a)",
		"value IN (a,b)",
	};

	static private final String[] COMPLEX_TESTS = {
		"value IN (1,2) & other <3",
	};

	static private final Closure<Filtering> SHOW_FILTERING = new Closure<Filtering>() {
		@Override
		public void execute(Filtering f) {
			System.out.println("=== " + f.expression);
			for (FilteringTerm t : f.filteringTerms) {
				System.out.println("\t" + t.columnName + " " + t.operator + " " + t.value);
			}
		}
	};
	
	@Test
	public void testLIKE() {
		testExpressions(SHOULD_PASS, LIKE_TESTS, SHOW_FILTERING, "value");
	}

	@Test
	public void testIsActiveOperators() {
		testExpressions(SHOULD_PASS, IS_ACTIVE_TESTS, null, "IsActive");
	}

	@Test
	public void testComplex() {
		testExpressions(SHOULD_PASS, COMPLEX_TESTS, SHOW_FILTERING, "value", "other");
	}

	@Test
	public void testIN_operator() {
		testExpressions(SHOULD_PASS, IN_TESTS_SHOULD_PASS, SHOW_FILTERING, "value");
		testExpressions(SHOULD_FAIL, IN_TESTS_SHOULD_FAIL, null, "value");
	}

	public void testExpressions(boolean shouldPass, 
			String[] expressions, 
			Closure<Filtering> check,
			String ... expectedColumnNames) 
	{
		for (String expr : expressions) {
			Filtering f = new Filtering(expr);
			if (shouldPass) {
				if (f.error != null) {
					fail(expr+": "+f.error);
				}
				Collection<String> coll = new ArrayList<String>(f.columnNames);
				
				for (String expectedColumnName : expectedColumnNames) {
					if (! coll.remove(expectedColumnName)) {
						fail(expr + ": Missing expected columnName '" + expectedColumnName + "'");
					}
				}
				
				if (! coll.isEmpty()) {
					fail(expr + ": Missing unexpected columnNames: " + join(",", coll));
				}
				if (check != null) {
					check.execute(f);
				}
			}
			else {
				if (f.error == null) {
					fail(expr+": VALID but should not be!");
				}
			}
		}
	}

	static public String join(String delim, Collection<String> coll) {
		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (String s : coll) {
			sb.append(sep).append(s);
			sep = delim;
		}
		return sb.toString();
	}

}

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

public enum NamesNSTAT {
	
	ZERO            ("-", 0, "ZERO"), 
	PREFERRED       ("Preferred", 1, "ASCII"),
	PREFERRED_ABBREV("Abbreviation", 2, "ASCII"),
	CHINESE_GBK ("Chinese GBK", 3, "DBCS", "Chinese"),
	CHINESE_BIG ("Chinese BIG", 4, "??", "Chinese"), // TODO check on what charset this should be
	JAPANESE    ("Japanese", 5, "??", "Japanese"),
	KOREAN      ("Korean", 6, "??", "Korean"),
	PREFERRED_ID("Preferred ID", 8, ""),
	DELETED     ("Deleted", 9, ""),         // "marked as deleted"
	UNICODE     ("Unicode", 10, "Unicode")

	;
	
	public final String display;
	public final int value;
	public final String charset;
	public final String language;
	
	NamesNSTAT(String d, int v, String cs) {
		this(d, v, cs, null);
	}
	
	NamesNSTAT(String d, int v, String cs, String lang) {
		this.display = d;
		this.value = v;
		this.charset = cs;
		this.language = lang;
	}
	
	@Override
	public String toString() {
		return Integer.toString(value, 10);
	}
	
	public boolean isLanguage() {
		return (language != null) && (PREFERRED_ABBREV.value < this.value) && (this.value < DELETED.value);
	}
	
	static public NamesNSTAT lookupByValue(int v) {
		for (NamesNSTAT nn : values()) {
			if (nn.value==v) {
				return nn;
			}
		}
		return null;
	}
	
	static public final NamesNSTAT[] ZERO_OR_PREFERRED = { ZERO, PREFERRED };
	static public final NamesNSTAT[] PREFERRED_OR_ABBREV = { PREFERRED, PREFERRED_ABBREV };
	
	static public String createInClause(NamesNSTAT ... nstats) {
		if (nstats.length<=0) {
			throw new IllegalArgumentException("no NamesNSTAT values supplied");
		}
		StringBuilder sb = new StringBuilder("(");
		String sep = "";
		for (NamesNSTAT nstat : nstats) {
			sb.append(sep).append(nstat.value);
			sep = ",";
		}
		sb.append(")");
		return sb.toString();
	}
	
}

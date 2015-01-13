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
import java.util.List;

public class OperationMatch {
	
	public WordNode node;
	
	private final List<String> parameterValues = new ArrayList<String>();

	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (node.getOperation() != null) {
			sb.append(node.getOperation());
		}
		if (!parameterValues.isEmpty()) {
			String sep = "(";
			for (String p : parameterValues) {
				sb.append(sep).append(p);
				sep = ",";
			}
			sb.append(")");
		}
		return sb.toString();
	}
	
	public List<String> getParameterValues() {
		return Collections.unmodifiableList(parameterValues);
	}

	public void addParameterValue(String p) {
		parameterValues.add(p);
	}

	public String getParameterValue(int i) {
		return parameterValues.get(i);
	}

	public int getParameterValueCount() {
		return parameterValues.size();
	}
}
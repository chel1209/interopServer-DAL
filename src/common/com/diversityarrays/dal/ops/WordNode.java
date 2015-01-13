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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

public class WordNode {

	static public boolean visitTree(WordNode node, Transformer<WordNode,Boolean> nodeVisitor) {
		if (! nodeVisitor.transform(node)) {
			return false;
		}
		for (WordNode child : node.nodes.values()) {
			if (! visitTree(child, nodeVisitor)) {
				return false;
			}
		}
		return true;
	}
	
	static private final Set<String> NON_ID_NUMERIC_PARAMETER_NAMES = new HashSet<String>();
	static {
		NON_ID_NUMERIC_PARAMETER_NAMES.add("_nperpage");
		NON_ID_NUMERIC_PARAMETER_NAMES.add("_num");
		NON_ID_NUMERIC_PARAMETER_NAMES.add("_random");
	}

	private Map<String,WordNode> nodes = new LinkedHashMap<String,WordNode>();
	private DalOperation operation;
	private Set<String> parameters = new HashSet<String>();
	
	public WordNode getSubNode(String word) {
		WordNode node = nodes.get(word);
		if (node==null) {
			node = new WordNode();
			nodes.put(word, node);
		}
		return node;
	}
	
	public boolean hasParameters() {
		return parameters!=null && ! parameters.isEmpty();
	}
	
	public boolean hasNonNumericParameters() {
		for (String p : parameters) {
			if (! (p.endsWith("id") || NON_ID_NUMERIC_PARAMETER_NAMES.contains(p))) {
				return true;
			}
		}
		return false;
	}

	private String value;
	@Override
	public String toString() {
		if (value==null) {
			StringBuilder sb = new StringBuilder();
			String sep = "";
			for (String k : nodes.keySet()) {
				sb.append(sep).append(k);
				sep = ",";
			}
			sb.append(" OP:").append(operation);
			
			value = sb.toString();
		}
		return value;
	}


	public void addParameter(String p) {
		parameters.add(p);
	}

	public void setOperation(DalOperation op) {
		if (operation!=null) {
			System.err.println("?Double operation: "+operation+" <> "+op);
		}
		this.operation = op;
	}
	
	public DalOperation getOperation() {
		return operation;
	}

	public WordNode lookup(String word) {
		return nodes.get(word);
	}

}
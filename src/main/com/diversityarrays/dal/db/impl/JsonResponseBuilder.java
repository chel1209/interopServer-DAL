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
package com.diversityarrays.dal.db.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import net.pearcan.json.JsonMap;


import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.server.DalServer;
import com.diversityarrays.dalclient.DALClient;


import fi.iki.elonen.NanoHTTPD.Response;

public class JsonResponseBuilder extends DalResponseBuilder {

	Stack<JsonMap> stack = new Stack<JsonMap>();
	JsonMap tos = null;
	String error = null;
	
	public JsonResponseBuilder() {
		tos = new JsonMap(true);
		stack.push(tos);
	}
	
	@Override
	public DalResponseBuilder startTag(String tag) {
		if (error==null) {
			JsonMap map = new JsonMap(true);
			tos.addToList(tag, map);
			stack.push(map);
			tos = map;
		}
		return this;
	}
	

	@Override
	public DalResponseBuilder attribute(String attrName, String attrValue) {
		tos.put(attrName, attrValue);
		return this;
	}

	@Override
	public DalResponseBuilder endTag(String tag) {
		if (error==null) {
			if (stack.isEmpty()) {
				error = "Invalid attempt to endTag("+tag+")";
			}
			else {
				stack.pop();
				tos = stack.firstElement();
				if (tag!=null && ! tag.isEmpty()) {
					List<String> keys = tos.getKeysInOrder();
					if (keys.isEmpty()) {
						error = JsonResponseBuilder.class.getName()+".endTag("+tag+"): Internal Error 1";
					}
					else if (! tag.equals(keys.get(keys.size()-1))) {
						error = JsonResponseBuilder.class.getName()+".endTag("+tag+"): Internal Error 2";
					}
				}
			}
		}
		return this;
	}
	
	@Override
	public String asString() {
		if (error==null) {
			int ssize = stack.size();
			if (ssize<1) {
				error = "too many endTag() calls";
			}
			else if (ssize>1) {
				error = "not enough endTag() calls";
			}
		}

		String result;
		JsonMap out;
		if (error==null) {
			out = stack.get(0);
			for (String meta : getResponseMetaTags()) {
				JsonMap map = new JsonMap();
				map.put(DALClient.ATTR_TAG_NAME, meta);
				out.addToList(DALClient.TAG_RECORD_META, map);
			}
		}
		else {
			JsonMap top = new JsonMap();
			JsonMap err = new JsonMap();
			top.addToList(DALClient.TAG_ERROR, err);
			err.put(DALClient.ATTR_MESSAGE, error);
			
			out = top;
		}

		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		try {
			out.write(ps);
			ps.close();
			result = baos.toString();
		} catch (IOException e) {
			StringBuilder sb = new StringBuilder("{");
			sb.append(DALClient.TAG_ERROR).append(" [ { ")
				.append(DALClient.ATTR_MESSAGE).append(": \"").append(e.getMessage().replaceAll("\"", "\\\""))
				.append(" } ]\n}");
			result = sb.toString();
		}

		return result;

	}

	@Override
	public Response build(Response.IStatus status) {
		if (error==null) {
			int ssize = stack.size();
			if (ssize<1) {
				error = "too many endTag() calls";
			}
			else if (ssize>1) {
				error = "not enough endTag() calls";
			}
		}

		JsonMap out;
		Response result;
		if (error==null) {
			out = stack.get(0);
			for (String meta : getResponseMetaTags()) {
				JsonMap map = new JsonMap();
				map.put(DALClient.ATTR_TAG_NAME, meta);
				out.addToList(DALClient.TAG_RECORD_META, map);
			}
		}
		else {
			JsonMap top = new JsonMap();
			JsonMap err = new JsonMap();
			top.addToList(DALClient.TAG_ERROR, err);
			err.put(DALClient.ATTR_MESSAGE, error);
			
			out = top;
		}

		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		try {
			out.write(ps);
			ps.close();
			result = new Response(status, DalServer.MIME_JSON, baos.toString());
		} catch (IOException e) {
			StringBuilder sb = new StringBuilder("{");
			sb.append(DALClient.TAG_ERROR).append(" [ { ")
				.append(DALClient.ATTR_MESSAGE).append(": \"").append(e.getMessage().replaceAll("\"", "\\\""))
				.append(" } ]\n}");
			result = new Response(ERROR_STATUS, DalServer.MIME_JSON, sb.toString());
		}

		return result;
	}
}
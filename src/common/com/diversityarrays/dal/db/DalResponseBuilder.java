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

import java.util.ArrayList;
import java.util.List;

import fi.iki.elonen.NanoHTTPD.Response;

/**
 * Common code for the XmlResponseBuilder and JsonResponseBuilder.
 * @author brian
 *
 */
public abstract class DalResponseBuilder {
	
	static public final Response.IStatus ERROR_STATUS = new Response.IStatus() {
		
		@Override
		public int getRequestStatus() {
			return 420;
		}
		
		@Override
		public String getDescription() {
			return "Method Failure";
		}
	};
	
	private List<String> responseMetaTags = new ArrayList<String>();

	public DalResponseBuilder addResponseMeta(String tag) {
		responseMetaTags.add(tag);
		return this;
	}
	
	protected Iterable<String> getResponseMetaTags() {
		return responseMetaTags;
	}

	abstract public String asString();

	abstract public DalResponseBuilder startTag(String tag);
	
	abstract public DalResponseBuilder attribute(String attrName, String attrValue);

	public DalResponseBuilder endTag() {
		return endTag(null);
	}

	
	abstract public DalResponseBuilder endTag(String tag);
	
	abstract public Response build(Response.IStatus status);

}
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

import java.io.StringWriter;

import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dalclient.DALClient;
import com.generationjava.io.WritingException;
import com.generationjava.io.xml.XmlWriter;

import fi.iki.elonen.NanoHTTPD.Response;

public class XmlResponseBuilder extends DalResponseBuilder {
	
	static public final String MIME_TEXT_XML = "text/xml";
	static public final String MIME_APPLICATION_XML = "application/xml";
	
	static final String TAG_DATA = "DATA";
	static final String XML_DATA_BEGIN = "<"+TAG_DATA+">";
	static final String XML_DATA_END = "</"+TAG_DATA+">";
	static final String XML_HEADER = "<?xml version='1.0' encoding='UTF-8'?>\n";
	
	StringWriter sw = new StringWriter();
	XmlWriter w = new XmlWriter(sw);
	WritingException error = null;

	public XmlResponseBuilder() {
	}
	
	@Override
	public DalResponseBuilder startTag(String tag) {
		if (error==null) {
			try {
				w.startEntity(tag);
			} catch (WritingException e) {
				error = e;
			}
		}
		return this;
	}
	
	@Override
	public DalResponseBuilder attribute(String attrName, String attrValue) {
		if (error==null) {
			try {
				w.writeAttribute(attrName, attrValue);
			} catch (WritingException e) {
				error = e;
			}
		}
		return this;
	}

	@Override
	public DalResponseBuilder endTag(String tag) {
		if (error==null) {
			try {
				w.endEntity(tag);
			} catch (WritingException e) {
				error = e;
			}
		}
		return this;
	}
	
	@Override
	public String asString() {
		String result;
		if (error==null) {
			try {
				w.close();
			} catch (WritingException e) {
				error = e;
			}
		}
		
		if (error==null) {
			StringBuilder sb = new StringBuilder(XML_HEADER);
			sb.append(XML_DATA_BEGIN);
			for (String meta : getResponseMetaTags()) {
				sb.append("<").append(DALClient.TAG_RECORD_META).append(' ')
				.append(DALClient.ATTR_TAG_NAME).append("=\"").append(meta).append("\" />\n");
			}
			sb.append(sw.toString());
			sb.append(XML_DATA_END);
			result = sb.toString();
		}
		else {
			result = XmlWriter.escapeXml(error.getMessage());
		}
		return result;

	}
	
	@Override
	public Response build(Response.IStatus status) {
		Response result;
		if (error==null) {
			try {
				w.close();
			} catch (WritingException e) {
				error = e;
			}
		}
		
		if (error==null) {
			StringBuilder sb = new StringBuilder(XML_HEADER);
			sb.append(XML_DATA_BEGIN);
			for (String meta : getResponseMetaTags()) {
				sb.append("<").append(DALClient.TAG_RECORD_META).append(' ')
				.append(DALClient.ATTR_TAG_NAME).append("=\"").append(meta).append("\" />\n");
			}

			sb.append(sw.toString());
			sb.append(XML_DATA_END);
			result = new Response(status, MIME_TEXT_XML, sb.toString());
		}
		else {
			error.printStackTrace();
			result = new Response(status, MIME_TEXT_XML, XmlWriter.escapeXml(error.getMessage()));
		}
		return result;
	}
	
}
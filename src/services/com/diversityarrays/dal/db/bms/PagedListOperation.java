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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.diversityarrays.dal.db.DalDatabaseUtil;
import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.DalResponseBuilder;
import com.diversityarrays.dal.db.EntityIterator;
import com.diversityarrays.dal.db.EntityOperation;
import com.diversityarrays.dal.db.EntityProvider;
import com.diversityarrays.dal.db.RecordCountCacheEntry;
import com.diversityarrays.dal.entity.DalEntity;
import com.diversityarrays.dal.entity.Page;
import com.diversityarrays.dal.server.DalSession;
import com.diversityarrays.dalclient.DALClient;

import fi.iki.elonen.NanoHTTPD.Method;

class PagedListOperation<T extends DalEntity> extends EntityOperation<T,BMS_DalDatabase> {

	public static final Pattern PATTERN = Pattern.compile("^list/([a-z]+)/_nperpage/page/_num$");
	
	public static String getEntityName(Matcher m) {
		return m.group(1);
	}

	public PagedListOperation(BMS_DalDatabase db, String entityName,
			Class<? extends T> tclass, EntityProvider<T> provider) 
	{
		super(db, entityName, "list/" + entityName + "/_nperpage/page/_num", tclass, provider);
	}
	
	@Override
	public void execute(DalSession session, DalResponseBuilder responseBuilder,
			Method method, String uri, List<String> dalOpParameters,
			Map<String, String> methodParms,
			Map<String, String> filePathByName)
	throws DalDbException {

		String filterClause = DalDatabaseUtil.getFilteringClause(methodParms);
		
		Page bmsPage = null;
		
		int nRecords;
		
		//RHT/Error1
		/*RecordCountCacheEntry cacheEntry = context.getRecordCountCacheEntry(session, entityClass);
		if (cacheEntry!=null && cacheEntry.isFor(filterClause)) {
			nRecords = cacheEntry.count;
			System.err.println(session.getUserId()+":"+entityClass.getName()+"."+filterClause+": cached value=" + nRecords);
		}
		else {*/
			bmsPage = entityProvider.getEntityCountPage(filterClause);
			context.setRecordCountCacheEntry(session, entityClass, filterClause, bmsPage.getTotalResults());

			System.err.println(session.getUserId()+":"+entityClass.getName()+"."+filterClause+": CACHING value=" + bmsPage.getTotalResults());
		//}

		int nPerPage = getIntParameter(0, dalOpParameters, "_nperpage", 1);
		int numOfPages = (bmsPage.getTotalResults() + nPerPage - 1) / nPerPage;
		int pageNum  = getIntParameter(1, dalOpParameters, "_num",      1);
		int firstRecord = (pageNum - 1) * nPerPage;
		EntityIterator<? extends T> iter = null;
		
		responseBuilder.addResponseMeta(entityTagName);
		responseBuilder.addResponseMeta(DALClient.TAG_PAGINATION);
		
		responseBuilder.startTag(DALClient.TAG_PAGINATION)
			.attribute(DALClient.ATTR_PAGE, Integer.toString(pageNum))
			.attribute(DALClient.ATTR_NUM_OF_RECORDS, Integer.toString(bmsPage.getTotalResults()))
			.attribute(DALClient.ATTR_NUM_OF_PAGES, Integer.toString(numOfPages))
			.attribute(DALClient.ATTR_NUM_PER_PAGE, Integer.toString(nPerPage))
			.endTag();
		
		if(nPerPage>BMSApiDataConnection.BMS_MAX_PAGE_SIZE){
			int processed = 0;
			boolean pending = false;
			bmsPage.setTotalPages(bmsPage.getTotalResults()/BMSApiDataConnection.BMS_MAX_PAGE_SIZE);
			if(pageNum > bmsPage.getTotalPages()){
				throw new DalDbException(BMSApiDataConnection.PAGE_DOES_NOT_EXIST);
			}
			pageNum = firstRecord/BMSApiDataConnection.BMS_MAX_PAGE_SIZE;
			bmsPage.setPageNumber(pageNum==0?1:pageNum);
			while(processed<nPerPage){
				if(pending==true){
					bmsPage.setPageNumber(bmsPage.getPageNumber()+1);
					if(nPerPage - processed < BMSApiDataConnection.BMS_MAX_PAGE_SIZE){
						iter = entityProvider.createIterator(firstRecord, nPerPage - processed, filterClause,bmsPage);
					}else{
						iter = entityProvider.createIterator(firstRecord, BMSApiDataConnection.BMS_MAX_PAGE_SIZE, filterClause,bmsPage);
					}
				}else{
					iter = entityProvider.createIterator(firstRecord, BMSApiDataConnection.BMS_MAX_PAGE_SIZE, filterClause,bmsPage);
				}
				try {
					T entity;
					iter.readLine();
					while (null != (entity = iter.nextEntity())) {
						appendEntity(responseBuilder, entity);
						processed++;
					}
					if(processed < nPerPage){
						pending = true;
					}
				}
				finally {
					try { iter.close(); }
					catch (IOException ignore) { }
				}
			}
		}else{
			iter = entityProvider.createIterator(firstRecord, nPerPage, filterClause,bmsPage);
			
			try {
				T entity;
				iter.readLine();
				
				while (null != (entity = iter.nextEntity())) {
					appendEntity(responseBuilder, entity);
					//iter.readLine();
				}
			}
			finally {
				try { iter.close(); }
				catch (IOException ignore) { }
			}
		}
	}

	private int getIntParameter(int pIndex, List<String> dalOpParameters, String paramName, int minValue) throws DalDbException {
		try {
			
			String paramValue = dalOpParameters==null ? null : dalOpParameters.get(pIndex);
			if (paramValue == null) {
				throw new DalDbException("Missing value for Parameter#" + pIndex + " (" + paramName + ")");
			}

			int result = Integer.parseInt(paramValue);
			if (result < minValue) {
				throw new DalDbException("Parameter#" + pIndex + " (" + paramName + ")  = '" + result + "' minimum is " + minValue);
			}
			return result;
		} catch (IndexOutOfBoundsException e) {
			throw new DalDbException("Missing or Parameter#" + pIndex + " (" + paramName + ")");
		} catch (NumberFormatException e) {
			throw new DalDbException("Missing or invalid Parameter#" + pIndex + " (" + paramName + ")");
		}
	}
	
}
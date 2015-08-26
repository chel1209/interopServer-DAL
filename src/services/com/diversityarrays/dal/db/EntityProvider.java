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

import com.diversityarrays.dal.entity.DalEntity;


public interface EntityProvider<T extends DalEntity> {
	/**
	 * Return the number of records satisfying the filterClause.
	 * @param filterClause may be null
	 * @return int
	 * @throws DalDbException 
	 */
	public int getEntityCount(String filterClause) throws DalDbException;
	
	/**
	 * 
	 * @param id
	 * @param filterClause may be null
	 * @return the required record or null
	 * @throws DalDbException 
	 */
	public T getEntity(String id, String filterClause) throws DalDbException;

	/**
	 * @param firstRecord zero means all records
	 * @param nRecords must be non-zero if firstRecord &gt; 0
	 * @param filterClause may be null
	 * @return Iterator over the required records
	 * @throws DalDbException 
	 */
	public EntityIterator<? extends T> createIdIterator(String id, int firstRecord, int nRecords, String filterClause) throws DalDbException;
	
	/**
	 * @param firstRecord zero means all records
	 * @param nRecords must be non-zero if firstRecord &gt; 0
	 * @param filterClause may be null
	 * @return Iterator over the required records
	 * @throws DalDbException 
	 */
	public EntityIterator<? extends T> createIterator(int firstRecord, int nRecords, String filterClause) throws DalDbException;
	
	/**
	 * Prepares an extra search in case the mapping of the basic information on a couple of entities doesn't map one-to-one.	
	 */
	public void prepareDetailsSearch() throws DalDbException;
	
	/**
	 * Executes an extra search in case the mapping of the basic information on a couple of entities doesn't map one-to-one
	 * @param Entity to which the information will be added.	
	 */
	public void getDetails(DalEntity entity) throws DalDbException;
	
	public void getFullDetails(DalEntity entity) throws DalDbException;
}
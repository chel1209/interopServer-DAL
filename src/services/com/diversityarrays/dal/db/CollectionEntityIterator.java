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

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.diversityarrays.dal.entity.DalEntity;

public class CollectionEntityIterator<T extends DalEntity> implements EntityIterator<T> {
	
	private final Iterator<T> iterator;

	public CollectionEntityIterator(Collection<T> coll) {
		this.iterator = coll.iterator();
	}

	@Override
	public void close() throws IOException {
		// Do nothing
	}

	@Override
	public T nextEntity() {
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}

	@Override
	public void readLine() throws DalDbException {
		throw new DalDbException(new UnsupportedOperationException());
		
	}
}
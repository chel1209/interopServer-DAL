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
package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="genus")
@EntityTag("Genus")
public class Genus extends DalEntity {

	// TODO add column descriptions from the schema
	@Id
    @Column(name="GenusId", nullable=false)
    private Integer genusId;

    @Column(name="GenusName", nullable=false, length=(32))
    private String genusName;
    
    public Genus() {}
    
    public String toString() {
    	return genusName;
    }

    public Integer getGenusId() {
		return genusId;
	}

	public void setGenusId(Integer genusId) {
		this.genusId = genusId;
	}

	public String getGenusName() {
		return genusName;
	}

	public void setGenusName(String genusName) {
		this.genusName = genusName;
	}

	static public final EntityColumn ID   = createEntityColumn(Genus.class, "genusId");
    
    static public final EntityColumn NAME = createEntityColumn(Genus.class, "genusName");
    
	static public EntityColumn[] entityColumns() {
		return getEntityColumns(Genus.class);
	}
}

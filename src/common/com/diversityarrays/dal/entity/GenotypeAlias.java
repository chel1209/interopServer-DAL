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

import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;


/**
* <b>Region: Genotypes and Specimens</b>
* <p>
* One genotype may have many historical names (aliases) under
* which it has been known.
*/

@Table(name="genotypealias")
@EntityTag("GenotypeAlias")
public class GenotypeAlias extends DalEntity {

	// TODO add column descriptions from the schema
	@Id
	@Column(name="GenotypeAliasId", nullable=false)
	private Integer genotypeAliasId;

	@Column(name="GenotypeAliasName", nullable=false, length=(255))
	private String genotypeAliasName;

	@JoinTable(name="genotype",
         joinColumns=@JoinColumn(name="GenotypeId", referencedColumnName="GenotypeId")
     )
	@Column(name="GenotypeId", nullable=false)
	private Integer genotypeId;

	// TODO implement this for "list/type/_class/_status
	@TypeId(classValue="genotypealias")
	@Column(name="GenotypeAliasType", nullable=true)
	private Integer genotypeAliasType;
	
	// TODO implement this for "list/type/_class/_status
	@TypeId(classValue="genotypealiasstatus")
	@Column(name="GenotypeAliasStatus", nullable=true)
	private Integer genotypeAliasStatus;
	
	@Column(name="GenotypeAliasLang", nullable=true, length=(45))
	private String genotypeAliasLang;

	public GenotypeAlias() {
		super();
	}

	@Override
	public String toString() {
		return genotypeAliasName;
	}

	public Integer getGenotypeAliasId() {
		return genotypeAliasId;
	}

	public void setGenotypeAliasId(Integer v) {
		this.genotypeAliasId = v;
	}

	public String getGenotypeAliasName() {
		return genotypeAliasName;
	}

	public void setGenotypeAliasName(String v) {
		this.genotypeAliasName = v;
	}

	public Integer getGenotypeId() {
		return genotypeId;
	}

	public void setGenotypeId(Integer v) {
		this.genotypeId = v;
	}
	
	public Integer getGenotypeAliasType() {
		return genotypeAliasType;
	}

	public void setGenotypeAliasType(Integer genotypeAliasType) {
		this.genotypeAliasType = genotypeAliasType;
	}
	
	public Integer getGenotypeAliasStatus() {
		return genotypeAliasStatus;
	}

	public void setGenotypeAliasStatus(Integer genotypeAliasStatus) {
		this.genotypeAliasStatus = genotypeAliasStatus;
	}

	public String getGenotypeAliasLang() {
		return genotypeAliasLang;
	}

	public void setGenotypeAliasLang(String genotypeAliasLang) {
		this.genotypeAliasLang = genotypeAliasLang;
	}

	static public final EntityColumn GENOTYPE_ALIAS_ID = createEntityColumn(GenotypeAlias.class, "genotypeAliasId");
	static public final EntityColumn GENOTYPE_ALIAS_NAME = createEntityColumn(GenotypeAlias.class, "genotypeAliasName");
	static public final EntityColumn GENOTYPE_ID = createEntityColumn(GenotypeAlias.class, "genotypeId");
	static public final EntityColumn GENOTYPE_ALIAS_TYPE = createEntityColumn(GenotypeAlias.class, "genotypeAliasType");
	static public final EntityColumn GENOTYPE_ALIAS_STATUS = createEntityColumn(GenotypeAlias.class, "genotypeAliasStatus");
	static public final EntityColumn GENOTYPE_ALIAS_LANG = createEntityColumn(GenotypeAlias.class, "genotypeAliasLang");
}

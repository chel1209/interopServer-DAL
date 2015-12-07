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
import java.sql.ResultSet;
import java.sql.SQLException;

import net.pearcan.json.JsonMap;
import net.pearcan.util.StringTemplate;

import com.diversityarrays.dal.db.DalDbException;
import com.diversityarrays.dal.db.SqlEntityFactory;
import com.diversityarrays.dal.entity.Genotype;
import com.diversityarrays.dal.service.DalDbNotYetImplementedException;
import com.diversityarrays.dalclient.Permission;

public class GenotypeFactory implements SqlEntityFactory<Genotype> {
	
	private boolean pending;
	
//	private static final ColumnNameMapping COLUMN_NAME_MAPPING;
//
//	static {
//		COLUMN_NAME_MAPPING = new ColumnNameMapping(Genotype.class)
//			.addColumn(Genotype.GENOTYPE_ID, "gid")
////			.addColumn(Genotype.GENOTYPE_ACRONYM, "") // NOT available in BMS
////			.addColumn(Genotype.ORIGIN_ID, "")        // NOT available in BMS
////			.addColumn(Genotype.CAN_PUBLISH_GENOTYPE, "") // NOT available in BMS (always true)
////			.addColumn(Genotype.GENOTYPE_COLOR, "")       // NOT available in BMS
////			.addColumn(Genotype.GENOTYPE_NOTE, "")        // NOT available in BMS
//			// specials
//			.addColumn(Genotype.GENOTYPE_NAME, "*SPECIAL*") // constructed: "Germplasm GID "+gid
//			.addColumn(Genotype.GENUS_ID, "*SPECIAL*") // via Genus record
//			.addColumn(Genotype.GENUS_NAME , "") // via Genus record
//			.addColumn(Genotype.SPECIES_NAME, "*SPECIAL*") // via Genus record
//			;
//	}
	
	private GenusStore genusStore;
	
	GenotypeFactory(GenusStore genusStore) {
		this.genusStore = genusStore;
	}

	@Override
	public String createCountQuery(String filterClause) throws DalDbNotYetImplementedException {
		String sql = createBaseQuery("g", "a", genusStore.getFldnoForGenus(), filterClause).toString();
		return sql;
	}
	
	static public StringBuilder createBaseQuery(String germplsmAlias, String atributsAlias, int fldnoForGenus, String whereClause) {
		return createBaseQuery2(germplsmAlias, atributsAlias, fldnoForGenus, whereClause);
	}

	@SuppressWarnings("unused")
	static private StringBuilder createBaseQuery1(String germplsmAlias, String atributsAlias, int fldnoForGenus) {
		String sql = StringTemplate.buildString(
				"SELECT ${germplsmAlias}.gid AS \"GenotypeId\""
				+", CONCAT('Germplasm GID ',CAST(g.gid AS CHAR)) AS \"GenotypeName\""
				+", CASE WHEN INSTR(a.aval, ' ')>0 THEN LEFT(a.aval,INSTR(a.aval,' ')) ELSE a.aval END AS \"GenusName\""
				+", CASE WHEN INSTR(a.aval, ' ')>0 THEN LTRIM(SUBSTR(a.aval,INSTR(a.aval,' ')+1)) ELSE null END AS \"SpeciesName\""
				+" FROM germplsm AS ${germplsmAlias}"
				+" LEFT JOIN atributs AS ${atributsAlias}"
				+" ON ${atributsAlias}.gid=${germplsmAlias}.gid AND ${atributsAlias}.atype=${fldnoForGenus}")
			.replace("germplsmAlias", "g")
			.replace("atributsAlias", "a")
			.replace("fldnoForGenus", fldnoForGenus)
			.build();
	
		return new StringBuilder(sql);
	}
	
	@SuppressWarnings("unused")
	static private StringBuilder createBaseQuery0(String germplsmAlias, String atributsAlias, int fldnoForGenus) {
		
		String sql = StringTemplate.buildString(
				"SELECT ${germplsmAlias}.*, ${atributsAlias}.aval AS atributsAval"
				+" FROM germplsm AS ${germplsmAlias}"
				+" LEFT JOIN atributs AS ${atributsAlias}"
				+" ON ${atributsAlias}.gid=${germplsmAlias}.gid AND ${atributsAlias}.atype=${fldnoForGenus}")
			.replace("germplsmAlias", "g")
			.replace("atributsAlias", "a")
			.replace("fldnoForGenus", fldnoForGenus)
			.build();
	
		return new StringBuilder(sql);
	}

	@Override
	public String createGetQuery(String id, String filterClause) throws DalDbNotYetImplementedException {
		
		StringBuilder whereBuilder = new StringBuilder("g.gid=");
		whereBuilder.append(id);
		if (filterClause != null) {
			whereBuilder.append(" AND (").append(filterClause).append(")");
		}
		
		String whereClause = whereBuilder.toString();
		
		StringBuilder sb = createBaseQuery("g", "a", genusStore.getFldnoForGenus(), whereClause);

		return sb.toString();
	}
	
	@Override
	public String createPagedListQuery(int firstRecord, int nRecords, String filterClause) throws DalDbNotYetImplementedException {
	
		String clause = buildWhereAndLimit(filterClause, nRecords, firstRecord);
		
		StringBuilder sb = createBaseQuery("g", "a", genusStore.getFldnoForGenus(), clause);

		return sb.toString();
	}
	
	@Override
	public Genotype createEntity(ResultSet rs) throws DalDbException {
		return createEntityImpl(rs);
//		return createEntityImpl_0(rs);
	}
	
	private Genotype createEntityImpl(ResultSet rs) throws DalDbException {
		Genotype genotype = new Genotype();
		
		try {
			int gid = rs.getInt("GenotypeId");
			genotype.setGenotypeId(gid);
			genotype.setGenotypeName(rs.getString("GenotypeName"));
			
			String genusName = rs.getString("GenusName");
			genotype.setGenusName(genusName);

			String speciesName = rs.getString("SpeciesName");
			genotype.setSpeciesName(speciesName);	

			// createBaseQuery1
//			BMS_Genus genus = genusStore.getGenusByName(genusName, speciesName);
//			genotype.setGenusId(genus.getGenusId());
				
			// createBaseQuery
			genotype.setGenusId(rs.getInt("GenusId"));
			
			genotype.setGenotypeAcronym(null);
			genotype.setOriginId(null);
			genotype.setCanPublishGenotype(true);
			genotype.setGenotypeColor(null);
			genotype.setGenotypeNote(null);
			
			genotype.setOwnGroupId(0);
			genotype.setAccessGroupId(0);
			
			genotype.setOwnGroupPerm(Permission.READ.onebit);
			genotype.setAccessGroupPerm(Permission.READ.onebit);
			genotype.setOtherPerm(Permission.READ.onebit);
			
			
		} catch (SQLException e) {
			throw new DalDbException(e);
		}
		
		return genotype;
	}
	
//	@SuppressWarnings("unused")
//	private Genotype createEntityImpl_0(ResultSet rs) throws DalDbException {
//		Genotype genotype = new Genotype();
//		
//		try {
//			int gid = rs.getInt("gid");
//			genotype.setGenotypeId(gid);
//			genotype.setGenotypeName("Germplasm GID " + gid);
//			
//			String[] genusAndSpecies = GenusStore.extractGenusAndSpecies(rs.getString("atributsAval"));
//			BMS_Genus genus = genusStore.getGenusByName(genusAndSpecies[0], genusAndSpecies[1]);
//			
//			genotype.setGenusId(genus.getGenusId());
//			genotype.setGenusName(genus.getGenusName());
//			genotype.setSpeciesName(genus.getSpeciesName());	
//				
//			genotype.setGenotypeAcronym(null);
//			genotype.setOriginId(null);
//			genotype.setCanPublishGenotype(true);
//			genotype.setGenotypeColor(null);
//			genotype.setGenotypeNote(null);
//			
//			genotype.setOwnGroupId(0);
//			genotype.setAccessGroupId(0);
//			
//			genotype.setOwnGroupPerm(Permission.READ.onebit);
//			genotype.setAccessGroupPerm(Permission.READ.onebit);
//			genotype.setOtherPerm(Permission.READ.onebit);
//			
//			
//		} catch (SQLException e) {
//			throw new DalDbException(e);
//		}
//		
//		return genotype;
//	}

	@Override
	public void close() throws IOException {
	}


	static public StringBuilder createBaseQuery2(String germplsmAlias, String atributsAlias, int fldnoForGenus, String constraint) {
		
		StringBuilder wc = new StringBuilder();
		if (constraint != null) {
			if (! constraint.trim().toUpperCase().startsWith("WHERE ")) {
				wc.append(" HAVING ");
			}
			else {
				wc.append(' ');
			}
			wc.append(constraint);
		}
		String whereClause = wc.toString();

		String sql = StringTemplate.buildString(
				"SELECT GenotypeId, GenotypeName, genus.GenusId, genus.GenusName, SpeciesName"
				+ " FROM"
				+ " (SELECT ${germplsmAlias}.gid AS GenotypeId"
				+ ", CONCAT('Germplasm GID ',CAST(${germplsmAlias}.gid AS CHAR)) AS GenotypeName"
				+ ", CASE WHEN INSTR(${atributsAlias}.aval, ' ')>0 THEN UPPER(LTRIM(LEFT(${atributsAlias}.aval,INSTR(${atributsAlias}.aval,' '))))"
				+ "  ELSE UPPER(LTRIM(${atributsAlias}.aval))"
				+ "  END AS GenusName"
				+ ", CASE WHEN INSTR(${atributsAlias}.aval, ' ')>0 THEN UPPER(LTRIM(SUBSTR(${atributsAlias}.aval,INSTR(${atributsAlias}.aval,' ')+1)))"
				+ "  ELSE null"
				+ "  END AS SpeciesName"
				+ " FROM germplsm AS ${germplsmAlias}"
				+ " LEFT JOIN atributs AS ${atributsAlias}"
				+ " ON ${atributsAlias}.gid=${germplsmAlias}.gid AND ${atributsAlias}.atype=${fldnoForGenus}"
				+ " ${whereClause}"
				+ ") AS X"
				+ " LEFT JOIN genus ON X.GenusName=genus.GenusName")
			.replace("germplsmAlias", "g")
			.replace("atributsAlias", "a")
			.replace("fldnoForGenus", fldnoForGenus)
			.replace("whereClause", whereClause)
			.build();
	
		return new StringBuilder(sql);
	}

	public static String buildWhereAndLimit(String filterClause, int nRecords, int firstRecord) {
		
		StringBuilder whereAndLimit = new StringBuilder();
		if (filterClause != null) {
			whereAndLimit.append(filterClause);
		}
		whereAndLimit.append(" LIMIT ").append(nRecords).append(" OFFSET ").append(firstRecord);

		return whereAndLimit.toString();
	}

	@Override
	public Genotype createEntity(JsonMap jsonMap) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the pending
	 */
	public boolean isPending() {
		return pending;
	}

	/**
	 * @param pending the pending to set
	 */
	public void setPending(boolean pending) {
		this.pending = pending;
	}

	@Override
	public String createPagedListQuery(int firstRecord, int nRecords,
			String filterClause, int pageNumber) throws DalDbException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
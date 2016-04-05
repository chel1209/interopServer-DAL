package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @author Raul Hernandez T.  
 */

@Table(name="Unit")
@EntityTag("Unit")
public class GeneralUnit extends DalEntity{

	@Column(name="UseBylayerattrib")	
	private String useBylayerattrib;
	
	@Column(name="UnitTypeName")
	private String unitTypeName;
	
	@Column(name="UseByTrait")
	private String useByTrait;
	
	@Column(name="UseByTrialEvent")
	private String useByTrialEvent;
	
	@Column(name="UseByItem")
	private String useByItem;
	
	@Column(name="UnitTypeId")
	private String unitTypeId;
	
	@Column(name="update")
	private String update;
	
	@Column(name="UnitName")
	private String unitName;
	
	@Column(name="UnitNote")
	private String unitNote;
	
	@Column(name="UnitSource")
	private String unitSource;
	
	@Column(name="UnitId")
	private String unitId;

	public String getUseBylayerattrib() {
		return useBylayerattrib;
	}

	public void setUseBylayerattrib(String useBylayerattrib) {
		this.useBylayerattrib = useBylayerattrib;
	}

	public String getUnitTypeName() {
		return unitTypeName;
	}

	public void setUnitTypeName(String unitTypeName) {
		this.unitTypeName = unitTypeName;
	}

	public String getUseByTrait() {
		return useByTrait;
	}

	public void setUseByTrait(String useByTrait) {
		this.useByTrait = useByTrait;
	}

	public String getUseByTrialEvent() {
		return useByTrialEvent;
	}

	public void setUseByTrialEvent(String useByTrialEvent) {
		this.useByTrialEvent = useByTrialEvent;
	}

	public String getUseByItem() {
		return useByItem;
	}

	public void setUseByItem(String useByItem) {
		this.useByItem = useByItem;
	}

	public String getUnitTypeId() {
		return unitTypeId;
	}

	public void setUnitTypeId(String unitTypeId) {
		this.unitTypeId = unitTypeId;
	}

	public String getUpdate() {
		return update;
	}

	public void setUpdate(String update) {
		this.update = update;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public String getUnitNote() {
		return unitNote;
	}

	public void setUnitNote(String unitNote) {
		this.unitNote = unitNote;
	}

	public String getUnitSource() {
		return unitSource;
	}

	public void setUnitSource(String unitSource) {
		this.unitSource = unitSource;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}
}

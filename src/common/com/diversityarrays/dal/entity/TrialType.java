package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Table;


/*
 * @author Raul Hernandez T.
 * @date   14/DIC/2015
 * */

@Table(name="trialtype")
@EntityTag("TrialType")
public class TrialType extends DalEntity {
	

	@Column(name="IsTypeActive")
	private Integer isTypeActive;
	
	@Column(name="typeId")
	private Integer typeId;
	
	@Column(name="typeName")
	private String  typeName;
	
	@Column(name="typeName")
	private String typeNote;

	public Integer getIsTypeActive() {
		return isTypeActive;
	}

	public void setIsTypeActive(Integer isTypeActive) {
		this.isTypeActive = isTypeActive;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeNote() {
		return typeNote;
	}

	public void setTypeNote(String typeNote) {
		this.typeNote = typeNote;
	}

}

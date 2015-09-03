package com.diversityarrays.dal.entity;

/*
 * author: Raul Hernandez T.
 * date:   08-31-2015
 */


public class Trait {
     
	private Integer traitId;
	private String  traitName;
	
	public Trait(){}
	
	public Trait(Integer traitId, String traitName) {
		this.traitId = traitId;
		this.traitName = traitName;
	}

	public Integer getTraitId() {
		return traitId;
	}
	public void setTraitId(Integer traitId) {
		this.traitId = traitId;
	}
	public String getTraitName() {
		return traitName;
	}
	public void setTraitName(String traitName) {
		this.traitName = traitName;
	}
	
	
}

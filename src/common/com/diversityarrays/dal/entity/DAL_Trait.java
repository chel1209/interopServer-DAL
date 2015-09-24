package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Id;

public class DAL_Trait extends DalEntity {
	
	static public final EntityColumn TRAIT_ID = createEntityColumn(DAL_Trait.class, "traitId");
	static public final EntityColumn TRAIT_NAME = createEntityColumn(DAL_Trait.class, "traitName");
	static public final EntityColumn CAPTION = createEntityColumn(DAL_Trait.class, "caption");
	static public final EntityColumn DESCRIPTION = createEntityColumn(DAL_Trait.class, "description");
	static public final EntityColumn DATA_TYPE = createEntityColumn(DAL_Trait.class, "dataType");
	static public final EntityColumn MAX_LENGTH = createEntityColumn(DAL_Trait.class, "maxLength");
	static public final EntityColumn VALIDATION_RULE = createEntityColumn(DAL_Trait.class, "validationRule");
	
	public DAL_Trait() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public String toString(){
		return "{ TraitId: \""+ traitId + "\", TraitName : \"" + traitName + "\" } "; 
	}

	@Id
	@Column(name="TraitId", length=(6000)) 
	private Integer traitId;
	
	@Column(name="TraitName", length=(6000))
	private String traitName;
	
	@Column(name="Caption", length=(6000))
	private String caption;
	
	@Column(name="Description", length=(6000))
	private String description;
	
	@Column(name="DataType", length=(6000))
	private String dataType;
	
	@Column(name="MaxLength", length=(6000))
	private String maxLength;
	
	@Column(name="ValidationRule", length=(6000))
	private String validationRule;

	/**
	 * @return the traitId
	 */
	public Integer getTraitId() {
		return traitId;
	}

	/**
	 * @param traitId the traitId to set
	 */
	public void setTraitId(Integer traitId) {
		this.traitId = traitId;
	}

	/**
	 * @return the traitName
	 */
	public String getTraitName() {
		return traitName;
	}

	/**
	 * @param traitName the traitName to set
	 */
	public void setTraitName(String traitName) {
		this.traitName = traitName;
	}

	/**
	 * @return the caption
	 */
	public String getCaption() {
		return caption;
	}

	/**
	 * @param caption the caption to set
	 */
	public void setCaption(String caption) {
		this.caption = caption;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the dataType
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType the dataType to set
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the maxLength
	 */
	public String getMaxLength() {
		return maxLength;
	}

	/**
	 * @param maxLength the maxLength to set
	 */
	public void setMaxLength(String maxLength) {
		this.maxLength = maxLength;
	}

	/**
	 * @return the validationRule
	 */
	public String getValidationRule() {
		return validationRule;
	}

	/**
	 * @param validationRule the validationRule to set
	 */
	public void setValidationRule(String validationRule) {
		this.validationRule = validationRule;
	}

}

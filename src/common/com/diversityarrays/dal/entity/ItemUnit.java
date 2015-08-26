package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Table(name="itemunit")
@EntityTag("ItemUnit")
public class ItemUnit extends DalEntity {
	
	@Id
	@Column(name="ItemUnitID", nullable=false)	
	private Integer itemUnitId;
	
	/*@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="UnitTypeId")
	private GeneralType generalType;*/
	
	@Column(name="ItemUnitName", nullable=false, length=(12))
	private String itemUnitName;
	
	@Column(name="ItemUnitNote", nullable=true, length=(254))
	private String itemUnitNote;
	
	@Column(name="ConversionRule", nullable=true, length=(254))
	private String conversionRule;	
	
	@Column(name="GramsConversionMultiplier", nullable=false)
	private Float gramsConversionMultiplier;
	
	/*
	 * Fields marked as @Transient don't exist in the database, so they are ignored by JPA. 
	 */
	@Transient
	private Integer unitTypeId;
	
	@Transient
	private String unitTypeName;
	
	static public final EntityColumn ITEM_UNIT_ID = createEntityColumn(ItemUnit.class, "itemUnitId");
	static public final EntityColumn ITEM_UNIT_NAME = createEntityColumn(ItemUnit.class, "itemUnitName");
	static public final EntityColumn ITEM_UNIT_NOTE = createEntityColumn(ItemUnit.class, "itemUnitNote");
	//static public final EntityColumn UNIT_TYPE_ID = createEntityColumn(ItemUnit.class, "unitTypeId");
	//static public final EntityColumn UNIT_TYPE_NAME = createEntityColumn(ItemUnit.class, "unitTypeName");
	 
	public ItemUnit(){
		super();
	}

	/**
	 * @return the itemUnitID
	 */
	public Integer getItemUnitId() {
		return itemUnitId;
	}

	/**
	 * @param itemUnitID the itemUnitID to set
	 */
	public void setItemUnitId(Integer itemUnitId) {
		this.itemUnitId = itemUnitId;
	}

	/**
	 * @return the generalType
	 */
	/*/public GeneralType getGeneralType() {
		return generalType;
	}

	/**
	 * @param generalType the generalType to set
	 */
	/*public void setGeneralType(GeneralType generalType) {
		this.generalType = generalType;
	}*/

	/**
	 * @return the itemUnitName
	 */
	public String getItemUnitName() {
		return itemUnitName;
	}

	/**
	 * @param itemUnitName the itemUnitName to set
	 */
	public void setItemUnitName(String itemUnitName) {
		this.itemUnitName = itemUnitName;
	}

	/**
	 * @return the itemUnitNote
	 */
	public String getItemUnitNote() {
		return itemUnitNote;
	}

	/**
	 * @param itemUnitNote the itemUnitNote to set
	 */
	public void setItemUnitNote(String itemUnitNote) {
		this.itemUnitNote = itemUnitNote;
	}

	/**
	 * @return the conversionRule
	 */
	public String getConversionRule() {
		return conversionRule;
	}

	/**
	 * @param conversionRule the conversionRule to set
	 */
	public void setConversionRule(String conversionRule) {
		this.conversionRule = conversionRule;
	}

	/**
	 * @return the gramsConversionMultiplier
	 */
	public Float getGramsConversionMultiplier() {
		return gramsConversionMultiplier;
	}

	/**
	 * @param gramsConversionMultiplier the gramsConversionMultiplier to set
	 */
	public void setGramsConversionMultiplier(Float gramsConversionMultiplier) {
		this.gramsConversionMultiplier = gramsConversionMultiplier;
	}



	/**
	 * @return the unitTypeId
	 */
	@Transient
	public Integer getUnitTypeId() {
		return unitTypeId;
	}

	/**
	 * @param unitTypeId the unitTypeId to set
	 */
	@Transient
	public void setUnitTypeId(Integer unitTypeId) {
		this.unitTypeId = unitTypeId;
	}

	/**
	 * @return the unitTypeName
	 */
	@Transient
	public String getUnitTypeName() {
		return unitTypeName;
	}

	/**
	 * @param unitTypeName the unitTypeName to set
	 */
	@Transient
	public void setUnitTypeName(String unitTypeName) {
		this.unitTypeName = unitTypeName;
	}
	
}
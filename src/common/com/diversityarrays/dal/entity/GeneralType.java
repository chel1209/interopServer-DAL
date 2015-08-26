package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name="generaltype")
@EntityTag("GeneralType")
public class GeneralType extends DalEntity {
	
	@Id
	@Column(name="TypeId", nullable=false)	
	private Integer typeId;
	
	@Column(name="TypeName", nullable=false, length=(100))
	private String typeName;
	
	@Column(name="typeNote", nullable=true, length=(200))
	private String typeNote;
	
	@Column(name="IsTypeActive", nullable=false, length=(1))
	private Boolean isTypeActive = true;
	
	static public final EntityColumn TYPE_ID = createEntityColumn(GeneralType.class, "typeId");
	static public final EntityColumn TYPE_NAME = createEntityColumn(GeneralType.class, "typeName");
	static public final EntityColumn TYPE_NOTE = createEntityColumn(GeneralType.class, "typeNote");
	static public final EntityColumn IS_TYPE_ACTIVE = createEntityColumn(GeneralType.class, "isTypeActive");
	
	public GeneralType(){
		super();
	}
	
	/**
	 * @return the typeId
	 */
	public Integer getTypeId() {
		return typeId;
	}
	/**
	 * @param typeId the typeId to set
	 */
	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}
	/**
	 * @return the typeName
	 */
	public String getTypeName() {
		return typeName;
	}
	/**
	 * @param typeName the typeName to set
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	/**
	 * @return the typeNote
	 */
	public String getTypeNote() {
		return typeNote;
	}
	/**
	 * @param typeNote the typeNote to set
	 */
	public void setTypeNote(String typeNote) {
		this.typeNote = typeNote;
	}
	/**
	 * @return the isTypeActive
	 */
	public Boolean isTypeActive() {
		return isTypeActive;
	}
	/**
	 * @param isTypeActive the isTypeActive to set
	 */
	public void setTypeActive(Boolean isTypeActive) {
		this.isTypeActive = isTypeActive;
	}
	/**
	 * @return the typeId
	 */
	public static EntityColumn getTypeIdColumn() {
		return TYPE_ID;
	}
	/**
	 * @return the typeName
	 */
	public static EntityColumn getTypeNameColumn() {
		return TYPE_NAME;
	}
	/**
	 * @return the typeNote
	 */
	public static EntityColumn getTypeNoteColumn() {
		return TYPE_NOTE;
	}
	/**
	 * @return the isTypeActive
	 */
	public static EntityColumn getIsTypeActive() {
		return IS_TYPE_ACTIVE;
	}
	

	
}

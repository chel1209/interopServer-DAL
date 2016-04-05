package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 
 * @author Raul Hernandez T.
 * @date 3-14-2016
 *
 */

@Table(name="Trait")
@EntityTag("Trait")
public class Traits extends DalEntity{
	
	@Column(name="OtherPermission")
	private String OtherPermission;
	
	@Column(name="TraitUnitName")
	private String traitUnitName;
	
	@Column(name="TraitValRuleErrMsg")
	private String traitValRuleErrMsg;
	
	@Column(name="AccessGroupName")
	private String accessGroupName;
	
	@Column(name="AccessGroupId")
	private String accessGroupId;
	
	@Column(name="TraitGroupTypeId")
	private String traitGroupTypeId;
	
	@Column(name="UltimatePermission")
	private String UltimatePermission;
	
	@Column(name="TraitDescription")
	private String traitDescription;
	
	@Column(name="chgPerm")
	private String chgPerm;
	
	@Column(name="AccessGroupPermission")
	private String accessGroupPermission;
	
	@Column(name="UltimatePerm")
	private String ultimatePerm;
	
	@Column(name="TraitDataType")
	private String traitDataType;
	
	@Column(name="AccessGroupPerm")
	private String accessGroupPerm;
	
	@Column(name="OwnGroupPermission")
	private String ownGroupPermission;
	
	@Column(name="update")
	private String update;
	
	@Column(name="TraitUnit")
	private String traitUnit;
	
	@Column(name="OtherPerm")
	private String otherPerm;
	
	@Column(name="OwnGroupName")
	private String ownGroupName;
	
	@Column(name="OwnGroupPerm")
	private String ownGroupPerm;
	
	@Column(name="TraitId")
	private String traitId;
	
	@Column(name="TraitValueMaxLength")
	private String traitValueMaxLength;
	
	@Column(name="chgOwner")
	private String chgOwner;
	
	@Column(name="TraitCaption")
	private String traitCaption;
	
	@Column(name="TraitValRule")
	private String traitValRule;
	
	@Column(name="addAlias")
	private String addAlias;
	
	@Column(name="TraitName")
	private String traitName;
	
	@Column(name="IsTraitUsedForAnalysis")
	private String isTraitUsedForAnalysis;
	
	@Column(name="OwnGroupId")
	private String ownGroupId;
	
	@Column(name="delete")
	private String delete;

	public String getOtherPermission() {
		return OtherPermission;
	}

	public void setOtherPermission(String otherPermission) {
		OtherPermission = otherPermission;
	}

	public String getTraitUnitName() {
		return traitUnitName;
	}

	public void setTraitUnitName(String traitUnitName) {
		this.traitUnitName = traitUnitName;
	}

	public String getTraitValRuleErrMsg() {
		return traitValRuleErrMsg;
	}

	public void setTraitValRuleErrMsg(String traitValRuleErrMsg) {
		this.traitValRuleErrMsg = traitValRuleErrMsg;
	}

	public String getAccessGroupName() {
		return accessGroupName;
	}

	public void setAccessGroupName(String accessGroupName) {
		this.accessGroupName = accessGroupName;
	}

	public String getAccessGroupId() {
		return accessGroupId;
	}

	public void setAccessGroupId(String accessGroupId) {
		this.accessGroupId = accessGroupId;
	}

	public String getTraitGroupTypeId() {
		return traitGroupTypeId;
	}

	public void setTraitGroupTypeId(String traitGroupTypeId) {
		this.traitGroupTypeId = traitGroupTypeId;
	}

	public String getUltimatePermission() {
		return UltimatePermission;
	}

	public void setUltimatePermission(String ultimatePermission) {
		UltimatePermission = ultimatePermission;
	}

	public String getTraitDescription() {
		return traitDescription;
	}

	public void setTraitDescription(String traitDescription) {
		this.traitDescription = traitDescription;
	}

	public String getChgPerm() {
		return chgPerm;
	}

	public void setChgPerm(String chgPerm) {
		this.chgPerm = chgPerm;
	}

	public String getAccessGroupPermission() {
		return accessGroupPermission;
	}

	public void setAccessGroupPermission(String accessGroupPermission) {
		this.accessGroupPermission = accessGroupPermission;
	}

	public String getUltimatePerm() {
		return ultimatePerm;
	}

	public void setUltimatePerm(String ultimatePerm) {
		this.ultimatePerm = ultimatePerm;
	}

	public String getTraitDataType() {
		return traitDataType;
	}

	public void setTraitDataType(String traitDataType) {
		this.traitDataType = traitDataType;
	}

	public String getAccessGroupPerm() {
		return accessGroupPerm;
	}

	public void setAccessGroupPerm(String accessGroupPerm) {
		this.accessGroupPerm = accessGroupPerm;
	}

	public String getOwnGroupPermission() {
		return ownGroupPermission;
	}

	public void setOwnGroupPermission(String ownGroupPermission) {
		this.ownGroupPermission = ownGroupPermission;
	}

	public String getUpdate() {
		return update;
	}

	public void setUpdate(String update) {
		this.update = update;
	}

	public String getTraitUnit() {
		return traitUnit;
	}

	public void setTraitUnit(String traitUnit) {
		this.traitUnit = traitUnit;
	}

	public String getOtherPerm() {
		return otherPerm;
	}

	public void setOtherPerm(String otherPerm) {
		this.otherPerm = otherPerm;
	}

	public String getOwnGroupName() {
		return ownGroupName;
	}

	public void setOwnGroupName(String ownGroupName) {
		this.ownGroupName = ownGroupName;
	}

	public String getOwnGroupPerm() {
		return ownGroupPerm;
	}

	public void setOwnGroupPerm(String ownGroupPerm) {
		this.ownGroupPerm = ownGroupPerm;
	}

	public String getTraitId() {
		return traitId;
	}

	public void setTraitId(String traitId) {
		this.traitId = traitId;
	}

	public String getTraitValueMaxLength() {
		return traitValueMaxLength;
	}

	public void setTraitValueMaxLength(String traitValueMaxLength) {
		this.traitValueMaxLength = traitValueMaxLength;
	}

	public String getChgOwner() {
		return chgOwner;
	}

	public void setChgOwner(String chgOwner) {
		this.chgOwner = chgOwner;
	}

	public String getTraitCaption() {
		return traitCaption;
	}

	public void setTraitCaption(String traitCaption) {
		this.traitCaption = traitCaption;
	}

	public String getTraitValRule() {
		return traitValRule;
	}

	public void setTraitValRule(String traitValRule) {
		this.traitValRule = traitValRule;
	}

	public String getAddAlias() {
		return addAlias;
	}

	public void setAddAlias(String addAlias) {
		this.addAlias = addAlias;
	}

	public String getTraitName() {
		return traitName;
	}

	public void setTraitName(String traitName) {
		this.traitName = traitName;
	}

	public String getIsTraitUsedForAnalysis() {
		return isTraitUsedForAnalysis;
	}

	public void setIsTraitUsedForAnalysis(String isTraitUsedForAnalysis) {
		this.isTraitUsedForAnalysis = isTraitUsedForAnalysis;
	}

	public String getOwnGroupId() {
		return ownGroupId;
	}

	public void setOwnGroupId(String ownGroupId) {
		this.ownGroupId = ownGroupId;
	}

	public String getDelete() {
		return delete;
	}

	public void setDelete(String delete) {
		this.delete = delete;
	}
	
	

}

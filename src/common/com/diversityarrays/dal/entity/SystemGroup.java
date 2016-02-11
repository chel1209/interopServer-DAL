package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="SystemGroup")
@EntityTag("SystemGroup")
public class SystemGroup extends DalEntity{
	
	@Column(name="systemGroupName")
	private String  systemGroupName;
	
	@Column(name="systemGroupDescription")
	private String  systemGroupDescription;
	
	@Column(name="systemGroupId")
	private String systemGroupId;
	
	public SystemGroup(){}

	public String getSystemGroupName() {
		return systemGroupName;
	}

	public void setSystemGroupName(String systemGroupName) {
		this.systemGroupName = systemGroupName;
	}

	public String getSystemGroupDescription() {
		return systemGroupDescription;
	}

	public void setSystemGroupDescription(String systemGroupDescription) {
		this.systemGroupDescription = systemGroupDescription;
	}

	public String getSystemGroupId() {
		return systemGroupId;
	}

	public void setSystemGroupId(String systemGroupId) {
		this.systemGroupId = systemGroupId;
	}
		

}

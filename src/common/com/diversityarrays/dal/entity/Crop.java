package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 
 * @author Raul Hernandez T.
 * @date 2/8/2016
 * @version 1.0 
 *
 */

@Table(name="Crop")
@EntityTag("Crop")
public class Crop extends DalEntity{

	@Column(name="systemGroupName")
	private String  systemGroupName;
	
	@Column(name="systemGroupDescription")
	private String  systemGroupDescription;
	
	@Column(name="systemGroupId")
	private String systemGroupId;
	
	public Crop(){}

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

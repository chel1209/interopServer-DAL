package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Table;


/**
 * @author: Raul Hernandez T.
 * @date:   10-06-2015
 */

@Table(name="Project")
@EntityTag("Project")
public class Project extends DalEntity{
	
	@Column(name="projectManagerId")
	private Integer projectManagerId;
	
	@Column(name="projectManagerName")
	private String  projectManagerName;
	
	@Column(name="projectStartDate")
	private String  projectStartDate;
	
	@Column(name="projectName")
	private String  projectName;
	
	@Column(name="projectNote")
	private String  projectNote;
	
	@Column(name="typeId")
	private Integer typeId;
	
	@Column(name="projectStatus")
	private String  projectStatus;
	
	@Column(name="projectEndDate")
	private String  projectEndDate;
	
	@Column(name="projectId")
	private String projectId;
	
	public Project(){}

	public Integer getProjectManagerId() {
		return projectManagerId;
	}

	public void setProjectManagerId(Integer projectManagerId) {
		this.projectManagerId = projectManagerId;
	}

	public String getProjectManagerName() {
		return projectManagerName;
	}

	public void setProjectManagerName(String projectManagerName) {
		this.projectManagerName = projectManagerName;
	}

	public String getProjectStartDate() {
		return projectStartDate;
	}

	public void setProjectStartDate(String projectStartDate) {
		this.projectStartDate = projectStartDate;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getProjectNote() {
		return projectNote;
	}

	public void setProjectNote(String projectNote) {
		this.projectNote = projectNote;
	}

	public Integer getTypeId() {
		return typeId;
	}

	public void setTypeId(Integer typeId) {
		this.typeId = typeId;
	}

	public String getProjectStatus() {
		return projectStatus;
	}

	public void setProjectStatus(String projectStatus) {
		this.projectStatus = projectStatus;
	}

	public String getProjectEndDate() {
		return projectEndDate;
	}

	public void setProjectEndDate(String projectEndDate) {
		this.projectEndDate = projectEndDate;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	

}

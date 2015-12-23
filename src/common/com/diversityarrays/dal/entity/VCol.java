package com.diversityarrays.dal.entity;

import javax.persistence.Column;
import javax.persistence.Table;

@Table(name="VCol")
@EntityTag("VCol")
public class VCol extends DalEntity{
	
	@Column(name="dummy")
	private String dummy;

}

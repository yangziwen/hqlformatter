package net.yangziwen.hqlformatter.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "table_relation")
public class TableRelation {
	
	@Id
	@Column
	private Long id;
	
	@Column
	private Long tableId;
	
	@Column
	private Long dependentTableId;
	
	public TableRelation() {};

	public TableRelation(Long tableId, Long dependentTableId) {
		this.tableId = tableId;
		this.dependentTableId = dependentTableId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTableId() {
		return tableId;
	}

	public void setTableId(Long tableId) {
		this.tableId = tableId;
	}

	public Long getDependentTableId() {
		return dependentTableId;
	}

	public void setDependentTableId(Long dependentTableId) {
		this.dependentTableId = dependentTableId;
	}
	
	

}

package net.yangziwen.hqlformatter.model;

public class TableRelation {
	
	private Long id;
	
	private Long tableId;
	
	private Long dependentTableId;

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

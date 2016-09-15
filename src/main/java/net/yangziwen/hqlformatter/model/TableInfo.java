package net.yangziwen.hqlformatter.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "table_info")
public class TableInfo {
	
	@Id
	@Column
	private Long id;
	
	@Column
	private String database;
	
	@Column
	private String tableName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	@Override
	public String toString() {
		return "TableInfo[id=" + id + ", database=" + database + ", tableName=" + tableName + "]";
	}

}

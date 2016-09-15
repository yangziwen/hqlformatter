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
	
	public TableInfo() {}
	
	public TableInfo(String database, String tableName) {
		this.database = database;
		this.tableName = tableName;
	}

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
	
	public static TableInfo parse(String fullTableName) {
		String[] array = fullTableName.split("\\.");
		return new TableInfo(array[0], array[1]);
	}

}

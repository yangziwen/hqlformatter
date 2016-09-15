package net.yangziwen.hqlformatter.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

import net.yangziwen.hqlformatter.util.StringUtils;

@Table(name = "table_info")
public class TableInfo {
	
	@Id
	@Column
	private Long id;
	
	@Column
	private String database;
	
	@Column
	private String tableName;
	
	@Column
	private String description;
	
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
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "TableInfo[id=" + id + ", database=" + database + ", tableName=" + tableName + "]";
	}
	
	public static TableInfo parse(String fullTableName, String defaultDatabase) {
		String[] array = fullTableName.split("\\.");
		if (array.length < 2 && StringUtils.isBlank(defaultDatabase)) {
			throw new IllegalArgumentException(String.format("failed to parse fullTableName[%s] with default database [%s]", fullTableName, defaultDatabase));
		}
		String database = array.length < 2 ? defaultDatabase : array[0];
		String tableName = array.length >= 2 ? array[1] : array[0];
		return new TableInfo(database, tableName);
	}

}

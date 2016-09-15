package net.yangziwen.hqlformatter.service;

import static net.yangziwen.hqlformatter.util.DataSourceFactory.getDataSource;

import java.util.ArrayList;
import java.util.List;

import net.yangziwen.hqlformatter.model.TableInfo;
import net.yangziwen.hqlformatter.model.TableRelation;
import net.yangziwen.hqlformatter.repository.TableInfoRepo;
import net.yangziwen.hqlformatter.repository.TableRelationRepo;
import net.yangziwen.hqlformatter.repository.base.QueryMap;
import net.yangziwen.hqlformatter.util.StringUtils;

public class TableService {

	private static TableInfoRepo tableInfoRepo = new TableInfoRepo(getDataSource());
	
	private static TableRelationRepo tableRelationRepo = new TableRelationRepo(getDataSource());
	
	public static void persistTableRelation(TableInfo table, List<TableInfo> dependentTableList) {
		table = ensureTableInfoExist(table);
		dependentTableList = ensureTableInfoExist(dependentTableList);
		for (TableInfo dependentTable : dependentTableList) {
			TableRelation rel = new TableRelation(table.getId(), dependentTable.getId());
			ensureTableRelationExist(rel);
		}
	}
	
	private static TableRelation ensureTableRelationExist(TableRelation relation) {
		TableRelation rel = tableRelationRepo.first(new QueryMap()
				.param("tableId", relation.getTableId())
				.param("dependentTableId", relation.getDependentTableId())
				);
		if (rel != null) {
			return rel;
		}
		tableRelationRepo.insert(relation);
		return relation;
	}
	
	private static TableInfo ensureTableInfoExist(TableInfo table) {
		TableInfo tbl = tableInfoRepo.first(new QueryMap()
				.param("database", table.getDatabase())
				.param("tableName", table.getTableName()));
		if (tbl != null) {
			if (StringUtils.isNotBlank(table.getDescription())
					&& !table.getDescription().equals(tbl.getDescription())) {
				tbl.setDescription(table.getDescription());
				tableInfoRepo.update(tbl);
			}
			return tbl;
		}
		tableInfoRepo.insert(table);
		return table;
	}
	
	private static List<TableInfo> ensureTableInfoExist(List<TableInfo> list) {
		List<TableInfo> newList = new ArrayList<TableInfo>();
		for (TableInfo table : list) {
			newList.add(ensureTableInfoExist(table));
		}
		return newList;
	}
	
}

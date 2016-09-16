package net.yangziwen.hqlformatter.service;

import static net.yangziwen.hqlformatter.util.DataSourceFactory.getDataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.yangziwen.hqlformatter.analyze.TableAnalyzer.Result;
import net.yangziwen.hqlformatter.model.TableInfo;
import net.yangziwen.hqlformatter.model.TableRelation;
import net.yangziwen.hqlformatter.repository.TableInfoRepo;
import net.yangziwen.hqlformatter.repository.TableRelationRepo;
import net.yangziwen.hqlformatter.repository.base.QueryMap;
import net.yangziwen.hqlformatter.util.StringUtils;
import net.yangziwen.hqlformatter.util.Utils;

public class TableService {

	private static TableInfoRepo tableInfoRepo = new TableInfoRepo(getDataSource());
	
	private static TableRelationRepo tableRelationRepo = new TableRelationRepo(getDataSource());
	
	public static List<TableInfo> getUpstreamTables(List<Long> tableIdList, int depth) {
		Set<Long> dependentIdSet = new HashSet<Long>();
		for (int i = 0; i < depth; i++) {
			if (Utils.isEmpty(tableIdList)) {
				break;
			}
			List<TableRelation> relList = tableRelationRepo.list(new QueryMap()
					.param("tableId__in", tableIdList)
					);
			tableIdList = new ArrayList<Long>();
			for (TableRelation rel : relList) {
				if (!dependentIdSet.contains(rel.getDependentTableId())) {
					tableIdList.add(rel.getDependentTableId());
				}
			}
			dependentIdSet.addAll(tableIdList);
		}
		if (Utils.isEmpty(dependentIdSet)) {
			return Collections.emptyList();
		}
		return tableInfoRepo.list(new QueryMap()
				.param("id__in", dependentIdSet)
				);
	}
	
	public static List<TableInfo> getDownstreamTables(List<Long> tableIdList, int depth) {
		Set<Long> derivedIdSet = new HashSet<Long>();
		for (int i = 0; i < depth; i++) {
			if (Utils.isEmpty(tableIdList)) {
				break;
			}
			List<TableRelation> relList = tableRelationRepo.list(new QueryMap()
					.param("dependentTableId__in", tableIdList)
					);
			tableIdList = new ArrayList<Long>();
			for (TableRelation rel : relList) {
				if (!derivedIdSet.contains(rel.getTableId())) {
					tableIdList.add(rel.getTableId());
				}
			}
			derivedIdSet.addAll(tableIdList);
		}
		if (Utils.isEmpty(derivedIdSet)) {
			return Collections.emptyList();
		}
		return tableInfoRepo.list(new QueryMap()
				.param("id__in", derivedIdSet)
				);
	}
	
	public static void persistTableRelation(List<Result> resultList) {
		for (Result result : resultList) {
			persistTableRelation(result.getTable(), result.getDependentTableList());
		}
	}
	
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

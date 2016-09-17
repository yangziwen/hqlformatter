package net.yangziwen.hqlformatter.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.yangziwen.hqlformatter.model.TableInfo;
import net.yangziwen.hqlformatter.model.TableRelation;
import net.yangziwen.hqlformatter.repository.base.QueryMap;
import net.yangziwen.hqlformatter.service.TableService;

public class TableCache {
	
	private static ConcurrentMap<Long, TableWrapper> TABLE_CACHE = new ConcurrentHashMap<Long, TableWrapper>();
	
	static {
		reload();
	}

	private TableCache() {
	}
	
	public static void reload() {
		
		ConcurrentMap<Long, TableWrapper> cache = new ConcurrentHashMap<Long, TableWrapper>();
		
		List<TableInfo> tblList = TableService.getTableInfoList(0, Integer.MAX_VALUE, QueryMap.emptyMap());
		
		for (TableInfo tableInfo : tblList) {
			cache.put(tableInfo.getId(), new TableWrapper(tableInfo));
		}
		
		List<TableRelation> relList = TableService.getTableRelationList(0, Integer.MAX_VALUE, QueryMap.emptyMap());
		
		for (TableRelation rel : relList) {
			cache.get(rel.getTableId()).addDependentId(rel.getDependentTableId());
			cache.get(rel.getDependentTableId()).addDerivedId(rel.getTableId());
		}
		
		TABLE_CACHE = cache;
	}
	
	public static TableWrapper getTable(Long tableId) {
		if (tableId == null) {
			return null;
		}
		return TABLE_CACHE.get(tableId);
	}
	
	public static List<TableWrapper> getDependentTables(Long tableId) {
		if (tableId == null) {
			return Collections.emptyList();
		}
		ConcurrentMap<Long, TableWrapper> cache = TABLE_CACHE;
		TableWrapper table = cache.get(tableId);
		if (table == null) {
			return Collections.emptyList();
		}
		List<TableWrapper> list = new ArrayList<TableWrapper>();
		for (Long dependentId : table.getDependentIds()) {
			if (table.getId().equals(dependentId)) {
				continue;
			}
			TableWrapper dependentTable = cache.get(dependentId);
			if (dependentTable == null) {
				continue;
			}
			list.add(dependentTable.clone());
		}
		return list;
	}
	
	public static List<TableWrapper> getDerivedTables(Long tableId) {
		if (tableId == null) {
			return Collections.emptyList();
		}
		ConcurrentMap<Long, TableWrapper> cache = TABLE_CACHE;
		TableWrapper table = cache.get(tableId);
		if (table == null) {
			return Collections.emptyList();
		}
		List<TableWrapper> list = new ArrayList<TableWrapper>();
		for (Long derivedId : table.getDerivedIds()) {
			if (table.getId().equals(derivedId)) {
				continue;
			}
			TableWrapper derivedTable = cache.get(derivedId);
			if (derivedTable == null) {
				continue;
			}
			list.add(derivedTable.clone());
		}
		return list;
	}
	
	public static List<TableWrapper> getDependentTableLayer(Collection<Long> tableIds) {
		List<TableWrapper> layer = new ArrayList<TableWrapper>();
		Set<Long> idSet = new LinkedHashSet<Long>();
		for (Long tableId : tableIds) {
			List<TableWrapper> list = getDependentTables(tableId);
			for (TableWrapper tbl : list) {
				if (!idSet.contains(tbl.getId())) {
					layer.add(tbl);
				}
				idSet.add(tbl.getId());
			}
		}
		return layer;
	}
	
	public static List<TableWrapper> getDerivedTableLayer(Collection<Long> tableIds) {
		List<TableWrapper> layer = new ArrayList<TableWrapper>();
		Set<Long> idSet = new LinkedHashSet<Long>();
		for (Long tableId : tableIds) {
			List<TableWrapper> list = getDerivedTables(tableId);
			for (TableWrapper tbl : list) {
				if (!idSet.contains(tbl.getId())) {
					layer.add(tbl);
				}
				idSet.add(tbl.getId());
			}
		}
		return layer;
	}
	
	public static RelationGraph getTableRelationGraph(Long tableId, int depth) {
		return new RelationGraph(TABLE_CACHE.get(tableId), depth);
	}
	
	public static class TableWrapper extends TableInfo implements Cloneable {
		
		private long weight;	// currently useless
		
		private LinkedHashSet<Long> dependentIds = new LinkedHashSet<Long>();
		
		private LinkedHashSet<Long> derivedIds = new LinkedHashSet<Long>();
		
		public TableWrapper(TableInfo tableInfo) {
			setId(tableInfo.getId());
			setDatabase(tableInfo.getDatabase());
			setTableName(tableInfo.getTableName());
			setDescription(tableInfo.getDescription());
		}

		public long getWeight() {
			return weight;
		}

		public void setWeight(long weight) {
			this.weight = weight;
		}

		public LinkedHashSet<Long> getDependentIds() {
			return dependentIds;
		}
		
		public void addDependentId(Long id) {
			dependentIds.add(id);
		}

		public LinkedHashSet<Long> getDerivedIds() {
			return derivedIds;
		}
		
		public void addDerivedId(Long id) {
			derivedIds.add(id);
		}
		
		@Override
		public String toString() {
			return "TableInfo[id=" + getId() + ", database=" + getDatabase() 
				+ ", tableName=" + getTableName() + ", weight=" + weight 
				+ ", dependentIds=" + dependentIds + ", derivedIds=" + derivedIds + "]";
		}
		
		@Override
		public TableWrapper clone() {
			TableWrapper wrapper = new TableWrapper(this);
			wrapper.setWeight(weight);
			wrapper.dependentIds = new LinkedHashSet<Long>(this.dependentIds);
			wrapper.derivedIds = new LinkedHashSet<Long>(this.derivedIds);
			return wrapper;
		}

	}
	
	public static class RelationGraph {
		
		private TableWrapper table;
		
		private List<List<TableWrapper>> dependentLayers = new ArrayList<List<TableWrapper>>();
		
		private List<List<TableWrapper>> derivedLayers = new ArrayList<List<TableWrapper>>();
		
		public RelationGraph(TableWrapper table) {
			this(table, Integer.MAX_VALUE);
		}
		
		public RelationGraph(TableWrapper table, int depth) {
			if (table == null) {
				throw new IllegalArgumentException("argument table cannot be null!");
			}
			this.table = table;
			this.initDependentLayers(depth);
			this.initDerivedLayers(depth);
		}

		public TableWrapper getTable() {
			return table;
		}

		public void setTable(TableWrapper table) {
			this.table = table;
		}

		public List<List<TableWrapper>> getDependentLayers() {
			return dependentLayers;
		}

		public void setDependentLayers(List<List<TableWrapper>> dependentLayers) {
			this.dependentLayers = dependentLayers;
		}

		public List<List<TableWrapper>> getDerivedLayers() {
			return derivedLayers;
		}

		public void setDerivedLayers(List<List<TableWrapper>> derivedLayers) {
			this.derivedLayers = derivedLayers;
		}
		
		private void initDependentLayers(int depth) {
			List<List<TableWrapper>> layers = new ArrayList<List<TableWrapper>>();
			Set<Long> idSet = new HashSet<Long>();
			
			List<TableWrapper> tempLayer = null;
			Set<Long> tableIds = new LinkedHashSet<Long>(Arrays.asList(table.getId()));
			while (depth > 0 && Utils.isNotEmpty(tempLayer = getDependentTableLayer(tableIds))) {
				tableIds = new LinkedHashSet<Long>();
				List<TableWrapper> layer = new ArrayList<TableWrapper>();
				for (TableWrapper tbl : tempLayer) {
					if (idSet.contains(tbl.getId())) {
						continue;
					}
					layer.add(tbl);
					idSet.add(tbl.getId());
					tableIds.add(tbl.getId());
				}
				layers.add(layer);
				depth --;
			}
			this.dependentLayers = layers;
		}
		
		private void initDerivedLayers(int depth) {
			List<List<TableWrapper>> layers = new ArrayList<List<TableWrapper>>();
			Set<Long> idSet = new HashSet<Long>();
			
			List<TableWrapper> tempLayer = null;
			Set<Long> tableIds = new LinkedHashSet<Long>(Arrays.asList(table.getId()));
			while (depth > 0 && Utils.isNotEmpty(tempLayer = getDerivedTableLayer(tableIds))) {
				tableIds = new LinkedHashSet<Long>();
				List<TableWrapper> layer = new ArrayList<TableWrapper>();
				for (TableWrapper tbl : tempLayer) {
					if (idSet.contains(tbl.getId())) {
						continue;
					}
					layer.add(tbl);
					idSet.add(tbl.getId());
					tableIds.add(tbl.getId());
				}
				layers.add(layer);
				depth --;
			}
			this.derivedLayers = layers;
		}
		
	}
	
}

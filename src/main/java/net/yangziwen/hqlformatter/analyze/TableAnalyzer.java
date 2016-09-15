package net.yangziwen.hqlformatter.analyze;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.yangziwen.hqlformatter.format.JoinTable;
import net.yangziwen.hqlformatter.format.Parser;
import net.yangziwen.hqlformatter.format.Query;
import net.yangziwen.hqlformatter.format.QueryTable;
import net.yangziwen.hqlformatter.format.SimpleTable;
import net.yangziwen.hqlformatter.format.Table;
import net.yangziwen.hqlformatter.format.UnionTable;
import net.yangziwen.hqlformatter.model.TableInfo;
import net.yangziwen.hqlformatter.util.StringUtils;

public class TableAnalyzer {
	
	public static Result analyze(String sql, String defaultDatabase) {
		int startPos = -1;
		if (StringUtils.isBlank(sql) || (startPos = sql.toLowerCase().indexOf("select")) == -1) {
			return null;
		}
		String tableName = parseTableName(sql.substring(0, startPos));
		if (StringUtils.isBlank(tableName)) {
			return null;
		}
		TableInfo table = TableInfo.parse(tableName, defaultDatabase);
		Result result = new Result(table);
		
		Query query = Parser.parseSelectSql(sql.substring(startPos));
		Set<SimpleTable> simpleTableSet = new TreeSet<SimpleTable>();
		collectSimpleTables(query.tableList(), simpleTableSet);
		for (SimpleTable st : simpleTableSet) {
			TableInfo t = TableInfo.parse(st.table(), defaultDatabase);
			result.addDependentTable(t);
		}
		return result;
	}
	
	private static String parseTableName(String sql) {
		Pattern pattern = Pattern.compile("insert\\s+\\w*\\s+table\\s+([^ ]+)\\s+", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
	
	private static void collectSimpleTables(List<Table<?>> tables, Set<SimpleTable> simpleTableSet) {
		for (Table<?> table : tables) {
			collectSimpleTables(table, simpleTableSet);
		}
	}
	
	private static void collectSimpleTables(Table<?> table, Set<SimpleTable> simpleTableSet) {
		if (SimpleTable.class.isInstance(table)) {
			simpleTableSet.add(SimpleTable.class.cast(table));
			return;
		}
		if (JoinTable.class.isInstance(table)) {
			JoinTable joinTable = (JoinTable) table;
			collectSimpleTables(joinTable.baseTable(), simpleTableSet);
			return;
		}
		if (QueryTable.class.isInstance(table)) {
			QueryTable queryTable = (QueryTable) table;
			collectSimpleTables(queryTable.query().tableList(), simpleTableSet);
			return;
		}
		if (UnionTable.class.isInstance(table)) {
			UnionTable unionTable = (UnionTable) table;
			collectSimpleTables(unionTable.unionTableList(), simpleTableSet);
			return;
		}
	}
	
	public static class Result {
		
		private TableInfo table;
		
		private List<TableInfo> dependentTableList = new ArrayList<TableInfo>();
		
		public Result(TableInfo table) {
			this.table = table;
		}
		
		public TableInfo getTable() {
			return table;
		}
		
		public Result addDependentTable(TableInfo dependentTable) {
			dependentTableList.add(dependentTable);
			return this;
		}
		
		public List<TableInfo> getDependentTableList() {
			return dependentTableList;
		}
		
	}
	
}

package net.yangziwen.hqlformatter.analyze;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.yangziwen.hqlformatter.format.JoinTable;
import net.yangziwen.hqlformatter.format.Parser;
import net.yangziwen.hqlformatter.format.Query;
import net.yangziwen.hqlformatter.format.QueryTable;
import net.yangziwen.hqlformatter.format.SimpleTable;
import net.yangziwen.hqlformatter.format.Table;
import net.yangziwen.hqlformatter.format.UnionTable;
import net.yangziwen.hqlformatter.model.TableInfo;
import net.yangziwen.hqlformatter.util.StringUtils;
import net.yangziwen.hqlformatter.util.Utils;
import spark.utils.IOUtils;

public class TableAnalyzer {
	
	private static final Logger logger = LoggerFactory.getLogger(TableAnalyzer.class);
	
	private static final Pattern DATABASE_PATTERN = Pattern.compile("USE\\s+([^ ]+)\\s*", Pattern.CASE_INSENSITIVE);
	
	private static final Pattern DESCRIPTION_PATTERN = Pattern.compile("crm-dm-ares[^\\\\/]*?(?:\\\\|/)(.+?)(?:\\\\|/)(hql|sql|calculation|[\\w_]+\\.sql)", Pattern.CASE_INSENSITIVE);
	
	public static List<Result> analyze(File file) {
		List<Result> resultList = new ArrayList<Result>();
		analyze0(file, resultList);
		return resultList;
	}
	
	private static void analyze0(File file, List<Result> resultList) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				analyze0(f, resultList);
			}
		}
		if (!file.getName().endsWith(".sql") && !file.getName().endsWith(".txt")) {
			return;
		}
		resultList.addAll(doAnalyze(file));
	}
	
	private static List<Result> doAnalyze(File file) {
		logger.info("analyze file [{}]", file.getAbsolutePath().replaceAll("\\\\", "/"));
		List<Result> resultList = new ArrayList<Result>();
		InputStream input = null;
		Matcher descriptionMather = DESCRIPTION_PATTERN.matcher(file.getAbsolutePath());
		String description = "";
		if (descriptionMather.find()) {
			description = descriptionMather.group(1).replaceAll("\\\\|/", "-").replace("自动绩效-", "");
		}
		String defaultDatabase = "";
		try {
			input = new FileInputStream(file);
			String content = IOUtils.toString(input);
			for (String sql : content.split(";")) {
				Matcher matcher = DATABASE_PATTERN.matcher(sql);
				if (matcher.find()) {
					defaultDatabase = matcher.group(1);
					continue;
				}
				Result result = TableAnalyzer.analyze(sql, defaultDatabase);
				if (result == null) {
					continue;
				}
				if (StringUtils.isNotBlank(description)) {
					result.getTable().setDescription(description);
				}
				resultList.add(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Utils.closeQuietly(input);
		}
		return resultList;
	}
	
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

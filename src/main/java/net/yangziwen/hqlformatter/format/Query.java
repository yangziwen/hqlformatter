package net.yangziwen.hqlformatter.format;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import net.yangziwen.hqlformatter.util.CollectionUtils;
import net.yangziwen.hqlformatter.util.StringUtils;

public class Query {
	
	private static final Pattern COMMENT_PREFIX_PATTERN = Pattern.compile("\\s*?--(?!\\d+\\})");
	
	protected List<String> selectList = new ArrayList<String>();
	
	protected List<Table<?>> tableList = new ArrayList<Table<?>>();
	
	protected List<String> whereList = new ArrayList<String>();

	protected List<String> groupByList = new ArrayList<String>();
	
	protected int startPos;
	
	protected int endPos;
	
	public int start() {
		return startPos;
	}
	
	public Query start(int startPos) {
		this.startPos = startPos;
		return this;
	}
	
	public int end() {
		return endPos;
	}

	public Query end(int endPos) {
		this.endPos = endPos;
		return this;
	}

	public List<String> selectList() {
		return selectList;
	}
	
	public List<Table<?>> tableList() {
		return tableList;
	}
	
	public List<String> whereList() {
		return whereList;
	}
	
	public List<String> groupByList() {
		return groupByList;
	}
	
	public Query addSelects(List<String> selectList) {
		this.selectList.addAll(selectList);
		return this;
	}
	
	public Query addTables(List<Table<?>> tableList) {
		this.tableList.addAll(tableList);
		return this;
	}
	
	public Query addWheres(List<String> whereList) {
		this.whereList.addAll(whereList);
		return this;
	}
	
	public Query addGroupBys(List<String> groupByList) {
		this.groupByList.addAll(groupByList);
		return this;
	}
	
	@Override
	public String toString() {
		return toString("    ");
	}
	
	public String toString(String indent) {
		return format(indent, new StringWriter()).toString();
	}
	
	public StringWriter format(String indent, StringWriter writer) {
		return format(indent, 0, writer);
	}
	
	public StringWriter format(String indent, int nestedDepth, StringWriter writer) {
		this.formatSelect(indent, nestedDepth, writer)
			.formatFrom(indent, nestedDepth, writer)
			.formatWhere(indent, nestedDepth, writer)
			.formatGroupBy(indent, nestedDepth, writer)
		;
		return writer;
	}
	
	private Query formatSelect(String indent, int nestedDepth, StringWriter writer) {
		String sep = chooseSeprator(selectList, indent, nestedDepth + 1);
		writer.append("SELECT ").append(selectList.get(0));
		for(int i = 1; i < selectList.size(); i++) {
			writer.append(",");
			String clause = selectList.get(i).trim();
			if(COMMENT_PREFIX_PATTERN.matcher(clause).find()) {		// 添加对注释的支持
				String[] strs = clause.split("(\\r\\n)|\\r|\\n");
				if(strs.length > 1) {
					String comment = strs[0];
					writer.append("  ").append(comment);
					clause = StringUtils.join(Arrays.copyOfRange(strs, 1, strs.length), "\n").trim();
				}
			}
			writer.append(sep)
				.append(clause);
		}
		return this;
	}
	
	private Query formatFrom(String indent, int nestedDepth, StringWriter writer) {
		writer.append("\n").append(StringUtils.repeat(indent, nestedDepth)).append("FROM ");
		tableList.get(0).format(indent, nestedDepth + 1, writer);
		for(int i = 1; i < tableList.size(); i++) {
			//writer.append("\n").append(baseIndent).append(indent);
			tableList.get(i).format(indent, nestedDepth + 1, writer);
		}
		return this;
	}
	
	private Query formatWhere(String indent, int nestedDepth, StringWriter writer) {
		if(CollectionUtils.isEmpty(whereList)) {
			return this;
		}
		String sep = chooseSeprator(whereList, indent, nestedDepth + 1);
		writer.append("\n").append(StringUtils.repeat(indent, nestedDepth))
			.append("WHERE ").append(whereList.get(0));
		for(int i = 1; i < whereList.size(); i++) {
			// 支持where语句中进行单行注释，如下
			// -- AND t.manager_type = 'XXX'
			if(!whereList.get(i-1).endsWith("--")) {
				writer.append(sep);
			} else {
				writer.append(" ");
			}
			writer.append("AND ").append(whereList.get(i));
		}
		return this;
	}
	
	private Query formatGroupBy(String indent, int nestedDepth, StringWriter writer) {
		if(CollectionUtils.isEmpty(groupByList)) {
			return this;
		}
		writer.append("\n").append(StringUtils.repeat(indent, nestedDepth))
			.append("GROUP BY ")
			.append(StringUtils.join(groupByList.toArray(), ", "))
		;
		return this;
	}
	
	
	private static String chooseSeprator(List<String> list, String indent, int nestedDepth) {
		String sep = "\n" + StringUtils.repeat(indent, nestedDepth);
		if(list.size() > 5) {
			return sep;
		}
		int totalLen = 0;
		for(String condition: list) {
			if(COMMENT_PREFIX_PATTERN.matcher(condition).find()) {
				return sep;
			}
			totalLen += condition.length();
		}
		if(totalLen < 80) {
			sep = " ";
		}
		return sep;
	}
	
}

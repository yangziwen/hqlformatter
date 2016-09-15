package net.yangziwen.hqlformatter.format;

import java.io.StringWriter;

import net.yangziwen.hqlformatter.util.StringUtils;

public class SimpleTable extends AbstractTable<SimpleTable> implements Table<SimpleTable>, Comparable<SimpleTable> {
	
	private String table;
	
	public SimpleTable(String table, String alias, int startPos, int endPos) {
		this.table = table;
		this.alias = alias;
		this.startPos = startPos;
		this.endPos = endPos;
	}
	
	public SimpleTable table(String table) {
		this.table = table;
		return this;
	}

	@Override
	public String table() {
		return table;
	}
	
	@Override
	public String toString() {
		return table + (StringUtils.isNotBlank(alias)? " " + alias: "");
	}

	@Override
	public StringWriter format(String indent, int nestedDepth, StringWriter writer) {
		writer.append(table());
		if(StringUtils.isNotBlank(alias())) {
			writer.append(" ").append(alias());
		}
		if(headComment != null) {
			writer.append("  ").append(headComment().content());
		}
		return writer;
	}

	@Override
	public int compareTo(SimpleTable other) {
		if (other == null || StringUtils.isBlank(other.table)) {
			return 1;
		}
		if (StringUtils.isBlank(table)) {
			return -1;
		}
		return table.compareTo(other.table);
	}

}

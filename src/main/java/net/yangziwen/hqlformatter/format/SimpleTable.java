package net.yangziwen.hqlformatter.format;

import java.io.StringWriter;

import net.yangziwen.hqlformatter.util.StringUtils;

public class SimpleTable implements Table {
	
	private String table;
	
	private String alias;
	
	private int startPos = 0;
	
	private int endPos = 0;
	
	public SimpleTable(String table, String alias, int startPos, int endPos) {
		this.table = table;
		this.alias = alias;
		this.startPos = startPos;
		this.endPos = endPos;
	}

	@Override
	public String table() {
		return table;
	}
	
	public SimpleTable table(String table) {
		this.table = table;
		return this;
	}

	@Override
	public String alias() {
		return alias;
	}
	
	public SimpleTable alias(String alias) {
		this.alias = alias;
		return this;
	}
	
	public int start() {
		return startPos;
	}
	
	public int end() {
		return endPos;
	}
	
	public SimpleTable end(int endPos) {
		this.endPos = endPos;
		return this;
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
		return writer;
	}

}

package net.yangziwen.hqlformatter.format;

import java.io.StringWriter;

import net.yangziwen.hqlformatter.util.StringUtils;

public class QueryTable implements Table {
	
	private Query query;
	
	private String alias;
	
	private int startPos;
	
	private int endPos;
	
	public QueryTable(Query query) {
		this.query = query;
		endPos = query.end();
	}
	
	public Query query() {
		return query;
	}

	@Override
	public String table() {
		return "QueryTable[" + query + "]";
	}

	@Override
	public String alias() {
		return alias;
	}
	
	public QueryTable alias(String alias) {
		this.alias = alias;
		return this;
	}
	
	public QueryTable start(int startPos) {
		this.startPos = startPos;
		return this;
	}
	
	public int start() {
		return this.startPos;
	}
	
	public QueryTable end(int endPos) {
		this.endPos = endPos;
		return this;
	}

	@Override
	public int end() {
		return endPos;
	}

	@Override
	public StringWriter format(String indent, int nestedDepth, StringWriter writer) {
		writer.append("(").append("\n").append(StringUtils.repeat(indent, nestedDepth));
		query.format(indent, nestedDepth, writer);
		writer.append("\n").append(StringUtils.repeat(indent, nestedDepth - 1)).append(")");
		if(StringUtils.isNotBlank(alias())) {
			writer.append(" ").append(alias());
		}
		return writer;
	}

}

package net.yangziwen.hqlformatter.format;

import java.io.StringWriter;

import net.yangziwen.hqlformatter.util.StringUtils;

public class QueryTable extends AbstractTable<QueryTable> implements Table<QueryTable> {
	
	private Query query;
	
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

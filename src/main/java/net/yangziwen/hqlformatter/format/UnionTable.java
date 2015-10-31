package net.yangziwen.hqlformatter.format;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.yangziwen.hqlformatter.util.StringUtils;

public class UnionTable extends AbstractTable<UnionTable> implements Table<UnionTable> {
	
	private List<Keyword> unionKeywords = new ArrayList<Keyword>();
	
	private List<Table<?>> unionTables = new ArrayList<Table<?>>();	// 按道理，这些table都应该是QueryTable

	@Override
	public String table() {
		List<String> tableNames = new ArrayList<String>();
		for(Table<?> table: unionTables) {
			tableNames.add(table.table());
		}
		return "UnionTable[" + StringUtils.join(tableNames.toArray(), ",") + "]";
	}
	
	public List<Keyword> unionKeywordList() {
		return unionKeywords;
	}
	
	public UnionTable addUnionKeyword(Keyword keyword) {
		if(!keyword.contains("union")) {
			throw new IllegalStateException(String.format("%s is not a union keyword!", keyword));
		}
		unionKeywords.add(keyword);
		return this;
	}
	
	public List<Table<?>> unionTableList() {
		return unionTables;
	}
	
	public Table<?> lastTable() {
		int size = unionTables.size();
		return unionTables.get(size - 1);
	}
	
	public UnionTable addUnionTable(Table<?> table) {
		if(table instanceof UnionTable) {
			UnionTable another = (UnionTable) table;
			unionTables.addAll(another.unionTables);
		} else {
			unionTables.add(table);
		}
		return this;
	}
	
	@Override
	public Comment headComment() {
		return unionTables.get(0).headComment();
	}
	
	@Override
	public UnionTable headComment(Comment comment) {
		unionTables.get(0).headComment(comment);
		return this;
	}

	@Override
	public StringWriter format(String indent, int nestedDepth, StringWriter buff) {
		String baseIndent = StringUtils.repeat(indent, nestedDepth);
		StringWriter sw = new StringWriter();
		unionTables.get(0).format(indent, nestedDepth, sw);
		int idx = sw.getBuffer().lastIndexOf(")") - 1;
		if(idx < 0) {
			idx = sw.getBuffer().length();
		}
		buff.append(sw.getBuffer().substring(0, idx));
		
		int size = unionTables.size();
		
		for(int i = 1; i < size; i++) {
			buff.append("\n");
			buff.append(baseIndent).append("UNION ALL");
			
			Comment comment = unionKeywords.get(i - 1).comment();
			if(comment != null) {
				buff.append("  ").append(comment.content());
			}
			
			buff.append("\n").append(baseIndent);
			sw = new StringWriter();
			unionTables.get(i).format(indent, nestedDepth, sw);
			idx = sw.getBuffer().indexOf("(") + 1;
			int idx2 = sw.getBuffer().lastIndexOf(")");
			if(idx2 == -1 || i == size - 1) {
				idx2 = sw.getBuffer().length();
			}
			buff.append(sw.getBuffer().substring(idx, idx2));
		}
		return buff.append(" ").append(alias()).append("\n");
	}

}

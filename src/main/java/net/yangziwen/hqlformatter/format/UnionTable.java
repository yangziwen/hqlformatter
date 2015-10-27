package net.yangziwen.hqlformatter.format;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.yangziwen.hqlformatter.util.StringUtils;

public class UnionTable implements Table {
	
	private List<Table> unionTables = new ArrayList<Table>();	// 按道理，这些table都应该是QueryTable
	
	private String alias;
	
	private int startPos;
	
	private int endPos;

	@Override
	public String table() {
		List<String> tableNames = new ArrayList<String>();
		for(Table table: unionTables) {
			tableNames.add(table.table());
		}
		return "UnionTable[" + StringUtils.join(tableNames.toArray(), ",") + "]";
	}

	@Override
	public String alias() {
		return alias;
	}
	
	public UnionTable alias(String alias) {
		this.alias = alias;
		return this;
	}
	
	public UnionTable start(int startPos) {
		this.startPos = startPos;
		return this;
	}
	
	public int start() {
		return startPos;
	}
	
	public UnionTable end(int endPos) {
		this.endPos = endPos;
		return this;
	}

	@Override
	public int end() {
		return endPos;
	}
	
	public List<Table> unionTableList() {
		return unionTables;
	}
	
	public Table lastTable() {
		int size = unionTables.size();
		return unionTables.get(size - 1);
	}
	
	public UnionTable addUnionTable(Table table) {
		if(table instanceof UnionTable) {
			UnionTable another = (UnionTable) table;
			unionTables.addAll(another.unionTables);
		} else {
			unionTables.add(table);
		}
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
			buff.append("\n")
				.append(baseIndent).append("---------").append("\n")
				.append(baseIndent).append("UNION ALL").append("\n")
				.append(baseIndent).append("---------").append("\n")
				.append("\n").append(baseIndent);
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

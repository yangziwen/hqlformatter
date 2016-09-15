package net.yangziwen.hqlformatter.format;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.yangziwen.hqlformatter.util.Utils;
import net.yangziwen.hqlformatter.util.StringUtils;


public class JoinTable extends AbstractTable<JoinTable> implements Table<JoinTable> {
	
	private Keyword joinType;
	
	private Table<?> baseTable;
	
	private List<String> joinOnList = new ArrayList<String>();
	
	@Override
	public String table() {
		return "JoinTable[" + baseTable.table() + "]";
	}

	public JoinTable joinType(Keyword joinType) {
		this.joinType = joinType;
		return this;
	}
	
	public Keyword joinType() {
		return joinType;
	}
	
	public JoinTable addJoinOns(List<String> joinOnList) {
		this.joinOnList = joinOnList;
		return this;
	}
	
	public List<String> joinOnList() {
		return joinOnList;
	}
	
	public Table<?> baseTable() {
		return baseTable;
	}
	
	public JoinTable baseTable(Table<?> baseTable) {
		this.baseTable = baseTable;
		return this;
	}

	@Override
	public StringWriter format(String indent, int nestedDepth, StringWriter writer) {
		String baseIndent = StringUtils.repeat(indent, nestedDepth - 1);
		writer.append("\n").append(baseIndent)
			.append(joinType.name()).append(" ");
		baseTable.format(indent, nestedDepth, writer);
		if(Utils.isNotEmpty(joinOnList)) {
			writer.append("\n").append(baseIndent)
				.append("ON").append(" ").append(joinOnList.get(0));
			for(int i = 1; i < joinOnList.size(); i++) {
				writer.append(" AND ").append(joinOnList.get(i));
			}
		}
		return writer;
	}

}

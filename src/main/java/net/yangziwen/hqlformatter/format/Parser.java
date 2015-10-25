package net.yangziwen.hqlformatter.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.yangziwen.hqlformatter.util.StringUtils;

public class Parser {
	
	public static final String[] KEYWORDS = {
		"select",
		"from",
		"join",
		"inner join",
		"left outer join",
		"full outer join",
		"semi join",
		"on",
		"union all",
		"where",
		"group by"
	};
	
	public static final Pattern KEYWORD_PATTERN = buildKeywordPatten(KEYWORDS);
	
	private Parser() {}
	
	private static Pattern buildKeywordPatten(String[] keywords) {
		List<String> keywordRegexList = new ArrayList<String>();
		for(String keyword: keywords) {
			keywordRegexList.add(keyword.replace(" ", "\\s+?"));
		}
		return Pattern.compile("(?<=^|[^\\w\\d])(" + StringUtils.join(keywordRegexList, "|") + ")[^\\w\\d]", Pattern.CASE_INSENSITIVE);
	}
	
	/**
	 * 解析查询语句
	 */
	public static Query parseQuery(String sql, int start) {
		Keyword selectKeyword = findKeyWord(sql, start);
		Keyword fromKeyword = findKeyWord(sql, selectKeyword.end());
		
		List<Table> tableList = parseTables(sql, fromKeyword.end());
		Table lastTable = tableList.get(tableList.size() - 1);
		
		Keyword nextKeyword = findKeyWord(sql, lastTable.end());
		Keyword whereKeyword = null;
		Keyword groupByKeyword = null;
		
		int endPos = findEndPos(sql, lastTable.end());
		
		if(nextKeyword.is("where") && nextKeyword.end() < endPos) {
			whereKeyword = nextKeyword;
			nextKeyword = findKeyWord(sql, whereKeyword.end());
		}
		
		if(nextKeyword.is("group by") && nextKeyword.end() < endPos) {
			groupByKeyword = nextKeyword;
			nextKeyword = findKeyWord(sql, groupByKeyword.end());
		}
		
		if(nextKeyword.is("union all") && nextKeyword.end() < endPos) {
			endPos = nextKeyword.start() - 1;
		}
		
		List<String> selectList = parseClauseList(sql, selectKeyword.end() + 1, fromKeyword.start());
		List<String> whereList = Collections.emptyList();
		List<String> groupByList = Collections.emptyList();
		
		if(whereKeyword != null) {
			whereList = splitByAnd(sql, whereKeyword.end() + 1, groupByKeyword != null? groupByKeyword.start() - 1: endPos);
		}
		if(groupByKeyword != null) {
			groupByList = parseClauseList(sql, groupByKeyword.end() + 1, endPos);
		}
		
		return new Query()
			.addSelects(selectList)
			.addTables(tableList)
			.addWheres(whereList)
			.addGroupBys(groupByList)
			.start(selectKeyword.start())
			.end(endPos - 1)
		;
	}
	
	/**
	 * 解析由“,”分隔的条件，如select或group by后的约束条件语句
	 */
	public static List<String> parseClauseList(String sql, int start, int end) {
		String substring = sql.substring(start, end);
		List<String> list = new ArrayList<String>();
		int pos = 0;
		int bracketCnt = 0;
		boolean quoteFlag = false;
		boolean doubleQuoteFlag = false;
		for(int i = 0, len = substring.length(); i < len; i++) {
			char c = substring.charAt(i);
			if(c == '(') {
				bracketCnt ++;
			} 
			else if (c == ')') {
				bracketCnt --;
			} 
			else if (c == '\'') {
				quoteFlag = !quoteFlag;
			}
			else if (c == '"') {
				doubleQuoteFlag = !doubleQuoteFlag;
			}
			else if (c == ',' && bracketCnt == 0 && !quoteFlag && !doubleQuoteFlag) {
				list.add(substring.substring(pos, i).trim());
				pos = i + 1;
			}
		}
		list.add(substring.substring(pos, substring.length()).trim());
		return list;
	}
	
	public static List<String> splitBy(String str, String regex, int start, int end) {
		String substring = str.substring(start, end);
		String[] arr = substring.split(regex);
		List<String> list = new ArrayList<String>();
		for(String s: arr) {
			list.add(s.trim());
		}
		return list;
	}
	
	public static List<String> splitByAnd(String str, int start, int end) {
		return splitBy(str, "[aA][nN][dD]", start, end);
	}
	
	public static int findEndPos(String sql, int start) {
		int pos = findEndBracket(sql, start);
		if(pos < 0) {
			pos = sql.length();
		}
		return pos;
	}
	
	public static int findEndBracket(String sql, int start) {
		return findEndBracket(sql, start, sql.length());
	}
	
	public static int findEndBracket(String sql, int start, int end) {
		int cnt = 0;
		for(int i = start; i < end; i ++) {
			char c = sql.charAt(i);
			if(c == '(') {
				cnt ++;
			} else if(c == ')') {
				cnt --;
			}
			if(cnt < 0) {
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * 解析from后的一个或多个table，包括子查询产生的临时表
	 */
	public static List<Table> parseTables(String sql, int start) {
		List<Table> tables = new ArrayList<Table>();
		Table table = parseTable(sql, start);
		tables.add(table);
		Keyword nextKeyword = findKeyWord(sql, table.end() + 1);
		while(nextKeyword.contains("join")) {
			table = parseJoinTable(sql, table.end() + 1);
			if(table != null) {
				tables.add(table);
			}
			nextKeyword = findKeyWord(sql, table.end());
			if(findEndBracket(sql, table.end(), nextKeyword.start()) > 0) {
				break;	// 子查询在当前on后面结束了
			}
		}
		return tables;
	}
	
	private static Table parseTable(String sql, int start) {
		int i = start;
		char c = sql.charAt(i);
		while (Character.isWhitespace(c)) {
			i++;
			c = sql.charAt(i);
		}
		Table table = null;
		if(c == '(') {
			table = parseQueryTable(sql, i + 1);
		} else {
			table = parseSimpleTable(sql, i);
		}
		return table;
	}
	
	/**
	 * 解析通过join语句连接的table
	 */
	private static Table parseJoinTable(String sql, int start) {
		Keyword nextKeyword = findKeyWord(sql, start);
		Keyword joinKeyword = null;
		if(nextKeyword.contains("join")) {
			joinKeyword = nextKeyword;
			start = joinKeyword.end();
		}
		Table table = parseTable(sql, start);
		int curPos = table.end();
		
		// 处理join on的情形
		if(joinKeyword != null) {
			JoinTable joinTable = new JoinTable()
				.baseTable(table)
				.joinType(joinKeyword);	
			nextKeyword = findKeyWord(sql, curPos);
			if(nextKeyword.is("on")) {
				Keyword onKeyword = nextKeyword;
				nextKeyword = findKeyWord(sql, onKeyword.end() + 1);
				int endPos = findEndBracket(sql, start, nextKeyword.start());
				if(endPos == -1) {
					endPos = nextKeyword.start() - 1;
				}
				joinTable.addJoinOns(splitByAnd(sql, onKeyword.end(), endPos))
					.start(joinKeyword.start()).end(endPos);
			}
			table = joinTable;
		}
		return table;
	}
	
	/**
	 * 解析子查询产生的临时表
	 */
	public static Table parseQueryTable(String sql, int start) {
		Query query = parseQuery(sql, start);
		Keyword nextKeyword = findKeyWord(sql, query.end() + 1);
		if(!nextKeyword.is("union all")) {
			int endPos = findEndBracket(sql, query.end() + 1, nextKeyword.start());
			int nextEndPos = findEndBracket(sql, endPos + 1, nextKeyword.start());
			if(nextEndPos == -1 || nextEndPos > nextKeyword.start()) {
				nextEndPos = nextKeyword.start();
			}
			String alias = sql.substring(endPos + 1, nextEndPos).trim().split("\\s+")[0];
			return new QueryTable(query).alias(alias)
					.start(start).end(endPos + alias.length());
		}
		// 处理union all的情形
		Keyword unionKeyword = nextKeyword;
		UnionTable unionTable = new UnionTable();
		QueryTable table = new QueryTable(query)
				.start(start).end(unionKeyword.start() - 1);
		unionTable.addUnionTable(table);
		while(nextKeyword.is("union all")) {
			unionKeyword = nextKeyword;
			query = parseQuery(sql, unionKeyword.end() + 1);
			table = new QueryTable(query).start(unionKeyword.end() + 1).end(query.end());
			unionTable.addUnionTable(table);
			nextKeyword = findKeyWord(sql, table.end());
		}
		int endPos = findEndBracket(sql, unionTable.lastTable().end() + 1, nextKeyword.start());
		String alias = sql.substring(endPos + 1, nextKeyword.start()).trim();
		return unionTable.alias(alias).start(start).end(nextKeyword.start() - 1);
	}
	
	/**
	 * 解析单张表的表名
	 */
	public static SimpleTable parseSimpleTable(String sql, int start) {
		Keyword nextKeyword = findKeyWord(sql, start);
		if(nextKeyword.is("null")) {
			return null;
		}
		int end = nextKeyword != null? nextKeyword.start() - 1: sql.length();
		String str = sql.substring(start, end).trim();
		String[] arr = str.split("\\s");
		String tableName = arr[0];
		String alias = arr.length >= 2? arr[1]: "";
		if (arr.length >= 3 && "as".equalsIgnoreCase(alias)) {
			alias = arr[2];
		}
		return new SimpleTable(tableName, alias, start, end);
	}
	
	public static Keyword findKeyWord(String sql, int start) {
		Matcher matcher = KEYWORD_PATTERN.matcher(sql);
		if(matcher.find(start)) {
			return new Keyword(matcher.group(1), matcher.start(1), matcher.end(1));
		}
		return Keyword.returnNull(sql);
	}
	
}

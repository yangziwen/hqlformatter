package net.yangziwen.hqlformatter.format;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Arrays;
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
		"left join",
		"left outer join",
		"full outer join",
		"semi join",
		"on",
		"union all",
		"where",
		"group by"
	};

	public static final Pattern KEYWORD_PATTERN = buildKeywordPatten(KEYWORDS);

	// 应对珠玑脚本中类似{DATE--1}表达式的例外，此情形中“--”不能视为注释
	// 为什么第一个\\s{0,n}中的n大于3就会报错?
	private static Pattern ARES_ESCAPE_PATTERN = Pattern.compile("(?<=\\{\\s{0,3}(HOUR|DATE|MONTH|YEAR)\\s*?)--\\s*?\\d+?\\s*?\\}", Pattern.CASE_INSENSITIVE);

	private Parser() {}

	private static Pattern buildKeywordPatten(String[] keywords) {
	    List<String> keywordRegexList = Arrays.stream(keywords)
	            .map(keyword -> keyword.replace(" ", "\\s+?"))
	            .collect(toList());
		return Pattern.compile("(?<=^|[^_\\-0-9a-zA-Z\u4e00-\u9fa5])(" + StringUtils.join(keywordRegexList, "|") + ")(?=[^_0-9a-zA-Z\u4e00-\u9fa5])", Pattern.CASE_INSENSITIVE);
	}

	private static String purifySql(String sql) {
		sql = sql.replaceAll("/\\*[\\w\\W]*?\\*/", "");
		sql = sql.replace("\t", "    ");
		return sql + "   ";
	}

	public static Query parseSelectSql(String sql) {
		return parseQuery(purifySql(sql), 0);
	}

	/**
	 * 解析查询语句
	 */
	private static Query parseQuery(String sql, int start) {
		Keyword selectKeyword = findKeyWord(sql, start);
		Keyword fromKeyword = findKeyWord(sql, selectKeyword.end());

		List<Table<?>> tableList = parseTables(sql, fromKeyword.end());

		tableList.get(0).headComment(fromKeyword.comment());

		Table<?> lastTable = tableList.get(tableList.size() - 1);

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
			.end(endPos)
		;
	}

	/**
	 * 解析由“,”分隔的条件，如select或group by后的约束条件语句
	 */
	private static List<String> parseClauseList(String sql, int start, int end) {
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

	private static List<String> splitBy(String str, String regex, int start, int end) {
		return Arrays.stream(str.substring(start, end).split(regex))
		        .map(String::trim)
		        .collect(toList());
	}

	private static List<String> splitByAnd(String str, int start, int end) {
		return splitBy(str, "[aA][nN][dD]", start, end);
	}

	private static int findEndPos(String sql, int start) {
		int pos = findEndBracket(sql, start);
		return pos >= 0 ? pos : sql.length();
	}

	private static int findEndBracket(String sql, int start) {
		return findEndBracket(sql, start, sql.length());
	}

	private static int findEndBracket(String sql, int start, int end) {
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
	private static List<Table<?>> parseTables(String sql, int start) {
		List<Table<?>> tables = new ArrayList<Table<?>>();
		Table<?> table = parseTable(sql, start);
		tables.add(table);
		Keyword nextKeyword = findKeyWord(sql, table.end());
		if (findEndBracket(sql, table.end(), nextKeyword.start()) > 0) {
			return tables;
		}
		while (nextKeyword.contains("join")) {
			table = parseJoinTable(sql, table.end());
			if(table != null) {
				tables.add(table);
			}
			nextKeyword = findKeyWord(sql, table.end());
			if (findEndBracket(sql, table.end(), nextKeyword.start()) > 0) {
				break;	// 子查询在当前on后面结束了
			}
		}
		return tables;
	}

	private static Table<?> parseTable(String sql, int start) {
		int i = start;
		char c = sql.charAt(i);
		while (Character.isWhitespace(c) || '-' == c) {
			i++;
			c = sql.charAt(i);
			if(c == '-') {	// 处理行末的注释
				int crlfIdx = findCrlf(sql, i + 1);
				if(crlfIdx == -1) {
					break;
				}
				i = crlfIdx;
				c = sql.charAt(i);
				while(c == '\r' || c == '\n') {
					i ++;
					c = sql.charAt(i);
				}
			}
		}
		return c == '('
				? parseQueryTable(sql, i)
				: parseSimpleTable(sql, i);
	}

	/**
	 * 解析通过join语句连接的table
	 */
	private static Table<?> parseJoinTable(String sql, int start) {
		Keyword nextKeyword = findKeyWord(sql, start);
		Keyword joinKeyword = null;
		if(nextKeyword.contains("join")) {
			joinKeyword = nextKeyword;
			start = joinKeyword.end();
		}
		Table<?> table = parseTable(sql, start);
		int curPos = table.end();

		// 处理join on的情形
		if(joinKeyword != null) {
			JoinTable joinTable = new JoinTable()
				.baseTable(table.headComment(joinKeyword.comment()))
				.joinType(joinKeyword);
			nextKeyword = findKeyWord(sql, curPos);
			if(nextKeyword.is("on")) {
				Keyword onKeyword = nextKeyword;
				nextKeyword = findKeyWord(sql, onKeyword.end());
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
	private static Table<?> parseQueryTable(String sql, int start) {
		Query query = parseQuery(sql, start);
		Keyword nextKeyword = findKeyWord(sql, query.end());
		if(!nextKeyword.is("union all")) {
			int endPos = findEndBracket(sql, query.end(), nextKeyword.start());
			int nextEndPos = findEndBracket(sql, endPos + 1, nextKeyword.start());
			if(nextEndPos == -1 || nextEndPos > nextKeyword.start()) {
				nextEndPos = nextKeyword.start() - 1;
			}
			String alias = sql.substring(endPos + 1, nextEndPos).trim().split("\\s+")[0];
			QueryTable queryTable = new QueryTable(query).alias(alias)
					.start(start).end(endPos + alias.length());
			queryTable.tailComment(catchComment(sql, queryTable.end()));
			return queryTable;
		}
		// 处理union all的情形
		Keyword unionKeyword = nextKeyword;
		QueryTable table = new QueryTable(query)
				.start(start).end(unionKeyword.start() - 1);
		UnionTable unionTable = new UnionTable().addUnionTable(table);
		while(nextKeyword.is("union all")) {
			unionKeyword = nextKeyword;
			query = parseQuery(sql, unionKeyword.end());
			table = new QueryTable(query).start(unionKeyword.end() + 1).end(query.end());
			unionTable.addUnionKeyword(unionKeyword).addUnionTable(table);
			nextKeyword = findKeyWord(sql, table.end());
		}
		int endPos = findEndBracket(sql, unionTable.lastTable().end(), nextKeyword.start());	// TODO nextEndPos
		int nextEndPos = findEndBracket(sql, endPos + 1, nextKeyword.start());
		if(nextEndPos == -1 || nextEndPos > nextKeyword.start()) {
			nextEndPos = nextKeyword.start() - 1;
		}
		String alias = sql.substring(endPos + 1, nextEndPos).trim();
		return unionTable.alias(alias).start(start).end(nextEndPos);
	}

	/**
	 * 解析单张表的表名
	 */
	private static SimpleTable parseSimpleTable(String sql, int start) {
		Keyword nextKeyword = findKeyWord(sql, start);
		int end = !nextKeyword.is("null")? nextKeyword.start() - 1: sql.length();
		int nextEndPos= findEndPos(sql, start);
		if(nextEndPos < end) {
			end = nextEndPos;
		}
		String str = sql.substring(start, end).trim();
		String[] arr = str.split("\\s+");
		String tableName = arr[0];
		String alias = arr.length >= 2? arr[1]: "";
		if (arr.length >= 3 && "as".equalsIgnoreCase(alias)) {
			alias = arr[2];
		}
		return new SimpleTable(tableName, alias, start, end);
	}

	private static Keyword findKeyWord(String sql, int start) {
		return findKeyWord(sql, start, false);
	}

	private static Keyword findKeyWord(String sql, int start, boolean ignoreComment) {
		Matcher matcher = KEYWORD_PATTERN.matcher(sql);
		if(matcher.find(start)) {
			Keyword keyword = new Keyword(matcher.group(1), matcher.start(1), matcher.end(1));
			if(!ignoreComment) {	// 避免捕获comment时，递归的查找后面所有的keyword
				keyword.comment(catchComment(sql, keyword.end()));
			}
			return keyword;
		}
		return Keyword.returnNull(sql);
	}

	private static Comment catchComment(String sql, int start) {

		int crlfIdx = findCrlf(sql, start);

		if(crlfIdx == -1) {
			return null;
		}

		String str = sql.substring(start, crlfIdx);

		int commentIdx = str.indexOf("--");

		while(commentIdx != -1 && isAresEscapePattern(str)) {
			commentIdx = str.indexOf("--", commentIdx + 2);
		}

		Keyword nextKeyword = findKeyWord(sql, start, true);
		if(!nextKeyword.is("null")) {
			if(commentIdx + start > nextKeyword.start()) {
				return null;
			}
		}

		if(commentIdx == -1) {
			return null;
		}

		return new Comment()
			.content(str.substring(commentIdx))
			.start(start + commentIdx)
			.end(crlfIdx)
		;

	}

	private static int findCrlf(String sql, int start) {
		int crIdx = sql.indexOf("\r", start);
		int lfIdx = sql.indexOf("\n", start);
		int crlfIdx = Math.max(crIdx, lfIdx);
		if(crlfIdx == -1 || crlfIdx <= start ) {
			return -1;
		}
		return crlfIdx;
	}

	private static boolean isAresEscapePattern(String line) {
		return ARES_ESCAPE_PATTERN.matcher(line).find();
	}

}

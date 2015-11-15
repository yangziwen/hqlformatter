define(function(require, exports, module) {
	
	"use strict";
	
	var _ = require('underscore'),
		model = require('app/model');
	
	var keywords = [
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
	];
	
	var keywordRe = (function() {
		return new RegExp(
			'(?:^|[^_\\-0-9a-zA-Z\u4e00-\u9fa5])('
			+
			_.map(keywords, function(v){
				return v.replace(/\s/g, '\\s+?')
			}).join('|')
			+
			')(?=[^_0-9a-zA-Z\u4e00-\u9fa5])','gi'
		)
	})();
	
	var aresEscapeRe = /(HOUR|DATE|MONTH|YEAR)\s*?--\s*?\d+?\s*?\}/i;
	
	function parseSelectSql(sql) {
		return parseQuery(sql, 0);
	}
	
	/**
	 * 解析查询语句
	 */
	function parseQuery(sql, start) {
		
		var selectKeyword = findKeyword(sql, start),
			fromKeyword = findKeyword(sql, selectKeyword.getEnd());
		
		console.assert(selectKeyword.is('select'), 'failed to find select keyword');
		console.assert(fromKeyword.is('from'), 'failed to find from keyword');
		
		var tables = parseTables(sql, fromKeyword.getEnd());
		
		tables[0].headComment(fromKeyword.getComment());
		
		var lastTable = tables[tables.length - 1];
		
		var nextKeyword = findKeyword(sql, lastTable.getEnd()),
			whereKeyword = null,
			groupByKeyword = null;
		
		var endPos = findEndPos(sql, lastTable.getEnd());
		
		if(nextKeyword.is('where') && nextKeyword.getEnd() < endPos) {
			whereKeyword = nextKeyword;
			nextKeyword = findKeyword(sql, whereKeyword.getEnd());
		}
		
		if(nextKeyword.is('group by') && nextKeyword.getEnd() < endPos) {
			groupByKeyword = nextKeyword;
			nextKeyword = findKeyword(sql, groupByKeyword.getEnd());
		}
		
		if(nextKeyword.is('union all') && nextKeyword.getEnd() < endPos) {
			endPos = nextKeyword.getStart() - 1;
		}
		
		var selects = parseClauseList(sql, selectKeyword.getEnd() + 1, fromKeyword.getStart()),
			wheres = [],
			groupBys = [];
		
		if(whereKeyword != null) {
			wheres = splitByAnd(sql, whereKeyword.getEnd() + 1, groupByKeyword != null? groupByKeyword.getStart() - 1: endPos);
		}
		
		if(groupByKeyword != null) {
			groupBys = parseClauseList(sql, groupByKeyword.getEnd() + 1, endPos);
		}
		
		return model.createQuery()
			.addSelects(selects)
			.addTables(tables)
			.addWheres(wheres)
			.addGroupBys(groupBys)
			.start(selectKeyword.getStart())
			.end(endPos)
		;
	}
	
	/**
	 * 解析from后的一个或多个table，包括子查询产生的临时表
	 */
	function parseTables(sql, start) {
		var tables = [];
		var table = parseTable(sql, start);
		tables.push(table);
		var nextKeyword = findKeyword(sql, table.getEnd());
		if(findEndBracket(sql, table.getEnd(), nextKeyword.getStart()) > 0) {
			return tables;
		}
		while(nextKeyword.contains('join')) {
			table = parseJoinTable(sql, table.getEnd());
			if(table != null) {
				tables.push(table);
			}
			nextKeyword = findKeyword(sql, table.getEnd());
			if(findEndBracket(sql, table.getEnd(), nextKeyword.getStart()) > 0) {
				break;	// 子查询在当前on后面结束了
			}
		}
		return tables;
	}
	
	function parseTable(sql, start) {
		var i = start, 
			c = sql.charAt(i);
		while(/\s/.test(c) || '-' == c) {
			i++;
			c = sql.charAt(i);
			if(c == '-') {	// 处理行末的注释
				var crlfIdx = findCrlf(sql, i + 1);
				if(crlfIdx == -1) {
					break;
				}
				i = crlfIdx;
				c = sql.charAt(i);
				while(c == '\r' || c == '\n') {
					i++;
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
	function parseJoinTable(sql, start) {
		var nextKeyword = findKeyword(sql, start);
		var joinKeyword = null;
		if(nextKeyword.contains('join')) {
			joinKeyword = nextKeyword;
			start = joinKeyword.getEnd();
		}
		var table = parseTable(sql, start);
		var curPos = table.getEnd();
		
		// 处理join on的情形
		if(joinKeyword != null) {
			var joinTable = model.createJoinTable()
				.baseTable(table.headComment(joinKeyword.getComment()))
				.joinKeyword(joinKeyword);
			nextKeyword = findKeyword(sql, curPos);
			// TODO
			if(nextKeyword.is('on')) {
				var onKeyword = nextKeyword;
				nextKeyword = findKeyword(sql, onKeyword.getEnd());
				var endPos = findEndBracket(sql, start, nextKeyword.getStart());
				if(endPos == -1) {
					endPos = nextKeyword.getStart() - 1;
				}
				joinTable.addJoinOns(splitByAnd(sql, onKeyword.getEnd(), endPos))
					.start(joinKeyword.getStart()).end(endPos);
			}
			table = joinTable;
		}
		return table;
	}
	
	/**
	 * 解析子查询产生的临时表
	 */
	function parseQueryTable(sql, start) {
		var query = parseQuery(sql, start);
		var nextKeyword = findKeyword(sql, query.getEnd());
		if(!nextKeyword.is('union all')) {
			var endPos = findEndBracket(sql, query.getEnd(), nextKeyword.getStart());
			var nextEndPos = findEndBracket(sql, endPos + 1, nextKeyword.getStart());
			if(nextEndPos == -1 || nextEndPos > nextKeyword.getStart()) {
				nextEndPos = nextKeyword.getStart() - 1;
			}
			var alias = sql.substring(endPos + 1, nextEndPos).trim().split(/\s+/)[0];
			var queryTable = model.createQueryTable()
				.query(query).alias(alias)
				.start(start).end(endPos + alias.length);
			queryTable.tailComment(catchComment(sql, queryTable.getEnd()));
			return queryTable;
		}
		// 处理union all的情形
		var unionKeyword = nextKeyword;
		var table = model.createQueryTable()
			.query(query).start(start).end(unionKeyword.getStart() - 1);
		var unionTable = model.createUnionTable().addUnionTables(table);
		while(nextKeyword.is('union all')) {
			unionKeyword = nextKeyword;
			query = parseQuery(sql, unionKeyword.getEnd());
			table = model.createQueryTable().query(query)
				.start(unionKeyword.getEnd() + 1).end(query.getEnd());
			unionTable.addUnionKeywords(unionKeyword).addUnionTables(table);
			nextKeyword = findKeyword(sql, table.getEnd());
		}
		var endPos = findEndBracket(sql, unionTable.getLastTable().getEnd(), nextKeyword.getStart());
		var nextEndPos = findEndBracket(sql, endPos + 1, nextKeyword.getStart());
		if(nextEndPos == -1 || nextEndPos > nextKeyword.getStart()) {
			nextEndPos = nextKeyword.getStart() - 1;
		}
		var alias = sql.substring(endPos + 1, nextEndPos).trim();
		return unionTable.alias(alias).start(start).end(nextEndPos);
	}
	
	/**
	 * 解析单张表的表名
	 */
	function parseSimpleTable(sql, start) {
		var nextKeyword = findKeyword(sql, start);
		var end = !nextKeyword.is('null')? nextKeyword.getStart() - 1: sql.length;
		var nextEndPos = findEndPos(sql, start);
		if(nextEndPos < end) {
			end = nextEndPos;
		}
		var str = sql.substring(start, end).trim();
		var arr = str.split(/\s+/);
		var tableName = arr[0];
		var alias = arr.length >= 2? arr[1]: '';
		if(arr.length >= 3 && 'as' == alias.toLowerCase()) {
			alias = arr[2];
		}
		return model.createSimpleTable()
			.table(tableName)
			.alias(alias)
			.start(start)
			.end(end);
	}
	
	function parseClauseList(sql, start, end) {
		var str = sql.substring(start, end);
		var list = [];
		var pos = 0, bracketCnt = 0, quoteFlag = false, doubleQuoteFlag = false;
		for(var i = 0, len = str.length; i < len; i++) {
			var c = str.charAt(i);
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
				list.push(str.substring(pos, i).trim());
				pos = i + 1;
			}
		}
		list.push(str.substring(pos, str.length).trim());
		return list;
	}
	
	function splitBy(str, regex, start, end) {
		return _.map(str.substring(start, end).split(regex), function(v) {
			return v.trim();
		});
	}
	
	function splitByAnd(str, start, end) {
		return splitBy(str, /[aA][nN][dD]/, start, end);
	}
	
	function findEndPos(sql, start) {
		var pos = findEndBracket(sql, start);
		if(pos < 0) {
			pos = sql.length;
		}
		return pos;
	}
	
	function findEndBracket(sql, start, end) {
		//!end && (end = sql.length);
		if(end === undefined) {
			end = sql.length;
		}
		var cnt = 0;
		for(var i = start; i < end; i++) {
			var c = sql.charAt(i);
			if(c === '(') {
				cnt ++;
			} else if (c === ')') {
				cnt --;
			}
			if(cnt < 0) {
				return i;
			}
		}
		return -1;
	}
	
	function findKeyword(sql, start, ignoreComment) {
		keywordRe.lastIndex = start;
		var matcher = keywordRe.exec(sql);
		if(matcher == null) {
			return returnNullKeyword(sql);
		}
		var keyword = model.createKeyword()
			.name(matcher[1])
			.start(keywordRe.lastIndex - matcher[1].length)
			.end(keywordRe.lastIndex);
		if(!ignoreComment) {
			keyword.comment(catchComment(sql, keyword.getEnd()));
		}
		return keyword;
	}
	
	function catchComment(sql, start) {
		
		var crlfIdx = findCrlf(sql, start);
		
		if(crlfIdx == -1) {
			return null;
		}
		
		var str = sql.substring(start, crlfIdx),
			commentIdx = str.indexOf('--');
		
		while(commentIdx != -1 && aresEscapeRe.test(str)) {
			commentIdx = str.indexOf('--', commentIdx + 2);
		}
		
		var nextKeyword = findKeyword(sql, start, true);
		if(!nextKeyword.is('null')) {
			if(commentIdx + start > nextKeyword.getStart()) {
				return null;
			}
		}
		
		if(commentIdx == -1) {
			return null;
		}
		
		return model.createComment()
			.content(str.substring(commentIdx))
			.start(start + commentIdx)
			.end(crlfIdx);
	}
	
	function returnNullKeyword(sql) {
		var len = sql.length;
		return model.createKeyword().name('null').start(len).end(len);
	}
	
	function findCrlf(sql, start) {
		var crIdx = sql.indexOf('\r', start),
			lfIdx = sql.indexOf('\n', start),
			crlfIdx = Math.max(crIdx, lfIdx);
		if(crlfIdx == -1 || crlfIdx <= start) {
			return -1;
		}
		return crlfIdx;
	}
	
	module.exports = {
		parseSelectSql: parseSelectSql
	};
	
});
define(function(require, exports, module) {
	
	"use strict";
	
	var _ = require('underscore');
	
	var commentPrefixRe = /\s*?--(?!\d+\})/;
	
	function repeat(str, repeat) {
		repeat || (repeat = 0);
		return new Array(repeat + 1).join(str);
	}
	
	function extendClass(superClass, proto) {
		var superInstance = _.isFunction(superClass)? new superClass(): superClass;
		return _.extend(
			(function() {}).prototype, 
			superInstance, proto, 
			{
				superInstance: superInstance,
				superMethod: function(methodName) {
					var method = superInstance[methodName];
					if(!_.isFunction[method]) {
						return null;
					}
					var args = [].concat(arguments);
					args.shift();
					args.unshift(this);
					return method.apply(args);
				}
			}
		).constructor;
	}
	
	var Base = extendClass({
		start(start) {
			this._start = start;
			return this;
		},
		getStart() {
			return this._start || 0;
		},
		end(end) {
			this._end = end;
			return this;
		},
		getEnd() {
			return this._end || 0;
		},
		ensureArray: function(attr) {
			if(!_.isString(attr)) {
				throw 'Argument[attr] is required!';
			}
			if(!_.isArray(this[attr])) {
				this[attr] = [];
			}
			return this[attr];
		}
	});
	
	var AbstractTable = extendClass(Base, {
		alias(alias) {
			this._alias = alias;
			return this;
		},
		getAlias() {
			return this._alias || '';
		},
		headComment(comment) {
			this._headComment = comment;
			return this;
		},
		getHeadComment() {
			return this._headComment || null;
		},
		tailComment(comment) {
			this._tailComment = comment;
			return this;
		},
		getTailComment() {
			return this._tailComment || null;
		}
	});
		
	var SimpleTable = extendClass(AbstractTable, {
		table: function(table) {
			this._table = table;
			return this;
		},
		getTable: function() {
			return this._table || '';
		},
		toString: function() {
			return this.getTable() + (_.isEmpty(this.getAlias())? '': ' ' + this.getAlias());
		},
		format: function(indent, nestedDepth, buffer) {
			buffer.push(this.getTable());
			if(!_.isEmpty(this.getAlias())) {
				buffer.push(" ", this.getAlias());
			}
			if(this.getHeadComment()) {
				buffer.push("  ", this.getHeadComment().getContent());
			}
			return buffer;
		}
	});
	
	var JoinTable = extendClass(AbstractTable, {
		joinKeyword: function(keyword) {
			this._joinKeyword = keyword;
			return this;
		},
		getJoinKeyword: function() {
			return this._joinKeyword || null;
		},
		baseTable: function(baseTable) {
			this._baseTable = baseTable;
			return this;
		},
		getBaseTable: function() {
			return this._baseTable || null;
		},
		getTable: function() {
			var table = this._baseTable? this._baseTable.getTable(): null;
			return 'JoinTable[' + table + ']';
		},
		addJoinOns: function (list) {
			!_.isArray(list) && (list = _.toArray(arguments));
			this._joinOns = this.ensureArray('_joinOns').concat(list);
			return this;
		},
		getJoinOns: function() {
			return this.ensureArray('_joinOns');
		},
		format: function(indent, nestedDepth, buffer) {
			var baseIndent = repeat(indent, nestedDepth - 1);
			buffer.push('\n', baseIndent, this.getJoinKeyword().getName(), ' ');
			this.getBaseTable().format(indent, nestedDepth, buffer);
			
			var joinOns = this.getJoinOns();
			if(!_.isEmpty(joinOns)) {
				buffer.push('\n', baseIndent, 'ON ', joinOns[0]);
				for(var i = 1, l = joinOns.length; i < l; i++) {
					buffer.push(' AND ', joinOns[i]);
				}
			}
			return buffer;
		}
	});
	
	var UnionTable = extendClass(AbstractTable, {
		addUnionKeywords: function(list) {
			!_.isArray(list) && (list = _.toArray(arguments));
			this._unionKeywords = this.ensureArray('_unionKeywords').concat(list);
			return this;
		},
		getUnionKeywords: function() {
			return this.ensureArray('_unionKeywords');
		},
		addUnionTables: function(list) {
			!_.isArray(list) && (list = _.toArray(arguments));
			this._unionTables = this.ensureArray('_unionTables').concat(list);
			return this;
		},
		getUnionTables: function() {
			return this.ensureArray('_unionTables');
		},
		getFirstTable: function() {
			return _.first(this._unionTables);
		},
		getLastTable: function() {
			return _.last(this._unionTables);
		},
		headComment: function(comment) {
			if(this.getFirstTable()) {
				this.getFirstTable().headComment(comment);
			}
			return this;
		},
		getHeadComment: function() {
			if(!this.getFirstTable()) {
				return null;
			}
			return this.getFirstTable().getHeadComment();
		},
		format: function(indent, nestedDepth, buffer) {
			var baseIndent = repeat(indent, nestedDepth);
			var unionTables = this.getUnionTables();
			var unionKeywords = this.getUnionKeywords();
			
			unionTables[0].format(indent, nestedDepth, buffer, false, true);
			
			var size = unionTables.length;
			
			for(var i = 1; i < size; i++) {
				buffer.push('\n', baseIndent, 'UNION ALL');
				
				var comment = unionKeywords[i-1].getComment();
				if(comment != null) {
					buffer.push('  ', comment.getContent());
				}
				
				buffer.push('\n', baseIndent);
				
				unionTables[i].format(indent, nestedDepth, buffer, true, i < size - 1);
			}
			buffer.push(' ', this.getAlias(), '\n');
		}
	});
	
	var QueryTable = extendClass(AbstractTable, {
		query: function(query) {
			if(query == null) {
				return this;
			}
			this._query = query;
			this.end(query.getEnd());
			return this;
		},
		getQuery: function() {
			return this._query || null;
		},
		getTable: function() {
			var queryStr = this._query? this._query.toString(): null;
			return 'QueryTable[' + queryStr + ']'
		},
		format: function(indent, nestedDepth, buffer, omitBeginBracket, omitEndBracket) {
			if(!omitBeginBracket) {
				buffer.push('(');
			}
			if(this.getHeadComment() != null) {
				buffer.push("  ", this.getHeadComment().getContent());
			}
			buffer.push('\n', repeat(indent, nestedDepth));
			this.getQuery().format(indent, nestedDepth, buffer);
			buffer.push('\n', repeat(indent, nestedDepth - 1));
			if(!omitEndBracket) {
				buffer.push(')');
			}
			if(!_.isEmpty(this.getAlias())) {
				buffer.push(' ', this.getAlias());
			}
			if(this.getTailComment() != null) {
				buffer.push('  ', this.getTailComment().getContent());
			}
			return buffer;
		}
	});
	
	var Query = extendClass(Base, {
		addSelects: function(list) {
			!_.isArray(list) && (list = _.toArray(arguments));
			this._selects = this.ensureArray('_selects').concat(list);
			return this;
		},
		getSelects: function() {
			return this.ensureArray('_selects');
		},
		addTables: function(list) {
			!_.isArray(list) && (list = _.toArray(arguments));
			this._tables = this.ensureArray('_tables').concat(list);
			return this;
		},
		getTables: function() {
			return this.ensureArray('_tables');
		},
		addWheres: function(list) {
			!_.isArray(list) && (list = _.toArray(arguments));
			this._wheres = this.ensureArray('_wheres').concat(list);
			return this;
		},
		getWheres: function() {
			return this.ensureArray('_wheres');
		},
		addGroupBys: function(list) {
			!_.isArray(list) && (list = _.toArray(arguments));
			this._groupBys = this.ensureArray('_groupBys').concat(list);
			return this;
		},
		getGroupBys: function() {
			return this.ensureArray('_groupBys');
		},
		_formatSelect: function(indent, nestedDepth, buffer) {
			var selects = this.getSelects();
			var sep = chooseSeparator(selects, indent, nestedDepth + 1);
			buffer.push('SELECT ', selects[0]);
			for(var i = 1, l = selects.length; i < l; i++) {
				buffer.push(',');
				var clause = selects[i].trim();
				if(commentPrefixRe.test(clause)) {
					var strs = clause.split(/(\r\n)|\r|\n/);
					if(strs.length > 1) {
						buffer.push('  ', strs.shift())
						clause = strs.join('\n').trim();
					}
				}
				buffer.push(sep, clause);
			}
			return this;
		},
		_formatFrom: function(indent, nestedDepth, buffer) {
			var tables = this.getTables();
			buffer.push('\n', repeat(indent, nestedDepth), 'FROM ');
			tables[0].format(indent, nestedDepth + 1, buffer);
			for(var i = 1, l = tables.length; i < l; i++) {
				tables[i].format(indent, nestedDepth + 1, buffer);
			} 
			return this;
		},
		_formatWhere: function(indent, nestedDepth, buffer) {
			var wheres = this.getWheres();
			if(_.isEmpty(wheres)) {
				return this;
			}
			var sep = chooseSeparator(wheres, indent, nestedDepth + 1);
			buffer.push('\n', repeat(indent, nestedDepth), 'WHERE ', wheres[0]);
			for(var i = 1, l = wheres.length; i < l; i++) {
				// 支持where语句中进行单行注释，如下
				// -- AND t.manager_type = 'XXX'
				if(!wheres[i-1].endsWith('--')) {
					buffer.push(sep);
				} else {
					buffer.push(' ');
				}
				buffer.push('AND ', wheres[i]);
			}
			return this;
		},
		_formatGroupBy: function(indent, nestedDepth, buffer) {
			var groupBys = this.getGroupBys();
			if(_.isEmpty(groupBys)) {
				return this;
			}
			buffer.push('\n', repeat(indent, nestedDepth), 'GROUP BY ', groupBys.join(', '));
			return this;
		},
		format: function(indent, nestedDepth, buffer) {
			this._formatSelect(indent, nestedDepth, buffer)
				._formatFrom(indent, nestedDepth, buffer)
				._formatWhere(indent, nestedDepth, buffer)
				._formatGroupBy(indent, nestedDepth, buffer)
			;
			return buffer;
		}
	});
	
	var Keyword = extendClass(Base, {
		name: function(name) {
			this._name = name;
			return this;
		},
		getName: function() {
			return this._name || '';
		},
		comment: function(comment) {
			this._comment = comment;
			return this;
		},
		getComment: function() {
			return this._comment || null;
		},
		is: function(keyword) {
			if(_.isEmpty(keyword) || _.isEmpty(this.getName())) {
				return false;
			}
			var names = this.getName().split(/\s+/),
				keywords = keyword.trim().split(/\s+/);
			if(names.length != keywords.length) {
				return false;
			}
			for(var i = 0, l = names.length; i < l; i++) {
				if(keywords[i].toLowerCase() !== names[i].toLowerCase()) {
					return false;
				}
			}
			return true;
		},
		contains: function(keyword) {
			if(_.isEmpty(keyword) || _.isEmpty(this.getName())) {
				return false;
			}
			return this.getName().toLowerCase().indexOf(keyword.toLowerCase()) >= 0;
		}
	});
	
	var Comment = extendClass(Base, {
		content: function(content) {
			this._content = content;
			return this;
		},
		getContent: function() {
			return this._content || '';
		}
	});
	
	function chooseSeparator(list, indent, nestedDepth) {
		var sep = '\n' + repeat(indent, nestedDepth);
		if(list.length > 5) {
			return sep;
		}
		var totalLen = 0;
		for(var i = 0, l = list.length; i < l; i++) {
			if(commentPrefixRe.test(list[i])) {
				return sep;
			}
			totalLen += list[i].length;
		}
		if(totalLen < 80) {
			return ' ';
		}
		return sep;
	}
	
	module.exports = {
		createKeyword: function() {
			return new Keyword();
		},
		createComment: function() {
			return new Comment();
		},
		createQuery: function() {
			return new Query();
		},
		createSimpleTable: function() {
			return new SimpleTable();
		},
		createQueryTable: function() {
			return new QueryTable();
		},
		createJoinTable: function() {
			return new JoinTable();
		},
		createUnionTable: function() {
			return new UnionTable();
		}
	};
	
});
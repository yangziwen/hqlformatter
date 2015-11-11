define(function(require, exports, module) {
	
	"use strict";
	
	var _ = require('underscore');
	
	function parseSelectSql(sql) {
		return sql;
	}
	
	module.exports = {
		parseSelectSql: parseSelectSql
	};
	
});
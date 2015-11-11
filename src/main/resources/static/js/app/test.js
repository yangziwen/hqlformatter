define(function(require, exports, module) {
	
	// "use strict";
	
	var beans = require('app/beans');
	
	function testKeyword() {
		var assertInfo = getBeanAssertion('Assertion of keyword {} failed');
		var selectKeyword = beans.createKeyword().name('select').start(0).end(5).comment('test');
		with(console){with(selectKeyword) {
			assert(getName() === 'select', assertInfo('name'));
			assert(getStart() === 0, assertInfo('start'));
			assert(getEnd() === 5, assertInfo('end'));
			assert(getComment() === 'test', assertInfo('comment'));
		}}
	}
	
	function testSimpleTable() {
		var assertInfo = getBeanAssertion('Assertion of simpleTable {} failed');
		var comment = beans.createComment().content('test');
		var simpleTable = beans.createSimpleTable()
			.table('daily_sign').alias('ds')
			.start(100).end(110)
			.headComment(comment).tailComment(comment);
		with(console){with(simpleTable) {
			assert(getTable() === 'daily_sign', assertInfo('table'));
			assert(getAlias() === 'ds', assertInfo('alias'));
			assert(getStart() === 100, assertInfo('start'));
			assert(getEnd() === 110, assertInfo('end'));
			assert(getHeadComment() === comment, assertInfo('headComment'));
			assert(getTailComment() === comment, assertInfo('tailComment'));
		}}
	}
	
	function getBeanAssertion(clause) {
		return function(field) {
			return clause.replace(/\{\}/, field);
		}
	}
	
	module.exports = {
		run: function() {
			testKeyword();
			testSimpleTable();
		}
	};
	
});
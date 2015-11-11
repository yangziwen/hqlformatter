define(function(require, exports, module) {
	
	// "use strict";
	
	var model = require('app/model');
	
	function testKeyword() {
		var assertFailed = getFailedAssertion('Assertion of keyword {} failed');
		var selectKeyword = model.createKeyword()
			.name('select')
			.start(0).end(5)
			.comment('test');
		with(console){with(selectKeyword) {
			assert(getName() === 'select', assertFailed('name'));
			assert(getStart() === 0, assertFailed('start'));
			assert(getEnd() === 5, assertFailed('end'));
			assert(getComment() === 'test', assertFailed('comment'));
		}}
	}
	
	function testSimpleTable() {
		var assertFailed = getFailedAssertion('Assertion of simpleTable {} failed');
		var comment = model.createComment().content('test');
		var simpleTable = model.createSimpleTable()
			.table('test_tbl').alias('tt')
			.start(100).end(110)
			.headComment(comment).tailComment(comment);
		with(console){with(simpleTable) {
			assert(getTable() === 'test_tbl', assertFailed('table'));
			assert(getAlias() === 'tt', assertFailed('alias'));
			assert(getStart() === 100, assertFailed('start'));
			assert(getEnd() === 110, assertFailed('end'));
			assert(getHeadComment() === comment, assertFailed('headComment'));
			assert(getTailComment() === comment, assertFailed('tailComment'));
		}}
	}
	
	function testJoinTable() {
		var assertFailed = getFailedAssertion('Assertion of joinTable {} failed');
		var keyword = model.createKeyword().name('inner join');
		var tbl = model.createSimpleTable()
			.table('test_tbl').alias('tt');
		var joinTable = model.createJoinTable()
			.baseTable(tbl)
			.joinKeyword(keyword)
			.addJoinOns('t1.name = t2.name', 't1.day = t2.day')
			.addJoinOns(['t1.org_id = t2.org_id', 't1.type = t2.type']);
		with(console){with(joinTable){
			assert(getBaseTable() === tbl, assertFailed('baseTable'));
			assert(getJoinKeyword() === keyword, assertFailed('joinKeyword'));
			assert(getJoinOns().length === 4, assertFailed('joinOns'));
		}}
	}
	
	function getFailedAssertion(msg) {
		return function(field) {
			return msg.replace(/\{\}/, field);
		}
	}
	
	module.exports = {
		run: function() {
			testKeyword();
			testSimpleTable();
			testJoinTable();
		}
	};
	
});
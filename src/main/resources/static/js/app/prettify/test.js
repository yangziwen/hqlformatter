define(function(require, exports, module) {
	
	// "use strict";
	
	var model = require('./model');
	
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
	
	function testComment() {
		var assertFailed = getFailedAssertion('Assertion of comment {} failed');
		var comment = model.createComment()
			.content('-- 测试').start(100).end(120);
		with(console){with(comment){
			assert(getContent() === '-- 测试', assertFailed('content'));
			assert(getStart() === 100, assertFailed('start'));
			assert(getEnd() === 120, assertFailed('end'));
		}}
	}
	
	function testQuery() {
		var assertFailed = getFailedAssertion('Assertion of query {} failed');
		var query = model.createQuery()
			.addSelects('t.id', 't.name')
			.addSelects(['t.type', 't.time'])
			.addTables('tbl1 as t1', 'tbl2 as t2')
			.addTables(['tbl3 as t3', 'tbl4 as t4'])
			.addWheres('id = 1', 'name = "abc"')
			.addWheres(['type = 2', 'time > "2015-11-01"'])
			.addGroupBys('name', 'type');
		with(console){with(query){
			assert(query.getSelects().length === 4, assertFailed('selects'));
			assert(query.getTables().length === 4, assertFailed('tables'));
			assert(query.getWheres().length === 4, assertFailed('wheres'));
			assert(query.getWheres()[0] === 'id = 1', assertFailed('wheres order'));
			assert(query.getGroupBys().length === 2, assertFailed('groupBys'));
			assert(query.getGroupBys()[0] === 'name', assertFailed('groupBys order'));
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
	
	function testQueryTable() {
		var assertFailed = getFailedAssertion('Assertion of queryTable {} failed');
		var q = model.createQuery();
		var queryTable = model.createQueryTable()
			.query(q);
		with(console){with(queryTable){
			assert(getQuery() === q, assertFailed('query'));
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
	
	function testUnionTable() {
		var assertFailed = getFailedAssertion('Assertion of unoinTable {} failed');
		var keyword1 = model.createKeyword(),
			keyword2 = model.createKeyword();
		var tbl1 = model.createSimpleTable(),
			tbl2 = model.createQueryTable();
		var unionTable = model.createUnionTable()
			.addUnionKeywords(keyword1, keyword2)
			.addUnionTables(tbl1, tbl2);
		with(console){with(unionTable){
			assert(getUnionKeywords().length === 2, assertFailed('unionKeywords'));
			assert(getUnionKeywords()[0] === keyword1, assertFailed('unionKeywords order'));
			assert(getUnionTables().length === 2, assertFailed('unionTables'));
			assert(getFirstTable() === tbl1, assertFailed('first table'));
			assert(getLastTable() === tbl2, assertFailed('last table'));
		}}
	}
	
	function textExtendClass(){
		var assertFailed = getFailedAssertion('Assertion of extendClass {} failed');
		var Parent = model._extendClass(Object, {
			test: function(param) {
				return 'test parent ' + param;
			}
		});
		var Child = model._extendClass(Parent, {
			test: function(param) {
				return 'test child ' + param;
			},
			testParent: function(param) {
				return this.invokeSuperMethod('test', param);
			}
		});
		var p = new Parent(), c = new Child();
		with(console){
			assert(p.test('p') === 'test parent p', assertFailed('parent'));
			assert(c.test('c') === 'test child c', assertFailed('child'));
			assert(c.testParent('pc') === 'test parent pc', assertFailed('invokeSuperMethod'));
		}
	}
	
	function getFailedAssertion(msg) {
		return function(field) {
			return msg.replace(/\{\}/, field);
		}
	}
	
	module.exports = {
		run: function() {
			testKeyword();
			testComment();
			testQuery();
			testSimpleTable();
			testQueryTable();
			testJoinTable();
			testUnionTable();
			textExtendClass();
		}
	};
	
});
define(function(require, exports, module) {
	
	var common = require('app/common'),
		$ = require('jquery');
	
	var editor = null;
	
	function initEditor() {
		editor = CodeMirror.fromTextArea($('#J_sql')[0], {
			mode: 'text/x-mysql',
			tabMode: 'indent',
			scrollbarStyle: 'simple',
			matchBrackets: true,
			lineNumbers: true,
		});
	}
	
	function initClearBtn() {
		$('#J_clearBtn').on('click', function() {
			if(editor) {
				editor.setValue('');
			}
		});
	}
	
	function initFormatBtn() {
		$('#J_formatBtn').on('click', function() {
			if(!editor) {
				common.alertMsg('编辑区尚未初始化!');
				return;
			}
			var sql = editor.getValue();
			if(!sql || /^\s*$/.test(sql) === true) {
				common.alertMsg('请先输入sql!');
				return;
			}
			doFormatSql(sql);
		});
	}
	
	function doFormatSql(sql) {
		$.post('/sql/format', {sql: sql}, function(result) {
			if(result.code !== 0) {
				common.alertMsg(result.msg || '格式化失败，请检查sql语法!');
				return;
			}
			var sql = result.data;
			editor.setValue(sql);
		});
	}
	
	
	
	module.exports = {
		init: function() {
			initEditor();
			initClearBtn();
			initFormatBtn();
		}
	};
});
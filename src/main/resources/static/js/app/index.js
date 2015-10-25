define(function(require, exports, module) {
	
	var common = require('app/common'),
		$ = require('jquery');
	
	var editor = null;
	
	function initEditor() {
		editor = CodeMirror.fromTextArea($('#J_sql')[0], {
			mode: "text/x-mysql",
			tabMode: "indent",
			cursorHeight: 0.8,
			matchBrackets: true,
			lineNumbers: true,
			//lineWrapping: true
		});
		window.editor = editor;
	}
	
	module.exports = {
		init: function() {
			initEditor();
		}
	};
});
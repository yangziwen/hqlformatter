define(function(require, exports, module) {
	
	"use strict";
	
	var common = require('app/common'),
		$ = require('jquery'),
		moment = require('moment');
	
	var editor = null;
	
	function initEditor() {
		
		editor = CodeMirror.fromTextArea($('#J_palo')[0], {
			mode: 'text/x-mysql',
			scrollbarStyle: 'simple',
			matchBrackets: true,
			highlightSelectionMatches: {
				annotateScrollbar: true
			},
			lineNumbers: true,
			indentUnit: 4,
			extraKeys: {
				Tab: function(cm) {
					cm.replaceSelection('    ');
				}
			}
		});
		
		$('#J_palo_wrapper').addClass('hide').css({'overflow-y': 'show'});
		
		resizeCodeMirror();
		
		$(window).on('resize', resizeCodeMirror);
		
	}
	
	function resizeCodeMirror() {
		//-- 让CodeMirror对屏幕的高度自适应 --//
		var defaultWinHeight = 610,
			minCmHeight = 400;
		var $wrapper = $('#J_palo').parent();
		var cmHeight = $(window).height() - defaultWinHeight + minCmHeight;
		cmHeight = Math.max(cmHeight, minCmHeight);
		if(cmHeight != $wrapper.height()) {
			console.log(cmHeight);
			$('style').filter(function(i, v) {
				return /^\.CodeMirror\{height:\d+px;\}$/.test($(v).html())
			}).remove();
			$wrapper.height(cmHeight);
			$(document.head).append('<style>.CodeMirror{height:' + cmHeight + 'px;}</style>');
		}
	}
	
	function init() {
		initEditor();
	}
	
	module.exports = {init: init};
	
});
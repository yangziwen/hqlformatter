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
	
	function initGenPaloInfoBtn() {
		$('#J_genPaloInfoBtn').on('click', function() {
			var info = parseCreateTableSql(editor.getValue());
			$('#J_paloTaskName').text('palo2_' + info.tableName);
			$("#J_paloTaskCmd").text(generateCmd(info));
		});
	}
	
	function parseCreateTableSql(sql) {
		if (!sql) {
			common.alertMsg('请输入建表语句!');
			return;
		}
		var re = /CREATE\s+(?:EXTERNAL)?\s+TABLE\s([a-zA-Z_0-9]+)\s*\(([\w\W]+)\)\s*COMMENT\s+'(.*?)'\s+PARTITIONED\s+BY\s+\((.*?)\)\s+ROW\s+FORMAT\s+DELIMITED\s+FIELDS\s+TERMINATED\s+BY\s+'(.*?)'.+LOCATION\s+'(.*?)'/i;
		sql = sql.trim().replace(/\n/g, ' ');
		var arr = re.exec(sql);
		if (arr == null) {
			common.alertMsg('请检查输入的建表语句是否正确!');
			return;
		}
		return {
			tableName: arr[1],
			fields: $.map(arr[2].trim().split(/,\s+/), function(v) {
				return v.trim().split(/\s+/)[0];
			}),
			comment: arr[3],
			partition: arr[4].trim().split(/\s+/)[0],
			columnSep: arr[5],
			location: arr[6]
		};
	}
	
	function generateCmd(info) {
		return [
		    "cd /home/nuomi/zhuji && sh zhuji_palo2_data_upload.sh",
		    "'{DATE}'",
		    "'nuomi_crm_dp'",
		    "'" + info.tableName + "'",
		    "'" + info.location + "/dt={DATE}/*" + "'",
		    "'" + info.fields.join(',') + "'",
		    "'" + info.columnSep + "'"
		].join(' ');
	}
	
	function init() {
		initEditor();
		initGenPaloInfoBtn();
	}
	
	module.exports = {init: init};
	
});
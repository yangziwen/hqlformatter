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
		$('#J_paloTableSql').height(cmHeight - 265);
	}
	
	function initGenPaloInfoBtn() {
		$('#J_genPaloInfoBtn').on('click', function() {
			var info = parseCreateTableSql(editor.getValue());
			if (info == null) {
				return;
			}
			$('#J_paloTaskName').text('palo2_' + info.tableName);
			$('#J_paloTaskCmd').text(generateCmd(info));
			$('#J_paloTableSql').text(generateTableSql(info));
		});
	}
	
	function parseCreateTableSql(sql) {
		if (!sql) {
			common.alertMsg('请输入建表语句!');
			return;
		}
		var re = /CREATE\s+(?:EXTERNAL)?\s+TABLE\s([a-zA-Z_0-9\.]+)\s*\(([\w\W]+)\)\s*COMMENT\s+'(.*?)'\s+PARTITIONED\s+BY\s+\((.*?)\)\s+ROW\s+FORMAT\s+DELIMITED\s+FIELDS\s+TERMINATED\s+BY\s+'(.*?)'.+LOCATION\s+'(.*?)'/i;
		sql = sql.trim().replace(/\n/g, ' ');
		var arr = re.exec(sql);
		if (arr == null) {
			common.alertMsg('请检查输入的建表语句是否正确!');
			return;
		}
		return {
			tableName: arr[1].split('.').reverse()[0],
			fields: $.map(arr[2].trim().split(/,\s+/), function(v) {
				var a = v.trim().split(/\s+/);
				return {
					name: a[0],
					type: a[1],
					comment: a[3]
				};
			}),
			comment: arr[3],
			partition: arr[4].trim().split(/\s+/)[0],
			columnSep: arr[5].replace('\\u00', new Array(9).join('\\') + 'x'),
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
		    "'" + $.map(info.fields, function(v) {return v.name}).join(',') + "'",
		    "'" + info.columnSep + "'"
		].join(' ');
	}
	
	function generateTableSql(info) {
		var buff = [];
		buff.push('CREATE TABLE `' + info.tableName + '` (');
		var arr = $.grep(info.fields, function(field) {
			return field.name == 'loaddate';
		});
		if (arr.length > 0) {
			buff.push('  `loaddate` bigint COMMENT ' + arr[0].comment + ',');
		}
		for (var i = 0, len = info.fields.length; i < len; i++) {
			var field = info.fields[i];
			if (field.name == 'loaddate') {
				continue;
			}
			var type = field.type.toLowerCase() == 'string' ? 'varchar(200)' : field.type;
			buff.push('  `' + field.name + '` ' + type + ' COMMENT ' + field.comment + (i < len - 1 ? ',' : ''));
		}
		var hashField = $.grep(info.fields, function(field) {
			return field.name != 'loaddate';
		})[0];
		buff.push(') ENGINE=OLAP')
		buff.push('DISTRIBUTED BY HASH (`' + hashField.name + '`) BUCKETS 32;');
		return buff.join('\n');
	}
	
	function init() {
		initEditor();
		initGenPaloInfoBtn();
	}
	
	module.exports = {init: init};
	
});
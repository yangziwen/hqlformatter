define(function(require, exports, module) {
	
	"use strict";
	
	require('jquery.tmpl');
	var common = require('app/common'),
		$ = require('jquery');
	
	function renderTables() {
		$.get('/styleReport/list' + location.search, function(result) {
			if (result.code != 0) {
				common.alertMsg('数据加载失败!');
				return;
			}
			var reportMapping = generateReportMapping(result.data);
			var reportTblList = [];
			for (var filePath in reportMapping) {
				reportTblList.push({
					filePath: filePath,
					reports: reportMapping[filePath]
				})
			}
			$('#J_report_wrapper').empty().append($('#J_report_tbl_tmpl').tmpl(reportTblList));
		});
	}
	
	function generateReportMapping(list) {
		var reportMapping = {};
		for (var i = 0, l = list.length; i < l; i++) {
			var report = list[i];
			report.suggests = (report.suggest || '').split(',');
			if (!reportMapping[report.filePath]) {
				reportMapping[report.filePath] = [];
			}
			reportMapping[report.filePath].push(report);
		}
		return reportMapping;
	}
	
	function init() {
		renderTables();
	}
	
	module.exports = {init: init};
	
});
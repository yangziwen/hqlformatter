define(function(require, exports, module) {
	
	"use strict";
	
	var common = require('app/common'),
		$ = require('jquery'),
		graph = require('app/tableinfo/graph');
	
	var full = false;	// 是否全屏
	var $selectedTbl = null;
	
	function resize() {
		var height = common.calSuitableHeight();
		$('.tbl-list').height(height - 93);
		$('#J_canvas').height(height + (full ? 40 : 0));
	}
	
	function initTableList() {
		$.get('/table/list', function(result) {
			if (result.code !== 0) {
				common.alertMsg('表信息加载失败!');
				return;
			}
			var $list = $.map(result.data, function(v) {
				return $('<div></div>')
					.data('id', v.id)
					.data('database', v.database)
					.data('tableName', v.tableName)
					.html('&lt;' + v.database + '&gt;' + '<br/>' + v.tableName)
					.attr('title', v.database + '.' + v.tableName)
			});
			$('.tbl-list').empty().append($list).append('<div style="padding: 0px;"/>');
			$('#J_table_count').text($list.length);
		});
	}
	
	function initFullScreenBtn() {
		var fullScreenClass = 'glyphicon-fullscreen',
			resizeSmallClass = 'glyphicon-resize-small';
		$('#J_full_screen_btn').on('click', function() {
			if (!full) {
				$('#J_canvas_wrapper').addClass('full-screen');
				$(this).children()
					.removeClass(fullScreenClass)
					.addClass(resizeSmallClass)
					.attr('title', '返回');
			} else {
				$('#J_canvas_wrapper').removeClass('full-screen');
				$(this).children()
					.removeClass(resizeSmallClass)
					.addClass(fullScreenClass)
					.attr('title', '全屏');
			}
			full = !full;
			resize();
		});
	}
	
	var database = '',
		tableName = '';
	
	function initDatabaseSelect() {
		var $listWrapper = $('.tbl-list');
		var $tableCount = $('#J_table_count');
		$('#J_database').on('change', function() {
			var cnt = 0;
			database = $(this).val();
			$listWrapper.children().each(function(i, tbl) {
				var $tbl = $(tbl);
				if (!$tbl.data('id')) {
					return;
				}
				var tblName = $tbl.data('tableName'),
					db = $tbl.data('database');
				if (tblName.indexOf(tableName) >= 0 && db.indexOf(database) >= 0) {
					$tbl.show();
					cnt ++;
				} else {
					$tbl.hide();
				}
			});
			$tableCount.text(cnt);
		});
	}
	
	function initTableNameInput() {
		var $listWrapper = $('.tbl-list');
		var $tableCount = $('#J_table_count');
		$('#J_table_name').on('keyup', function() {
			var cnt = 0;
			tableName = $(this).val();
			$listWrapper.children().each(function(i, tbl) {
				var $tbl = $(tbl);
				if (!$tbl.data('id')) {
					return;
				}
				var tblName = $tbl.data('tableName'),
					db = $tbl.data('database');
				if (tblName.indexOf(tableName) >= 0 && db.indexOf(database) >= 0) {
					$tbl.show();
					cnt ++;
				} else {
					$tbl.hide();
				}
			});
			$tableCount.text(cnt);
		});
	}
	
	function initDepthSelect() {
		$('#J_depth').on('change', function() {
			if ($selectedTbl != null) {
				graph.render($selectedTbl.data('id'));
			}
		});
	}
	
	function initRenderGraph() {
		$('.tbl-list').on('click', 'div', function() {
			if ($selectedTbl != null) {
				$selectedTbl.removeClass('selected');
			}
			$selectedTbl = $(this).addClass('selected');
			graph.render($selectedTbl.data('id'));
		});
	}
	
	function init() {
		resize();
		initTableList();
		initTableNameInput();
		initDatabaseSelect();
		initDepthSelect();
		initRenderGraph();
		initFullScreenBtn();
		$(window).on('resize', resize);
	}
	
	module.exports = {init: init};
	
});
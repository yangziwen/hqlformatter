define(function(require, exports, module) {
	
	"use strict";
	
	var common = require('app/common'),
		$ = require('jquery'),
		graph = require('app/tableinfo/graph');
	
	var full = false;	// 是否全屏
	
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
		$('#J_database').on('change', function() {
			database = $(this).val();
			$listWrapper.children().each(function(i, tbl) {
				var $tbl = $(tbl);
				var tblName = $tbl.data('tableName'),
					db = $tbl.data('database');
				if (tblName.indexOf(tableName) >= 0 && db.indexOf(database) >= 0) {
					$tbl.show();
				} else {
					$tbl.hide();
				}
			});
		});
	}
	
	function initTableNameInput() {
		var $listWrapper = $('.tbl-list');
		$('#J_table_name').on('keyup', function() {
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
				} else {
					$tbl.hide();
				}
			});
		});
	}
	
	function initRenderGraph() {
		var $selected = null;
		$('.tbl-list').on('click', 'div', function() {
			if ($selected != null) {
				$selected.removeClass('selected');
			}
			$selected = $(this).addClass('selected');
			graph.render($selected.data('id'));
		});
	}
	
	function init() {
		resize();
		initTableList();
		initTableNameInput();
		initDatabaseSelect();
		initRenderGraph();
		initFullScreenBtn();
		$(window).on('resize', resize);
	}
	
	module.exports = {init: init};
	
});
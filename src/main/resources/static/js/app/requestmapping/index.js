define(function(require, exports, module) {
	
	"use strict";
	
	var common = require('app/common'),
		$ = require('jquery');
	
	function initMappingList() {
		$.get('/requestMapping/list', function(result) {
			if (result.code !== 0) {
				common.alertMsg('url路由信息加载失败!');
				return;
			}
			var $list = $.map(result.data, function(v) {
				var className = v.className.split('.').reverse()[0];
				var $tr = $('<tr>')
					.data('id', v.id)
					.data('project', v.project.toLowerCase())
					.data('url', v.requestUrl.toLowerCase())
					.data('className', className.toLowerCase())
					.data('methodName', v.methodName.toLowerCase())
					.data('authorities', v.authorities.toLowerCase());
				return $tr
					.append($('<td>').text(v.project))
					.append($('<td>').text(v.requestUrl))
					.append($('<td>').text(className).attr('title', v.className))
					.append($('<td>').text(v.methodName + (v.authorities ? '\n(' + v.authorities + ')' : '')));
			});
			$('#J_request_mapping_tbody').empty().append(renderTrs($list));
		}).then(function(result) {
			var list = $(result.data).map(function(i, v) {
				return v.project;
			}).toArray();
			initProjectSelect($.unique(list));
		});
	}
	
	var queryInfo = {};
	
	function doFilter($tr) {
		if (queryInfo.project && $tr.data('project') != queryInfo.project) {
			return false;
		}
		if (queryInfo.url) {
			try {
				var urlPattern = new RegExp(queryInfo.url);
				if (!urlPattern.test($tr.data('url'))) {
					return false;
				}
			} catch (e) {
				if ($tr.data('url').indexOf(queryInfo.url) == -1) {
					return false;
				}
			}
		}
		if (queryInfo.className && $tr.data('className').indexOf(queryInfo.className) == -1) {
			return false;
		}
		if (queryInfo.methodName && $tr.data('methodName').indexOf(queryInfo.methodName) == -1) {
			return false;
		}
		return true;
	}
	
	function renderTrs($trs) {
		var cnt = 0;
		$($trs).each(function(i, tr) {
			var $tr = $(tr);
			if (doFilter($tr)) {
				$tr.show();
				cnt ++;
			} else {
				$tr.hide();
			}
		});
		$('#J_request_mapping_tab').children('span').text(cnt);
		return $trs;
	}
	
	
	function initProjectSelect(projectList) {
		
		$('#J_project').append($.map(projectList, function(v) {
			return $('<option>').val(v).text(v);
		}));
		
		$('#J_project').on('change', function() {
			queryInfo.project = $(this).val().toLowerCase();
			renderTrs('#J_request_mapping_tbody tr');
		});
	}
	
	function initRequestUrlInput() {
		
		$('#J_request_url').on('keyup', function() {
			var val = $(this).val().toLowerCase();
			if (queryInfo.url == val) {
				return;
			}
			queryInfo.url = val;
			renderTrs('#J_request_mapping_tbody tr');
		});
		
	}
	
	function initClassNameInput() {
		
		$('#J_class_name').on('keyup', function() {
			var val = $(this).val().toLowerCase();
			if (queryInfo.className == val) {
				return;
			}
			queryInfo.className = val;
			renderTrs('#J_request_mapping_tbody tr');
		});
		
	}
	
	function initMethodNameInput() {
		
		$('#J_method_name').on('keyup', function() {
			var val = $(this).val().toLowerCase();
			if (queryInfo.methodName == val) {
				return;
			}
			queryInfo.methodName = val;
			renderTrs('#J_request_mapping_tbody tr');
		});
		
	}
	
	function resizeTable() {
		var cmHeight = common.calSuitableHeight();
		var $wrapper = $('#J_request_mapping_tbl_wrapper');
		var $table = $wrapper.children('table').eq(0);
		$wrapper.height(cmHeight - 100);
	}
	
	function initTabActive() {
		var inited = false;
		$('#J_request_mapping_tab').on('tab-active', function() {
			if (!inited) {
				initMappingList();
				inited = true;
			}
			resizeTable();
		})
	}
	
	function init() {
//		initMappingList();
		initTabActive();
		initRequestUrlInput();
		initClassNameInput();
		initMethodNameInput();
		$(window).on('resize', resizeTable);
//		$('#J_request_mapping_tab').on('tab-active', resizeTable);
	}
	
	module.exports = {init: init};
	
});
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
					.data('methodName', v.methodName.toLowerCase());
				return $tr
					.append($('<td>').text(v.project))
					.append($('<td>').text(v.requestUrl))
					.append($('<td>').text(className).attr('title', v.className))
					.append($('<td>').text(v.methodName));
			});
			$('#J_request_mapping_tbody').empty().append($list);
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
		if (queryInfo.url && $tr.data('url').indexOf(queryInfo.url) == -1) {
			return false;
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
		$($trs).each(function(i, tr) {
			var $tr = $(tr);
			if (doFilter($tr)) {
				$tr.show();
			} else {
				$tr.hide();
			}
		});
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
	
	function init() {
		initMappingList();
		initRequestUrlInput();
		initClassNameInput();
		initMethodNameInput();
	}
	
	module.exports = {init: init};
	
});
define(function(require, exports, module) {
	
	"use strict";
	
	var prettify = require('app/prettify/index'),
		palo = require('app/palo/index');
	
	function init() {
		prettify.init();
		palo.init();
		initTabs();
	}
	
	function initTabs() {
		$('#J_tabs').on('click', 'a', function() {
			var $a = $(this);
			var $li = $a.parent();
			$li.addClass('active').removeClass('cursor-pointer');
			$li.siblings().removeClass('active').addClass('cursor-pointer')
				.children('a').each(function(i, a) {
					var id = $(a).attr('id').replace('_tab', '_wrapper');
					$('#' + id).addClass('hide');
				});
			$('#' + $a.attr('id').replace('_tab', '_wrapper')).removeClass('hide');
		});
	}
	
	module.exports = {init: init};
	
});
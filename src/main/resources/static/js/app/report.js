define(function(require, exports, module) {

	"use strict";
	
	var styleReport = require('app/stylereport/index');

	function init() {
		styleReport.init();
	}

	module.exports = {init: init};

});
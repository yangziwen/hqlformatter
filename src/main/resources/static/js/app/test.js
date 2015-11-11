define(function(require, exports, module) {
	
	"use strict";
	
	var beans = require('app/beans');
	
	module.exports = {
		run: function() {
			console.log(beans.createKeyword().name('select').start(0).end(5).comment('test'));
		}
	};
	
});
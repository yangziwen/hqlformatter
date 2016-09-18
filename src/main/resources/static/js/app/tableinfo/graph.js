define(function(require, exports, module) {
	
	"use strict";
	
	var common = require('app/common'),
		$ = require('jquery');
	
	var anchors = ['Top', 'Bottom', 'Left', 'Right', 'TopRight', 'TopLeft', 'BottomRight', 'BottomLeft'];
	
	var defaultOptions = {
		Endpoint: ['Dot', {radius : 2}],
		ConnectionOverlays: [
		    ['Arrow', {
		    	location: 1,
		    	id: 'arrow',
		    	width: 10,
		    	length: 10,
		    	foldback: 0.5
		    }]
		]
	};
	
	var instance1 = null,
		instance2 = null;
	
/*	jsPlumb.bind('ready', function() {
		instance1 = jsPlumb.getInstance($.extend({
			Connector: 'Straight'
		}, defaultOptions));
		instance2 = jsPlumb.getInstance($.extend({
			Connector: 'StateMachine'
		}, defaultOptions));
	});*/
	
	function render(tableId) {
		var depth = $('#J_depth').val();
		var url = '/table/graph/' + tableId + '?depth=' + depth;
		$.get(url, function(result) {
			if (result.code != 0) {
				common.alertMsg(result.msg);
				return;
			}
			$('#J_canvas').children().remove();
			instance1 = jsPlumb.getInstance($.extend({
				Connector: 'Straight'
			}, defaultOptions));
			instance2 = jsPlumb.getInstance($.extend({
				Connector: 'StateMachine'
			}, defaultOptions));
			doRender(result.data);
		});
	}
	
	function doRender(graph) {
		var table = graph.table;
		table.style = {
			'border-width': 2,
			'border-color': '#333',
			'font-weight': 'bold'
		};
		batchCreateTableWrapper(graph);
		batchConnectTableWrapper(graph);
	}
	
	function batchCreateTableWrapper(graph) {
		for (var l = graph.derivedLayers.length,  i = l - 1; i >= 0 ; i--) {
			var layer = graph.derivedLayers[i];
			for (var j = 0; j < layer.length; j++) {
				createTableWrapper(layer[j], j * 180, (l - i - 1) * 100);
			}
		}
		
		createTableWrapper(graph.table, 0, l * 100);
		
		for (var i = 0; i < graph.dependentLayers.length; i++) {
			var layer = graph.dependentLayers[i];
			for (var j = 0; j < layer.length; j++) {
				createTableWrapper(layer[j], j * 180, (l + i + 1) * 100);
			}
		}
		
		instance1.draggable($('.tbl-wrapper'));
	}
	
	function batchConnectTableWrapper(graph) {
		
		var table = graph.table;
		
		for (var i = 0; i < graph.derivedLayers.length - 1; i++) {
			var layer = graph.derivedLayers[i];
			for (var j = 0; j < layer.length; j++) {
				var tbl = layer[j];
				for (var k = 0; k < tbl.derivedIds.length; k++) {
					connectTableWrapper(instance1, tbl.id, tbl.derivedIds[k]);
				}
				
			}
		}
		
		for (var i = 0; i < table.derivedIds.length; i++) {
			if (table.id == table.derivedIds[i]) {
				connectTableWrapper(instance2, table.id, table.derivedIds[i]);
			} else {
				connectTableWrapper(instance1, table.id, table.derivedIds[i]);
			}
		}
		
		for (var i = 0; i < table.dependentIds.length; i++) {
			connectTableWrapper(instance1, table.dependentIds[i], table.id);
		}
		
		for (var i = 0; i < graph.dependentLayers.length - 1; i++) {
			var layer = graph.dependentLayers[i];
			for (var j = 0; j < layer.length; j++) {
				var tbl = layer[j];
				for (var k = 0; k < tbl.dependentIds.length; k++) {
					connectTableWrapper(instance1, tbl.dependentIds[k], tbl.id);
				}
			}
		}
	}
	
	function createTableWrapper(tbl, left, top) {
		$('<div>')
			.data('id', tbl.id)
			.attr('id', 'tbl-wrapper-' + tbl.id)
			.attr('title', tbl.database + '.' + tbl.tableName)
			.text(tbl.tableName)
			.addClass('tbl-wrapper')
			.css($.extend({
				left: left + 10,
				top: top + 10
			}, tbl.style))
			.appendTo('#J_canvas');
	}
	
	function connectTableWrapper(instance, sourceId, targetId) {
		instance.connect({
			source: 'tbl-wrapper-' + sourceId,
			target: 'tbl-wrapper-' + targetId,
			anchor: anchors,
			paintStyle: {strokeStyle: "#5c96bc", lineWidth: 2 }
		});
	}
	
	module.exports = {
		render: render
	};
	
});
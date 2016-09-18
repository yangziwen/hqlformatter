package net.yangziwen.hqlformatter.controller;

import static net.yangziwen.hqlformatter.controller.CodeEnum.ERROR_PARAM;
import static net.yangziwen.hqlformatter.controller.CodeEnum.OK;
import static net.yangziwen.hqlformatter.controller.CodeEnum.TABLE_NOT_EXIST;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;

import net.yangziwen.hqlformatter.model.TableInfo;
import net.yangziwen.hqlformatter.repository.base.QueryMap;
import net.yangziwen.hqlformatter.service.TableService;
import net.yangziwen.hqlformatter.util.StringUtils;
import net.yangziwen.hqlformatter.util.TableCache;
import net.yangziwen.hqlformatter.util.TableCache.RelationGraph;
import net.yangziwen.hqlformatter.util.TableCache.TableWrapper;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Route;
import spark.Spark;

public class TableController {
	
	public static void init() {
		
		Spark.get("/table/list", new Route() {
			
			@Override
			public Object handle(Request request, Response response) throws Exception {
				
				response.type("application/json");
				
				List<TableInfo> list = TableService.getTableInfoList(0, Integer.MAX_VALUE, new QueryMap()
						.orderByAsc("database")
						.orderByAsc("tableName"));
				
				Map<String, Object> resultMap = OK.toResult();
				resultMap.put("data", list);
				
				return resultMap;
			}
		}, new ResponseTransformer() {
			@Override
			public String render(Object model) throws Exception {
				return JSON.toJSONString(model);
			}
		});
		
		Spark.get("/table/graph/:tableId", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				
				response.type("application/json");
				
				String tableIdParam = request.params(":tableId");
				if (StringUtils.isBlank(tableIdParam) || !Pattern.matches("^\\d+$", tableIdParam)) {
					return ERROR_PARAM.toResult();
				}
				
				int depth = Integer.MAX_VALUE;
				String depthParam = request.queryParams("depth");
				if (StringUtils.isNotBlank(depthParam) && Pattern.matches("^\\d+$", depthParam)) {
					depth = Integer.valueOf(depthParam);
				}
				
				Long tableId = Long.valueOf(tableIdParam);
				
				TableWrapper table = TableCache.getTable(tableId);
				if (table == null) {
					return TABLE_NOT_EXIST.toResult();
				}
				
				RelationGraph graph = new RelationGraph(table, depth);
				Map<String, Object> resultMap = OK.toResult();
				resultMap.put("data", graph);
				return resultMap;
			}
		}, new ResponseTransformer() {
			@Override
			public String render(Object model) throws Exception {
				return JSON.toJSONString(model);
			}
		});
		
	}

}

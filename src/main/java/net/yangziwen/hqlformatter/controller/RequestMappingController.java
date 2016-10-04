package net.yangziwen.hqlformatter.controller;

import static net.yangziwen.hqlformatter.controller.CodeEnum.OK;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import net.yangziwen.hqlformatter.model.RequestMappingInfo;
import net.yangziwen.hqlformatter.repository.base.QueryMap;
import net.yangziwen.hqlformatter.service.RequestMappingService;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Route;
import spark.Spark;

public class RequestMappingController {
	
	public static void init() {
		
		Spark.get("/requestMapping/list", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				
				response.type("application/json");
				
				List<RequestMappingInfo> list = RequestMappingService.getRequestMappingList(0,  Integer.MAX_VALUE, new QueryMap()
						.orderByAsc("project")
						.orderByAsc("className")
						.orderByAsc("requestUrl")
						);
				
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
		
	}

}

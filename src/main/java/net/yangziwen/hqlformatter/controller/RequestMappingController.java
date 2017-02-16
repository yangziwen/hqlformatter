package net.yangziwen.hqlformatter.controller;

import static net.yangziwen.hqlformatter.controller.CodeEnum.OK;

import java.util.List;

import com.alibaba.fastjson.JSON;

import net.yangziwen.hqlformatter.model.RequestMappingInfo;
import net.yangziwen.hqlformatter.repository.base.QueryMap;
import net.yangziwen.hqlformatter.service.RequestMappingService;
import spark.Spark;

public class RequestMappingController {

	public static void init() {

		Spark.get("/requestMapping/list", (request, response) -> {

			response.type("application/json");

			List<RequestMappingInfo> list = RequestMappingService.getRequestMappingList(0,  Integer.MAX_VALUE, new QueryMap()
					.orderByAsc("project")
					.orderByAsc("className")
					.orderByAsc("requestUrl")
					);

			return OK.toResult("data", list);

		}, JSON::toJSONString);

	}

}

package net.yangziwen.hqlformatter.controller;

import static net.yangziwen.hqlformatter.controller.CodeEnum.ERROR_PARAM;
import static net.yangziwen.hqlformatter.controller.CodeEnum.OK;
import static net.yangziwen.hqlformatter.controller.CodeEnum.PARSE_FAILED;

import java.util.Map;

import com.alibaba.fastjson.JSON;

import net.yangziwen.hqlformatter.format.Parser;
import net.yangziwen.hqlformatter.format.Query;
import net.yangziwen.hqlformatter.util.StringUtils;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Route;
import spark.Spark;

public class SqlController {
	
	public static void init() {
		
		Spark.post("/sql/format", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				response.type("application/json");
				String sql = request.queryParams("sql");
				if (StringUtils.isBlank(sql)) {
					return ERROR_PARAM.toResult();
				}
				try {
					Query query = Parser.parseSelectSql(sql);
					String fomattedSql = query.toString().replaceAll("(?m)^\\s*$\\n", ""); // 暂时先去除空行
					Map<String, Object> resultMap = OK.toResult();
					resultMap.put("data", fomattedSql);	
					return resultMap;
				} catch (Exception e) {
					System.err.println("failed to parse sql");
					e.printStackTrace();
					return PARSE_FAILED.toResult();
				}
			}
		}, new ResponseTransformer() {
			@Override
			public String render(Object model) throws Exception {
				return JSON.toJSONString(model);
			}
		});
		
	}
	
}

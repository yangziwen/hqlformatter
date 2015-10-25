package net.yangziwen.hqlformatter.controller;

import static net.yangziwen.hqlformatter.controller.SqlController.CodeEnum.ERROR_PARAM;
import static net.yangziwen.hqlformatter.controller.SqlController.CodeEnum.OK;
import static net.yangziwen.hqlformatter.controller.SqlController.CodeEnum.PARSE_FAILED;

import java.util.HashMap;
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
				Map<String, Object> resultMap = new HashMap<String, Object>();
				String sql = request.queryParams("sql");
				if(StringUtils.isBlank(sql)) {
					resultMap.put("code", ERROR_PARAM.code());
					resultMap.put("msg", ERROR_PARAM.msg());
					return resultMap;
				}
				sql = removeComments(sql);
				try {
					Query query = Parser.parseQuery(sql, 0);
					resultMap.put("code",  OK.code());
					resultMap.put("data", query.toString());
				} catch (Exception e) {
					System.err.println("failed to parse sql");
					resultMap.put("code", PARSE_FAILED.code());
					resultMap.put("msg", PARSE_FAILED.msg());
				}
				return resultMap;
			}
		}, new ResponseTransformer() {
			@Override
			public String render(Object model) throws Exception {
				return JSON.toJSONString(model);
			}
		});
		
	}
	
	private static String removeComments(String sql) {
		sql = sql.replaceAll("/\\*[\\w\\W]*?\\*/", "");
		StringBuilder buff = new StringBuilder();
		for(String line: sql.split("\n")) {
			int commentIdx = line.indexOf("--");
			if(commentIdx >= 0) {
				line = line.substring(0, commentIdx);
			}
			buff.append(line).append("\n");
		}
		return sql;
	}
	
	public static enum CodeEnum {
		
		OK(0, ""),
		ERROR_PARAM(1, "参数有误!"),
		PARSE_FAILED(101, "sql格式化失败!")
		;
		
		private int code;
		private String msg;
		
		private CodeEnum(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}

		public int code() {
			return code;
		}

		public String msg() {
			return msg;
		}
		
	}
	
}

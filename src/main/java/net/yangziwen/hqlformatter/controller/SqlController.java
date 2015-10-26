package net.yangziwen.hqlformatter.controller;

import static net.yangziwen.hqlformatter.controller.SqlController.CodeEnum.ERROR_PARAM;
import static net.yangziwen.hqlformatter.controller.SqlController.CodeEnum.OK;
import static net.yangziwen.hqlformatter.controller.SqlController.CodeEnum.PARSE_FAILED;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	// 应对珠玑脚本中类似{DATE--1}表达式的例外，此情形中“--”不能视为注释
	// 为什么第一个\\s{0,n}中的n大于3就会报错?
	private static Pattern ARES_ESCAPE_PATTERN = Pattern.compile("(?<=\\{\\s{0,3}(HOUR|DATE|MONTH|YEAR)\\s*?)--\\s*?\\d+?\\s*?\\}", Pattern.CASE_INSENSITIVE);
	
	public static void init() {
		
		Spark.post("/sql/format", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				response.type("application/json");
				Map<String, Object> resultMap = new HashMap<String, Object>();
				String sql = request.queryParams("sql");
				if(StringUtils.isBlank(sql)) {
					resultMap.put("code", ERROR_PARAM.code());
					resultMap.put("msg", ERROR_PARAM.msg());
					return resultMap;
				}
				sql = purifySql(sql);
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
	
	private static String purifySql(String sql) {
		sql = sql.replaceAll("/\\*[\\w\\W]*?\\*/", "");
		sql = sql.replace("\t", "    ");
		StringBuilder buff = new StringBuilder();
		for(String line: sql.split("\n")) {
			int commentIdx = line.indexOf("--");
			if(commentIdx >= 0) {
				Matcher matcher = ARES_ESCAPE_PATTERN.matcher(line);
				if(!matcher.find(commentIdx)) {
					line = line.substring(0, commentIdx);
				}
			}
			buff.append(line).append("\n");
		}
		return buff.toString() + "   ";		// 解析最后一行时有个bug，这里先偷懒解决下
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

package net.yangziwen.hqlformatter.controller;

import static net.yangziwen.hqlformatter.controller.CodeEnum.ERROR_PARAM;
import static net.yangziwen.hqlformatter.controller.CodeEnum.OK;
import static net.yangziwen.hqlformatter.controller.CodeEnum.PARSE_FAILED;

import com.alibaba.fastjson.JSON;

import net.yangziwen.hqlformatter.format.Parser;
import net.yangziwen.hqlformatter.format.Query;
import net.yangziwen.hqlformatter.util.StringUtils;
import spark.Spark;

public class SqlController {

	public static void init() {

		Spark.post("/sql/format", (request, response) -> {
			response.type("application/json");
			String sql = request.queryParams("sql");
			if (StringUtils.isBlank(sql)) {
				return ERROR_PARAM.toResult();
			}
			try {
				Query query = Parser.parseSelectSql(sql);
				String fomattedSql = query.toString().replaceAll("(?m)^\\s*$\\n", ""); // 暂时先去除空行
				return OK.toResult("data", fomattedSql);
			} catch (Exception e) {
				System.err.println("failed to parse sql");
				e.printStackTrace();
				return PARSE_FAILED.toResult();
			}
		}, JSON::toJSONString);

	}

}

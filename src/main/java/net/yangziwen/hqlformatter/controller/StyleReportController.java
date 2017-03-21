package net.yangziwen.hqlformatter.controller;

import static net.yangziwen.hqlformatter.controller.CodeEnum.OK;

import java.util.List;
import java.util.Map;

import net.yangziwen.hqlformatter.model.StyleReport;
import net.yangziwen.hqlformatter.service.StyleReportService;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class StyleReportController {

    public static void init() {

        Spark.get("/styleReport/list", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                response.type("application/json");
                List<StyleReport> list = StyleReportService.getStyleReportList();
                Map<String, Object> resultMap = OK.toResult();
                resultMap.put("data", list);
                return resultMap;
            }
        }, JsonTransformer.instance());

    }

}

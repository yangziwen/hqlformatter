package net.yangziwen.hqlformatter.controller;

import static net.yangziwen.hqlformatter.controller.CodeEnum.OK;

import java.util.List;
import java.util.Map;

import net.yangziwen.hqlformatter.model.StyleReport;
import net.yangziwen.hqlformatter.repository.base.QueryMap;
import net.yangziwen.hqlformatter.service.StyleReportService;
import net.yangziwen.hqlformatter.util.StringUtils;
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
                String excludedColors = request.queryParams("excludedColors");
                QueryMap params = new QueryMap();
                if (StringUtils.isNotBlank(excludedColors)) {
                    params.param("capture__not_in", excludedColors.split(","));
                }
                List<StyleReport> list = StyleReportService.getStyleReportList(0, 0, params);
                Map<String, Object> resultMap = OK.toResult();
                resultMap.put("data", list);
                return resultMap;
            }
        }, JsonTransformer.instance());

    }

}

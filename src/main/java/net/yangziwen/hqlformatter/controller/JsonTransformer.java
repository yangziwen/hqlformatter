package net.yangziwen.hqlformatter.controller;

import com.alibaba.fastjson.JSON;

import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {
	
	private static final JsonTransformer INSTANCE = new JsonTransformer();
	
	private JsonTransformer() {
	}

	@Override
	public String render(Object model) throws Exception {
		return JSON.toJSONString(model);
	}
	
	public static JsonTransformer instance() {
		return INSTANCE;
	}

}

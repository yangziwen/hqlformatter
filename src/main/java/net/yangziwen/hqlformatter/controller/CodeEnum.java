package net.yangziwen.hqlformatter.controller;

import java.util.HashMap;
import java.util.Map;

public enum CodeEnum {

	OK(0, ""),
	ERROR_PARAM(1, "参数有误!"),
	PARSE_FAILED(101, "sql格式化失败!"),
	TABLE_NOT_EXIST(201, "表不存在!")
	;

	public Map<String, Object> toResult() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("code", code);
		result.put("msg", msg);
		return result;
	}

	public Map<String, Object> toResult(String name, Object value) {
	    Map<String, Object> result = toResult();
	    result.put(name, value);
	    return result;
	}

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

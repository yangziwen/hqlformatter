package net.yangziwen.hqlformatter.service;

import static net.yangziwen.hqlformatter.util.DataSourceFactory.getDataSource;

import java.io.File;
import java.util.List;
import java.util.Map;

import net.yangziwen.hqlformatter.analyze.RequestMappingAnalyzer;
import net.yangziwen.hqlformatter.analyze.RequestMappingAnalyzer.Result;
import net.yangziwen.hqlformatter.model.RequestMappingInfo;
import net.yangziwen.hqlformatter.repository.RequestMappingInfoRepo;
import net.yangziwen.hqlformatter.repository.base.QueryMap;

public class RequestMappingService {
	
	private static RequestMappingInfoRepo requestMappingInfoRepo = new RequestMappingInfoRepo(getDataSource());
	
	public static List<RequestMappingInfo> getRequestMappingList(int offset, int limit, Map<String, Object> params) {
		return requestMappingInfoRepo.list(offset, limit, params);
	}
	
	public static int getRequestMappingCount(Map<String, Object> params) {
		return requestMappingInfoRepo.count(params);
	}
	
	public static void persistRequestMapping(List<RequestMappingInfo> list) {
		for (RequestMappingInfo requestMapping : list) {
			ensureRequestMappingInfoExist(requestMapping);
		}
	}
	
	private static RequestMappingInfo ensureRequestMappingInfoExist(RequestMappingInfo requestMapping) {
		RequestMappingInfo mapping = requestMappingInfoRepo.first(new QueryMap()
				.param("project", requestMapping.getProject())
				.param("className", requestMapping.getClassName())
				.param("requestUrl", requestMapping.getRequestUrl())
				.param("methodName", requestMapping.getMethodName())
				);
		if (mapping != null) {
			return mapping;
		}
		requestMappingInfoRepo.insert(requestMapping);
		return requestMapping;
	}
	
	public static void main(String[] args) {
		File file = new File("d:/workspace40/crm-dm-web");
		List<Result> list = RequestMappingAnalyzer.analyze(file);
		for (Result result : list) {
			ensureRequestMappingInfoExist(new RequestMappingInfo(result));
		}
	}

}

package net.yangziwen.hqlformatter.repository;

import javax.sql.DataSource;

import net.yangziwen.hqlformatter.model.RequestMappingInfo;
import net.yangziwen.hqlformatter.repository.base.BaseRepository;

public class RequestMappingInfoRepo extends BaseRepository<RequestMappingInfo> {

	public RequestMappingInfoRepo(DataSource dataSource) {
		super(dataSource);
	}
	
}

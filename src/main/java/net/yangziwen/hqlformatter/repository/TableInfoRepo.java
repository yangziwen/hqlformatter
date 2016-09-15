package net.yangziwen.hqlformatter.repository;

import javax.sql.DataSource;

import net.yangziwen.hqlformatter.model.TableInfo;
import net.yangziwen.hqlformatter.repository.base.BaseRepository;

public class TableInfoRepo extends BaseRepository<TableInfo> {

	public TableInfoRepo(DataSource dataSource) {
		super(dataSource);
	}

}

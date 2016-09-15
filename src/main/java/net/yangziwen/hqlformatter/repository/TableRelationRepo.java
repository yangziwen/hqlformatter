package net.yangziwen.hqlformatter.repository;

import javax.sql.DataSource;

import net.yangziwen.hqlformatter.model.TableRelation;
import net.yangziwen.hqlformatter.repository.base.BaseRepository;

public class TableRelationRepo extends BaseRepository<TableRelation> {

	public TableRelationRepo(DataSource dataSource) {
		super(dataSource);
	}

}

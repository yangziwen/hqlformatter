package net.yangziwen.hqlformatter.repository;

import javax.sql.DataSource;

import net.yangziwen.hqlformatter.model.StyleReport;
import net.yangziwen.hqlformatter.repository.base.BaseRepository;

public class StyleReportRepo extends BaseRepository<StyleReport> {

    public StyleReportRepo(DataSource dataSource) {
        super(dataSource);
    }

}

package net.yangziwen.hqlformatter.repository.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import net.yangziwen.hqlformatter.util.ReflectionUtils;
import net.yangziwen.hqlformatter.util.StringUtils;
import net.yangziwen.hqlformatter.util.Utils;

public class ReadOnlyBaseRepository<E> {
	
	protected static final Map<String, Object> EMPTY_PARAMS = Collections.emptyMap();

	protected ModelMapping<E> modelMapping = ModelMapping
			.newInstance(ReflectionUtils.<E> getSuperClassGenericType(this.getClass(), 0));

	protected Sql2o sql2o;

	protected ReadOnlyBaseRepository(DataSource dataSource) {
		sql2o = new Sql2o(dataSource);
	}
	
	public E first() {
		return first(EMPTY_PARAMS);
	}
	
	public E first(Map<String, Object> params) {
		List<E> list = list(params);
		return list != null && list.size() > 0 ? list.get(0) : null;
	}
	
	public List<E> list() {
		return list(EMPTY_PARAMS);
	}
	
	public List<E> list(Map<String, Object> params) {
		return list(0, Integer.MAX_VALUE, params);
	}

	public List<E> list(int offset, int limit, Map<String, Object> params) {
		params = new HashMap<String, Object>(params);
		StringBuilder buff = new StringBuilder();
		appendSelect(params, buff);
		appendFrom(params, buff);
		appendWhere(params, buff);
		appendGroupBy(params, buff);
		appendOrderBy(params, buff);
		appendLimit(offset, limit, buff);
		return doList(buff.toString(), params);
	}

	protected List<E> doList(String sql, Map<String, Object> params) {
		Connection conn = null;
		try {
			conn = sql2o.open();
			Query query = conn.createQuery(sql);
			for (Entry<String, Object> entry : params.entrySet()) {
				query.addParameter(entry.getKey(), entry.getValue());
			}
			return query.executeAndFetch(modelMapping.clazz);
		} finally {
			conn.close();
		}
	}
	
	public Integer count() {
		return count(EMPTY_PARAMS);
	}

	public Integer count(Map<String, Object> params) {
		params = new HashMap<String, Object>(params);
		StringBuilder buff = new StringBuilder();
		buff.append("SELECT COUNT(1) ");
		if (params.containsKey(RepoKeys.GROUP_BY)) {
			buff.append(" FROM ( SELECT 1 ");
			appendFrom(params, buff);
			appendWhere(params, buff);
			buff.append(" ) result ");
		} else {
			appendFrom(params, buff);
			appendWhere(params, buff);
		}
		return doCount(buff.toString(), params);
	}

	protected Integer doCount(String sql, Map<String, Object> params) {
		Connection conn = null;
		try {
			conn = sql2o.open();
			Query query = conn.createQuery(sql);
			for (Entry<String, Object> entry : params.entrySet()) {
				query.addParameter(entry.getKey(), entry.getValue());
			}
			return query.executeScalar(Integer.class);
		} finally {
			conn.close();
		}
	}

	// ------- 内部方法 -------- //

	protected void appendSelect(Map<String, Object> params, StringBuilder buff) {
		buff.append("SELECT ");
		int i = 0;
		for (String stmt : modelMapping.getSelectStmts()) {
			if (i++ > 0) {
				buff.append(", ");
			}
			buff.append(stmt);
		}
	}

	protected void appendFrom(Map<String, Object> params, StringBuilder buff) {
		buff.append(" FROM ").append(modelMapping.getTable(params));
	}

	@SuppressWarnings("unchecked")
	protected void appendWhere(Map<String, Object> params, StringBuilder buff) {
		List<Map<String, Object>> orParamsList = new ArrayList<Map<String, Object>>();
		List<String> keys = new ArrayList<String>(params.keySet());
		for (String key : keys) {
			if (key != null && key.toLowerCase().endsWith(RepoKeys.OR)) {
				Map<String, Object> orParams = (Map<String, Object>) params.remove(key);
				if (Utils.isNotEmpty(orParams)) {
					orParamsList.add(orParams);
				}
			}
		}
		buff.append(" WHERE ");
		appendAndConditions(params, buff);
		for (Map<String, Object> orParams : orParamsList) {
			buff.append(" AND (");
			appendOrConditions(orParams, buff);
			buff.append(")");
			params.putAll(orParams);
		}
	}

	protected void appendGroupBy(Map<String, Object> params, StringBuilder buff) {
		Object groupBy = params.remove(RepoKeys.GROUP_BY);
		if (groupBy == null) {
			return;
		}
		if (groupBy instanceof Collection) {
			buff.append(" GROUP BY ");
			int i = 0;
			for (Object obj : (Collection<?>) groupBy) {
				if (i++ > 0) {
					buff.append(", ");
				}
				buff.append(obj);
			}
			return;
		}
		buff.append(" GROUP BY ").append(groupBy);
	}

	@SuppressWarnings("unchecked")
	protected void appendOrderBy(Map<String, Object> params, StringBuilder buff) {
		Object orderBy = params.remove(RepoKeys.ORDER_BY);
		if (orderBy == null) {
			return;
		}
		if (orderBy instanceof String) {
			String str = (String) orderBy;
			if (StringUtils.isNotBlank(str)) {
				buff.append(" ORDER BY ").append(str);
			}
			return;
		}
		if (orderBy instanceof Map) {
			Map<String, Object> orderByMap = (Map<String, Object>) orderBy;
			if (Utils.isEmpty(orderByMap)) {
				return;
			}
			int i = 0;
			for (Entry<String, Object> entry : orderByMap.entrySet()) {
				String key = modelMapping.getColumnByField(entry.getKey());
				if (StringUtils.isBlank(key)) {
					key = entry.getKey();
				}
				String value = entry.getValue() != null ? entry.getValue().toString() : "";
				if (StringUtils.isBlank(key)) {
					continue;
				}
				if (i++ > 0) {
					buff.append(", ");
				} else {
					buff.append(" ORDER BY ");
				}
				buff.append(key).append(" ").append(value);
			}
		}
	}

	protected void appendLimit(int offset, int limit, StringBuilder buff) {
		if (limit <= 0) {
			return;
		}
		if (offset < 0) {
			offset = 0;
		}
		buff.append(" LIMIT ").append(limit).append(" OFFSET ").append(offset);
	}

	protected void appendAndConditions(Map<String, Object> params, StringBuilder buff) {
		buff.append(" 1 = 1 ");
		for (Entry<String, Object> entry : params.entrySet()) {
			if (RepoKeys.isRepoKey(entry.getKey())) {
				continue;
			}
			if (entry.getValue() instanceof Object[]) {
				entry.setValue(Arrays.asList((Object[]) entry.getValue()));
			}
			Condition condition = Condition.parse(entry.getKey(), modelMapping);
			if (condition == null) {
				continue;
			}
			buff.append(" AND ").append(condition.toSql());
		}
	}

	protected void appendOrConditions(Map<String, Object> params, StringBuilder buff) {
		buff.append(" 1 = 2 ");
		for (Entry<String, Object> entry : params.entrySet()) {
			if (RepoKeys.isRepoKey(entry.getKey())) {
				continue;
			}
			if (entry.getValue() instanceof Object[]) {
				entry.setValue(Arrays.asList((Object[]) entry.getValue()));
			}
			Condition condition = Condition.parse(entry.getKey(), modelMapping);
			if (condition == null) {
				continue;
			}
			buff.append(" OR ").append(condition.toSql());
		}
	}

}

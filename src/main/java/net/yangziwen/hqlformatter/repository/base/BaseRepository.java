package net.yangziwen.hqlformatter.repository.base;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.sql2o.Connection;
import org.sql2o.Query;

import net.yangziwen.hqlformatter.util.ReflectionUtils;

public class BaseRepository<E> extends ReadOnlyBaseRepository<E> {
	
	protected BaseRepository(DataSource dataSource) {
		super(dataSource);
	}
	
	private static <T> String generateUpdateSql(ModelMapping<T> modelMapping, Map<String, Object> params) {
		String idFieldName = modelMapping.getIdFieldName();
		String idColumnName = modelMapping.getIdColumn();
		Map<String, String> mappingWithoutId = modelMapping.getFieldColumnMappingWithoutIdField();
		
		StringBuilder updateBuff = new StringBuilder().append(" UPDATE ").append(modelMapping.getTable(params));
		Entry<?, ?>[] entrys = mappingWithoutId.entrySet().toArray(new Entry[]{});
		Entry<?, ?> entry = entrys[0];
		updateBuff.append(" SET ").append(entry.getValue()).append("=:").append(entry.getKey());
		for (int i = 1; i < entrys.length; i++) {
			entry = entrys[i];
			updateBuff.append(", ").append(entry.getValue()).append("=:").append(entry.getKey());
		}
		updateBuff.append(" WHERE ").append(idColumnName).append("=:").append(idFieldName);
		
		return updateBuff.toString();
	}
	
	private static <T> String generateInsertSql(ModelMapping<T> modelMapping, Map<String, Object> params) {
		Map<String, String> mappingWithoutId = modelMapping.getFieldColumnMappingWithoutIdField();
		
		StringBuilder insertBuff = new StringBuilder().append(" INSERT INTO ").append(modelMapping.getTable(params));
		Entry<?, ?>[] entrys = mappingWithoutId.entrySet().toArray(new Entry[]{});
		insertBuff.append(" ( ").append(entrys[0].getValue());
		for (int i = 1; i < entrys.length; i++) {
			insertBuff.append(", ").append(entrys[i].getValue());
		}
		insertBuff.append(" ) VALUES ( :").append(entrys[0].getKey());
		for (int i = 1; i < entrys.length; i++) {
			insertBuff.append(", :").append(entrys[i].getKey());
		}
		insertBuff.append(" ) ");
		
		return insertBuff.toString();
	}
	
	private void fillIdValue(E entity, Object id) {
		Field idField = modelMapping.getIdField();
		if (idField == null) {
			return;
		}
		if (idField.getType() == String.class) {
			ReflectionUtils.setFieldValue(entity, idField, id.toString());
		}
		else if (idField.getType() == Integer.class) {
			ReflectionUtils.setFieldValue(entity, idField, Integer.valueOf(id.toString()));
		}
		else if (idField.getType() == Long.class) {
			ReflectionUtils.setFieldValue(entity, idField, Long.valueOf(id.toString()));
		}
	}
	
	public void insert(E entity) {
		insert(entity, EMPTY_PARAMS);
	}
	
	public void insert(E entity, Map<String, Object> params) {
		Connection conn = null;
		try {
			conn = sql2o.open();
			Query query = conn.createQuery(generateInsertSql(modelMapping, params), true);
			Field idField = modelMapping.getIdField();
			for (Field field : modelMapping.getFields()) {
				if (field == null || idField == field) {
					continue;
				}
				query.addParameter(field.getName(), ReflectionUtils.getFieldValue(entity, field));
			}	
			Object id = query.executeUpdate().getKey();
			fillIdValue(entity, id);
		} finally {
			conn.close();
		}
	}
	
	public void update(E entity) {
		update(entity, EMPTY_PARAMS);
	}
	
	public void update(E entity, Map<String, Object> params) {
		Connection conn = null;
		try {
			conn = sql2o.open();
			Query query = conn.createQuery(generateUpdateSql(modelMapping, params));
			for (Field field : modelMapping.getFields()) {
				query.addParameter(field.getName(), ReflectionUtils.getFieldValue(entity, field));
			}	
			query.executeUpdate();
		} finally {
			conn.close();
		}
	}
	
	public void delete(E entity) {
		delete(entity, EMPTY_PARAMS);
	}
	
	public void delete(E entity, Map<String, Object> params) {
		deleteById(ReflectionUtils.<Long>getFieldValue(entity, modelMapping.getIdField()), params);
	}
	
	public void deleteById(Long id) {
		deleteById(id, EMPTY_PARAMS);
	}
	
	public void deleteById(Long id, Map<String, Object> params) {
		String sql = "DELETE FROM " + modelMapping.getTable(params) + " WHERE " + modelMapping.getIdColumn() + " = :id";
		Connection conn = null;
		try {
			conn = sql2o.open().createQuery(sql).addParameter("id", id).executeUpdate();
		} finally {
			conn.close();
		}
	}
	
	public void deleteByParams(Map<String, Object> params) {
		StringBuilder sqlBuff = new StringBuilder("DELETE FROM ")
				.append(modelMapping.getTable(params));
		appendWhere(params, sqlBuff);
		Connection conn = null;
		try {
			conn = sql2o.open();
			Query query = conn.createQuery(sqlBuff.toString());
			for (Entry<String, Object> entry : params.entrySet()) {
				query.addParameter(entry.getKey(), entry.getValue());
			}
			query.executeUpdate();
		} finally {
			conn.close();
		}
		
	}
	
}

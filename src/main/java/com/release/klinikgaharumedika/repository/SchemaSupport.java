package com.release.klinikgaharumedika.repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class SchemaSupport {

    private static final Map<String, Set<String>> TABLE_COLUMNS_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResultSetMetaData, Set<String>> RESULT_COLUMNS_CACHE = new ConcurrentHashMap<>();

    private SchemaSupport() {
    }

    static boolean hasColumn(Connection connection, String tableName, String columnName) throws SQLException {
        String cacheKey = buildCacheKey(connection, tableName);
        Set<String> columns = TABLE_COLUMNS_CACHE.get(cacheKey);
        if (columns != null) {
            return columns.contains(normalize(columnName));
        }

        Set<String> discoveredColumns = ConcurrentHashMap.newKeySet();
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getColumns(connection.getCatalog(), null, tableName, null)) {
            while (rs.next()) {
                discoveredColumns.add(normalize(rs.getString("COLUMN_NAME")));
            }
        }
        TABLE_COLUMNS_CACHE.put(cacheKey, discoveredColumns);
        return discoveredColumns.contains(normalize(columnName));
    }

    static boolean hasColumn(ResultSet resultSet, String columnName) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        Set<String> columns = RESULT_COLUMNS_CACHE.get(metaData);
        if (columns == null) {
            Set<String> discoveredColumns = ConcurrentHashMap.newKeySet();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                discoveredColumns.add(normalize(metaData.getColumnLabel(i)));
                discoveredColumns.add(normalize(metaData.getColumnName(i)));
            }
            RESULT_COLUMNS_CACHE.put(metaData, discoveredColumns);
            columns = discoveredColumns;
        }
        return columns.contains(normalize(columnName));
    }

    private static String buildCacheKey(Connection connection, String tableName) throws SQLException {
        String catalog = connection.getCatalog();
        return normalize(catalog != null ? catalog : "") + "." + normalize(tableName);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}

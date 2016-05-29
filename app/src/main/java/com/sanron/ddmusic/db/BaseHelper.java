package com.sanron.ddmusic.db;

import java.util.Map;

/**
 * Created by sanron on 16-5-29.
 */
public abstract class BaseHelper {
    public static final String ID = "_id";

    public static String buildCreateSql(String table, Map<String, String> columns) {
        StringBuilder sb = new StringBuilder("create table if not exists ").append(table).append("(");
        sb.append(ID).append(" integer primary key autoincrement,");
        for (Map.Entry<String, String> column : columns.entrySet()) {
            sb.append(column.getKey()).append(" ").append(column.getValue()).append(",");
        }
        sb.replace(sb.length() - 1, sb.length(), ")");
        return sb.toString();
    }

    public static String createIndexSql(String indexname, String tablename, String column) {
        StringBuffer sb = new StringBuffer();
        return sb.append("create index ").append(indexname)
                .append(" on ").append(tablename)
                .append("(").append(column).append(")")
                .toString();
    }

}

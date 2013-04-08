package edu.cmu.cs.ziy.util.cache;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.SerializationUtils;
import org.tmatesoft.sqljet.core.SqlJetException;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.ISqlJetTable;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class SqliteCacher<T extends Serializable> {

  private SqlJetDb db;

  public static enum Type {
    INTEGER, TEXT, REAL, BLOB
  };

  public static <T extends Serializable> SqliteCacher<T> create(File dbFile, Type... keyTypes)
          throws SqlJetException {
    List<String> fields = Lists.newArrayList();
    List<String> keys = Lists.newArrayList();
    fields.add("value BLOB");
    for (int i = 0; i < keyTypes.length; i++) {
      fields.add("key" + i + " " + keyTypes[i].toString() + " NOT NULL");
      keys.add("key" + i);
    }
    String createDbSql = "CREATE TABLE cache (" + Joiner.on(", ").join(fields) + ")";
    String createIndexSql = "CREATE INDEX keys ON cache (" + Joiner.on(", ").join(keys) + ")";
    return create(dbFile, createDbSql, createIndexSql);
  }

  private static <T extends Serializable> SqliteCacher<T> create(File dbFile, String createDbSql,
          String createIndexSql) throws SqlJetException {
    // create database
    SqlJetDb db = SqlJetDb.open(dbFile, true);
    db.getOptions().setAutovacuum(true);
    db.beginTransaction(SqlJetTransactionMode.WRITE);
    try {
      db.getOptions().setUserVersion(1);
    } finally {
      db.commit();
    }
    // create table and index
    try {
      db.createTable(createDbSql);
      db.createIndex(createIndexSql);
    } finally {
      db.commit();
    }
    return new SqliteCacher<T>(db);
  }

  public static <T extends Serializable> SqliteCacher<T> open(File dbFile) throws SqlJetException {
    SqlJetDb db = SqlJetDb.open(dbFile, true);
    return new SqliteCacher<T>(db);
  }

  private SqliteCacher(SqlJetDb db) {
    this.db = db;
  }

  @SuppressWarnings("unchecked")
  public List<T> lookup(Object... keys) throws SqlJetException {
    db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
    List<T> results = Lists.newArrayList();
    try {
      ISqlJetTable table = db.getTable("cache");
      ISqlJetCursor cursor = table.lookup("keys", keys);
      if (!cursor.eof()) {
        do {
          results.add((T) SerializationUtils.deserialize(cursor.getBlobAsArray("value")));
        } while (cursor.next());
      }
    } finally {
      db.commit();
    }
    return results;
  }

  public void insert(Serializable value, Object... keys) throws SqlJetException {
    db.beginTransaction(SqlJetTransactionMode.WRITE);
    try {
      ISqlJetTable table = db.getTable("cache");
      Object[] fields = new Object[keys.length + 1];
      fields[0] = SerializationUtils.serialize(value);
      for (int i = 0; i < keys.length; i++) {
        fields[i + 1] = keys[i];
      }
      table.insert(fields);
    } finally {
      db.commit();
    }
  }

  public void close() throws SqlJetException {
    db.close();
  }

}

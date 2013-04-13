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

  private ISqlJetTable table;

  private boolean isTransactionBegun;

  private int numTransactions;

  private static int FREQUENCY;

  public static void setTransactionCommitFrequency(int frequency) {
    FREQUENCY = frequency;
  }

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

  private SqliteCacher(SqlJetDb db) throws SqlJetException {
    this.db = db;
    this.table = db.getTable("cache");
    this.isTransactionBegun = false;
    this.numTransactions = 0;
  }

  @SuppressWarnings("unchecked")
  public List<T> lookup(Object... keys) throws SqlJetException {
    checkAndBeginTransaction(SqlJetTransactionMode.READ_ONLY);
    List<T> results = Lists.newArrayList();
    try {
      ISqlJetCursor cursor = table.lookup("keys", keys);
      if (!cursor.eof()) {
        do {
          results.add((T) SerializationUtils.deserialize(cursor.getBlobAsArray("value")));
        } while (cursor.next());
      }
    } finally {
      commitTransaction();
    }
    return results;
  }

  public void batchUpdate(T value, Object... keys) throws SqlJetException {
    checkAndBeginTransaction(SqlJetTransactionMode.WRITE);
    Object[] fields = new Object[keys.length + 1];
    fields[0] = SerializationUtils.serialize(value);
    for (int i = 0; i < keys.length; i++) {
      fields[i + 1] = keys[i];
    }
    ISqlJetCursor cursor = table.lookup("keys", keys);
    if (!cursor.eof()) {
      do {
        cursor.update(fields);
      } while (cursor.next());
    }
    if (numTransactions > FREQUENCY) {
      commitTransaction();
    }
  }

  public void update(T value, Object... keys) throws SqlJetException {
    checkAndBeginTransaction(SqlJetTransactionMode.WRITE);
    try {
      Object[] fields = new Object[keys.length + 1];
      fields[0] = SerializationUtils.serialize(value);
      for (int i = 0; i < keys.length; i++) {
        fields[i + 1] = keys[i];
      }
      ISqlJetCursor cursor = table.lookup("keys", keys);
      if (!cursor.eof()) {
        do {
          cursor.update(fields);
          numTransactions++;
        } while (cursor.next());
      }
    } finally {
      commitTransaction();
    }
  }

  public void batchInsert(T value, Object... keys) throws SqlJetException {
    checkAndBeginTransaction(SqlJetTransactionMode.WRITE);
    Object[] fields = new Object[keys.length + 1];
    fields[0] = SerializationUtils.serialize(value);
    for (int i = 0; i < keys.length; i++) {
      fields[i + 1] = keys[i];
    }
    table.insert(fields);
    numTransactions++;
    if (numTransactions > FREQUENCY) {
      commitTransaction();
    }
  }

  public void insert(T value, Object... keys) throws SqlJetException {
    checkAndBeginTransaction(SqlJetTransactionMode.WRITE);
    try {
      Object[] fields = new Object[keys.length + 1];
      fields[0] = SerializationUtils.serialize(value);
      for (int i = 0; i < keys.length; i++) {
        fields[i + 1] = keys[i];
      }
      table.insert(fields);
    } finally {
      commitTransaction();
    }
  }

  public void close() throws SqlJetException {
    commitTransaction();
    db.close();
  }

  private void checkAndBeginTransaction(SqlJetTransactionMode mode) throws SqlJetException {
    if (!isTransactionBegun) {
      beginTransaction(mode);
    }
  }

  private void beginTransaction(SqlJetTransactionMode mode) throws SqlJetException {
    db.beginTransaction(mode);
    isTransactionBegun = true;
  }

  private void commitTransaction() throws SqlJetException {
    db.commit();
    isTransactionBegun = false;
    numTransactions = 0;
  }

}

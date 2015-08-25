/*
 * Copyright (c) 2007 Cornel Mihaila (http://www.mihaila.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.package zutil;
 */

package com.mihaila.zorm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.mihaila.zorm.exception.ZormException;
import com.mihaila.zorm.exception.ZormInvalidFieldValueException;
import com.mihaila.zorm.exception.ZormInvalidSqlFieldValueException;
import com.mihaila.zorm.exception.ZormPersistentObjectNotFoundException;
import com.mihaila.zorm.field.ZStringField;
import com.mihaila.zorm.query.ZSelectQuery;
import com.mihaila.zutil.factory.FactoryManager;
import com.mihaila.zutil.text.EncodeUtil;

/**
 * Represents the main interface of interaction between the application and the
 * ZORM framework.
 */
public class ZSession {

	/**
	 * The initial capacity for the <code>StringBuilder</code> object used in
	 * constructing a SQL select query.
	 */
	private final static int SELECT_BUFFER_CAPACITY = 256;

	/**
	 * The initial capacity for the <code>StringBuilder</code> object used in
	 * constructing an SQL update query (or insert query).
	 */
	private final static int UPDATE_BUFFER_CAPACITY = 1024;

	/**
	 * The initial capacity for the <code>StringBuilder</code> object used in
	 * constructing an SQL delete query.
	 */
	private final static int DELETE_BUFFER_CAPACITY = 128;

	/**
	 * A number uniquely identifying each session object. This field is lazy
	 * initialized. A value of 0 means that it was not initialized.
	 */
	private int m_sessionId; // initially 0

	/**
	 * The SQL connection associated with this <code>ZSession</code>.
	 */
	private Connection m_sqlConn; // lazy loaded

	/**
	 * The SQL <code>Statement</code> object associated with this
	 * <code>ZSession</code>.
	 */
	private Statement m_sqlStatement; // lazy loaded

	/**
	 * If true, this session is closed and no operations can be done.
	 */
	private boolean m_closed; // initially false

	/**
	 * Dictionary containing the loaded <code>ZPersistent</code> objects,
	 * using as key "table:id".
	 * 
	 * This field is lazy loaded.
	 */
	private HashMap<String, ZPersistent> m_loadedObjects; // initially null

	/**
	 * If true, this session will permit the automatic fetching from database of
	 * the uninitialized fields when they are read.
	 */
	private boolean m_autoFetchingFieldsOnRead; // default false

	/**
	 * Counts the number of queries made in this session by the ZORM framework.
	 */
	private int m_numQueries; // initially 0

	/**
	 * Make the constructor to have package access because only the
	 * <code>ZManager</code> need to create </code>ZSession</code> objects.
	 * 
	 */
	ZSession() {
		setAutoFetchingFieldsOnRead(ZManager.isAutoFetchingFieldsOnRead());
	}

	/**
	 * Returns the id of this session (a number uniquely identifying each
	 * session object).
	 * 
	 * @return the id of this session
	 */
	public final int getSessionId() {
		if (m_sessionId == 0) {
			m_sessionId = ZManager.getNewSessionId();
		}
		return m_sessionId;
	}

	/**
	 * Set the session id of this object.
	 * 
	 * @param sessionId
	 */
	public void setSessionId(int sessionId) {
		m_sessionId = sessionId;
	}

	/**
	 * Get the SQL connection associated with this object. If no connection was
	 * set (via <code>setSqlConnection(Connection)</code>), than a new
	 * connection is retrieved by calling
	 * <code>ZManager.getNewConnection()</code>.
	 * 
	 * @return the SQL connection associated with this object.
	 */
	public final Connection getSqlConnection() {
		checkSessionNotClosed();
		if (m_sqlConn == null) {
			m_sqlConn = ZManager.getNewSqlConnection();
		}
		return m_sqlConn;
	}

	/**
	 * Set the SQL connection for this session. This function is used by the
	 * <code>ZManager</code>.
	 * 
	 * @param sqlConn
	 */
	final void setSqlConnection(Connection sqlConn) {
		if (m_sqlConn != null) {
			throw new ZormException("SQL Connection was already set.");
		}
		m_sqlConn = sqlConn;
	}

	/**
	 * Returns a new <code>ZSelectQuery</code> object.
	 * 
	 * @return a new <code>ZSelectQuery</code> object.
	 */
	public final ZSelectQuery getSelectQuery() {
		ZSelectQuery query = new ZSelectQuery();
		query.setSession(this);
		return query;
	}

	/**
	 * Returns the AutoFetchingFieldsOnRead state. If true, this session will
	 * permit the automatic fetching from database of the uninitialized fields
	 * when they are read.
	 * 
	 * @return the defaultAutoFetchFieldsOnRead state
	 */
	public final boolean isAutoFetchingFieldsOnRead() {
		return m_autoFetchingFieldsOnRead;
	}

	/**
	 * Set the AutoFetchingFieldsOnRead state. If true, this session will permit
	 * the automatic fetching from database of the uninitialized fields when
	 * they are read.
	 * 
	 * @param autoFetchingFieldsOnRead
	 */
	public final void setAutoFetchingFieldsOnRead(
			boolean autoFetchingFieldsOnRead) {
		m_autoFetchingFieldsOnRead = autoFetchingFieldsOnRead;
	}

	/**
	 * Returns the SQL <code>Statement</code> object associated with this
	 * <code>ZSession</code>.
	 * 
	 * @return the SQL <code>Statement</code> object associated with this
	 *         <code>ZSession</code>
	 */
	public final Statement getSqlStatement() {
		checkSessionNotClosed();
		if (m_sqlStatement == null) {
			try {
				m_sqlStatement = getSqlConnection().createStatement(
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
			} catch (Exception e) {
				throw new ZormException("Error creating SQL Statement", e);
			}
		}
		return m_sqlStatement;
	}

	/**
	 * Returns the number of queries made in this session by the ZORM framework.
	 * 
	 * @return the m_numQueries
	 */
	public final int getNumQueries() {
		return m_numQueries;
	}

	/**
	 * Clears the counter of the number of queries made in this session.
	 */
	public final void clearNumQueries() {
		m_numQueries = 0;
	}

	/**
	 * Get a new <code>ZPersistent</code> object.
	 * 
	 * @param klass
	 * @return a new <code>ZPersistent</code> object
	 */
	public final <T extends ZPersistent> T getNew(Class<T> klass) {
		T ob = FactoryManager.newInstance(klass);
		((ZPersistent) ob).setSessionBackdoor(this);
		return ob;
	}

	/**
	 * Get the <code>ZPersistent</code> object with the specified meta and id.
	 * 
	 * @param meta
	 * @param id
	 * @return the <code>ZPersistent</code> object with the specified meta and
	 *         id
	 */
	public final ZPersistent get(ZPersistentMeta meta, String id) {
		checkIfMetaHasIdField(meta);
		return getSingle(meta, id, true, null);
	}

	/**
	 * Get the <code>ZPersistent</code> object with the specified meta, id and
	 * fields loaded.
	 * 
	 * @param meta
	 * @param id
	 * @param fields
	 * @return the <code>ZPersistent</code> object with the specified meta, id
	 *         and fields loaded
	 */
	public final ZPersistent get(ZPersistentMeta meta, String id,
			ZField[] fields) {
		checkIfMetaHasIdField(meta);
		return getSingle(meta, id, true, fields);
	}

	/**
	 * Get the <code>ZPersistent</code> objects with the specified meta and
	 * ids.
	 * 
	 * @param meta
	 * @param ids
	 * @return the <code>ZPersistent</code> objects with the specified meta
	 *         and ids
	 */
	public final ZPersistent[] get(ZPersistentMeta meta, String[] ids) {
		checkIfMetaHasIdField(meta);
		// [TODO] optimize it by using "select ... in ..." queries
		ZPersistent[] obs = new ZPersistent[ids.length];
		int i = 0;
		for (String id : ids) {
			obs[i++] = getSingle(meta, id, true, null);
		}
		return obs;

	}

	/**
	 * Get the <code>ZPersistent</code> objects with the specified meta, ids
	 * and fields loaded.
	 * 
	 * @param meta
	 * @param ids
	 * @param fields
	 * @return the <code>ZPersistent</code> objects with the specified meta,
	 *         ids and fields loaded
	 */
	public final ZPersistent[] get(ZPersistentMeta meta, String[] ids,
			ZField[] fields) {
		// TODO: optimize it by using "select ... in ..." queries
		checkIfMetaHasIdField(meta);
		ZPersistent[] obs = new ZPersistent[ids.length];
		int i = 0;
		for (String id : ids) {
			obs[i++] = getSingle(meta, id, true, fields);
		}
		return obs;

	}

	/**
	 * Get the <code>ZPersistent</code> object with the specified meta and id
	 * without touching the database.
	 * 
	 * @param meta
	 * @param id
	 * @return the <code>ZPersistent</code> object with the specified meta and
	 *         id without touching the database
	 */
	public final ZPersistent getShallow(ZPersistentMeta meta, String id) {
		checkIfMetaHasIdField(meta);
		return getSingle(meta, id, false, null);
	}

	/**
	 * Get the <code>ZPersistent</code> objects with the specified meta and
	 * ids without touching the database.
	 * 
	 * @param meta
	 * @param ids
	 * @return the <code>ZPersistent</code> objects with the specified meta
	 *         and ids without touching the database
	 */
	public final ZPersistent[] getShallow(ZPersistentMeta meta, String[] ids) {
		checkIfMetaHasIdField(meta);
		ZPersistent[] obs = new ZPersistent[ids.length];
		int i = 0;
		for (String id : ids) {
			obs[i++] = getSingle(meta, id, false, null);
		}
		return obs;

	}

	/**
	 * Get the <code>ZPersistent</code> object with the specified meta by
	 * initializing it from a <code>ResultSet</code>.
	 * 
	 * @param meta
	 * @param fields
	 * @param rs
	 * @param firstFieldPos
	 *            the position of the first field in the <code>ResultSet</code>.
	 *            The other fields must follow, in order.
	 * @return the <code>ZPersistent</code> object with the specified meta by
	 *         initializing it from a <code>ResultSet</code>
	 * @throws SQLException
	 */
	public final ZPersistent getAndFetchFromResultSet(ZPersistentMeta meta,
			ZField[] fields, ResultSet rs, int firstFieldPos)
			throws SQLException {
		ZStringField idField = meta.getIdField();
		ZPersistent ob;
		int fieldPos = firstFieldPos;
		if (idField == null) {
			ob = FactoryManager.newInstance(meta.getPersistentType());
		} else {
			String id = (String) fromSqlValue(idField, rs
					.getObject(firstFieldPos));
			if (id == null) {
				return null;
			}
			ob = getShallow(meta, id);
			fieldPos++;
		}
		for (ZField field : fields) {
			setFieldFromSqlValue(ob, field, rs.getObject(fieldPos++));
		}
		return ob;
	}

	/**
	 * Dettach all the <code>ZPersistent</code> objects (not new) from this
	 * session (clear the object cache).
	 * 
	 */
	public final void dettachAll() {
		for (ZPersistent ob : getLoadedObjects().values()) {
			ob.dettach();
		}
	}

	/**
	 * Save all
	 * <code>ZPersistent<code> objects (not new) attached to this session.
	 *
	 */
	public final void saveAll() {
		for (ZPersistent ob : getLoadedObjects().values()) {
			ob.save();
		}
	}

	public final void commit() {
		if (m_sqlConn != null) {
			try {
				if (!m_sqlConn.getAutoCommit()) {
					m_sqlConn.commit();
				}
			} catch (SQLException e) {
				throw new ZormException("Error making a commit to SQL Conn", e);
			}
		}
	}

	public final void saveAllAndCommit() {
		saveAll();
		commit();
	}

	public final void rollback() {
		if (m_sqlConn != null) {
			try {
				if (!m_sqlConn.getAutoCommit()) {
					m_sqlConn.rollback();
				}
			} catch (SQLException e) {
				throw new ZormException("Error making a rollback to SQL Conn",
						e);
			}
		}
	}

	/**
	 * Returns the state of the connection (if it has been closed or not).
	 * 
	 * @return the state of the connection (if it has been closed or not)
	 */
	public final boolean isClosed() {
		return m_closed;
	}

	/**
	 * Close the session by releasing SQL resources and removing the session
	 * from the <code>ZManager</code> thread local pool.
	 */
	public final void close() {
		if (isClosed()) {
			return;
		}
		// close the SQL statement
		if (m_sqlStatement != null) {
			try {
				// [MEMO] here, m_sqlStatement.isClosed() should be used, but
				// isClosed() gives an AbstractMethodError (with mysql driver)
				m_sqlStatement.close();
			} catch (Exception e) {
				throw new ZormException("Error closing the SQL Statement", e);
			} finally {
				m_sqlStatement = null;
			}
		}
		// close the SQL connection
		if (m_sqlConn != null) {
			try {
				if (!m_sqlConn.isClosed()) {
					m_sqlConn.close();
				}
			} catch (Exception e) {
				throw new ZormException("Error closing the SQL Connection", e);
			} finally {
				m_sqlConn = null;
			}
		}
	}

	/**
	 * Log the specified query and increment the numQueries counter.
	 * 
	 * @param query
	 */
	public final void logQuery(String query) {
		m_numQueries++;
		if (ZManager.getLogger().isInfoEnabled()) {
			String message = "SESSION" + getSessionId() + ": query: \"" + query
					+ '"';
			ZManager.getLogger().info(message);
		}
	}

	/**
	 * Fetch the specified field from the database. If the field has autoFetched
	 * flag set, than all the autoFetched fields that were not inizialied are
	 * retrieved from the database. This method is used by
	 * <code>ZPersistent.getFieldValue(ZField)</code>.
	 * 
	 * @param ob
	 * @param field
	 */
	final void fetchFieldInternal(ZPersistent ob, ZField field) {
		if (isAutoFetchingFieldsOnRead()) {
			if (field.isAutoFetched()) {
				fetchFromDb(ob, getNotInitializedFields(ob, ob.getMeta()
						.getAutoFetchedFields()));
			} else {
				fetchFromDb(ob, new ZField[] { field });
			}
		} else {
			throw new ZormException("Cannot automatically fetch field: "
					+ field + " when autoFetchingFieldsOnRead mode is disabled");
		}
	}

	/**
	 * Fetch the non-initialized specified fields from the database.
	 * 
	 * @param ob
	 * @param fields
	 */
	final void fetchFieldsInternal(ZPersistent ob, ZField[] fields) {
		fetchFromDb(ob, getNotInitializedFields(ob, fields));
	}

	/**
	 * Fetch the non-initialized auto fetched fields from the database.
	 * 
	 * @param ob
	 */
	final void fetchAutoFetchedFieldsInternal(ZPersistent ob) {
		fetchFieldsInternal(ob, ob.getMeta().getAutoFetchedFields());
	}

	/**
	 * Add the specified object to cache and replace an existing object, if any.
	 * This method is used by <code>ZPersistent.attach(ZSession)</code>.
	 * 
	 * @param ob
	 */
	final void addToCache(ZPersistent ob) {
		String key = getLoadedObjectsKey(ob.getMeta(), ob.getId());
		ZPersistent oldOb = getLoadedObjects().put(key, ob);
		if (oldOb != null) {
			oldOb.setSessionBackdoor(null);
		}
	}

	/**
	 * Remove the specified <code>ZPersistent</code> object from cache. This
	 * method is used by <code>ZPersistent.dettach()</code>.
	 * 
	 * @param ob
	 */
	final void removeFromCache(ZPersistent ob) {
		ZPersistentMeta meta = ob.getMeta();
		if (meta.getIdField() == null) {
			return;
		}
		getLoadedObjects().remove(getLoadedObjectsKey(meta, ob.getId()));

	}

	/**
	 * Save the specified <code>ZPersistent</code> object in the database.
	 * This method is used by <code>ZPersistent.save()</code>.
	 * 
	 * @param ob
	 */
	final void saveInternal(ZPersistent ob) {
		if (ob.isNew()) {
			saveNewToDb(ob);
		} else {
			saveExistingToDb(ob);
		}
	}

	/**
	 * Delete the persistent object from the database (the object is specified
	 * by meta information and id). This method is used by
	 * <code>ZPersistent.delete()</code>.
	 * 
	 * @param meta
	 * @param id
	 * @return true, if the object was deleted from the database, false if it
	 *         didn't exist.
	 */
	final boolean deleteInternal(ZPersistentMeta meta, String id) {
		checkIfMetaHasIdField(meta);
		String query = constructDeleteQuery(meta, id);
		logQuery(query);
		Statement stmt = getSqlStatement();
		try {
			int rowsAffected = stmt.executeUpdate(query);
			if (rowsAffected == 0) {
				return false;
			} else if (rowsAffected > 1) {
				throw new ZormException(
						rowsAffected
								+ " were affected while trying to delete the persistent object with id field: "
								+ meta.getIdField()
								+ ". This means that the id field specified is not a primary key.");
			}
		} catch (Exception e) {
			throw new ZormException(
					"Error trying to delete persistent object with id field: "
							+ meta.getIdField() + " from SQL database", e);
		}
		getLoadedObjects().remove(getLoadedObjectsKey(meta, id));
		return true;
	}

	/**
	 * Checks that the session is not closed. If it is closed, an exception is
	 * throwned.
	 */
	private void checkSessionNotClosed() {
		if (isClosed()) {
			throw new ZormException("Session is closed.");
		}
	}

	/**
	 * Checks if the specified <code>ZPersistentMeta</code> object has an id
	 * field. If not, a <code>ZormException</code> is throwned.
	 * 
	 * @param meta
	 */
	private static void checkIfMetaHasIdField(ZPersistentMeta meta) {
		if (meta.getIdField() == null) {
			throw new ZormException(meta.toString()
					+ " meta does not have an id field specified.");
		}
	}

	/**
	 * Contructs the SQL select query using the specified table name, id and
	 * fields.
	 * 
	 * @param tableName
	 * @param id
	 * @param fields
	 * @return the SQL select query
	 */
	private static String constructSelectQuery(ZPersistentMeta meta, String id,
			ZField[] fields) {
		StringBuilder query = new StringBuilder(SELECT_BUFFER_CAPACITY);
		query.append("SELECT ");
		int i = 0;
		for (ZField field : fields) {
			if (i++ > 0) {
				query.append(", ");
			}
			query.append(field.getName());
		}
		query.append(" FROM ");
		query.append(meta.getTableName());
		query.append(" WHERE ");
		query.append(meta.getIdField().getName());
		query.append(" = ");
		EncodeUtil.writeSqlEncoded(query, id);
		return query.toString();
	}

	/**
	 * Contruct the SQL insert query using the specified
	 * <code>ZPersistent</code> object.
	 * 
	 * @param fields
	 * @return the SQL insert query
	 */
	private static String constructInsertQuery(ZPersistent ob) {
		ZField[] fields = getModifiedFields(ob);
		StringBuilder query = new StringBuilder(UPDATE_BUFFER_CAPACITY);
		query.append("INSERT INTO ");
		query.append(ob.getMeta().getTableName());
		query.append(" (");
		if (fields.length > 0) {
			int i = 0;
			for (ZField field : fields) {
				if (i++ > 0) {
					query.append(", ");
				}
				query.append(field.getName());
			}
		}
		query.append(") VALUES (");
		if (fields.length > 0) {
			int i = 0;
			for (ZField field : fields) {
				if (i++ > 0) {
					query.append(", ");
				}
				Object value = ob.getFieldValue(field);
				EncodeUtil.writeSqlEncoded(query, field.toSqlValue(value));
			}
		}
		query.append(')');
		return query.toString();
	}

	/**
	 * Contruct the SQL update query using the specified
	 * <code>ZPersistent</code> object.
	 * 
	 * @param fields
	 * @return the SQL insert query
	 */
	private static String constructUpdateQuery(ZPersistent ob) {
		ZPersistentMeta meta = ob.getMeta();
		ZStringField idField = meta.getIdField();
		ZField[] fields = getModifiedFields(ob);
		StringBuilder query = new StringBuilder(UPDATE_BUFFER_CAPACITY);
		query.append("UPDATE ");
		query.append(meta.getTableName());
		query.append(" SET ");
		int i = 0;
		for (ZField field : fields) {
			if (i++ > 0) {
				query.append(", ");
			}
			query.append(field.getName());
			query.append(" = ");
			Object value = ob.getFieldValue(field);
			if (field.isUsingSqlExprForUpdate()) {
				query.append(field.toSqlExpr(value));
			} else {
				EncodeUtil.writeSqlEncoded(query, field.toSqlValue(value));
			}
		}
		query.append(" WHERE ");
		query.append(idField.getName());
		query.append(" = ");
		EncodeUtil.writeSqlEncoded(query, idField.toSqlValue(ob
				.getFieldValue(idField)));
		return query.toString();
	}

	/**
	 * Contructs the SQL delete query using the specified table name and id.
	 * 
	 * @param tableName
	 * @param id
	 * @return the SQL delete query
	 */
	private static String constructDeleteQuery(ZPersistentMeta meta, String id) {
		StringBuilder query = new StringBuilder(DELETE_BUFFER_CAPACITY);
		query.append("DELETE FROM ");
		query.append(meta.getTableName());
		query.append(" WHERE ");
		query.append(meta.getIdField().getName());
		query.append(" = ");
		EncodeUtil.writeSqlEncoded(query, id);
		return query.toString();
	}

	/**
	 * Returns an array with the modified fields from the specified
	 * <code>ZPersistent</code> object.
	 * 
	 * @param ob
	 * @param fields
	 * @return an array with the modified fields from the specified
	 *         <code>ZPersistent</code> object
	 */
	private static ZField[] getModifiedFields(ZPersistent ob) {
		int n = 0;
		ZField[] allFields = ob.getMeta().getAllFields();
		for (ZField field : allFields) {
			if (ob.isFieldModified(field)) {
				n++;
			}
		}
		if (n == allFields.length) {
			return allFields;
		}
		ZField[] filteredFields = new ZField[n];
		n = 0;
		for (ZField field : allFields) {
			if (ob.isFieldModified(field)) {
				filteredFields[n++] = field;
			}
		}
		return filteredFields;
	}

	/**
	 * Returns an array with the fields from the specified array that are not
	 * initialized.
	 * 
	 * @param ob
	 * @param fields
	 * @return an array with the fields from the specified array that are not
	 *         initialized.
	 */
	private static ZField[] getNotInitializedFields(ZPersistent ob,
			ZField[] fields) {
		int n = 0;
		for (ZField field : fields) {
			if (!ob.isFieldInitialized(field)) {
				n++;
			}
		}
		if (n == fields.length) {
			return fields;
		}
		ZField[] filteredFields = new ZField[n];
		n = 0;
		for (ZField field : fields) {
			if (!ob.isFieldInitialized(field)) {
				filteredFields[n++] = field;
			}
		}
		return filteredFields;
	}

	/**
	 * Returns the dictionary containing the loaded <code>ZPersistent</code>
	 * objects.
	 * 
	 * @return the dictionary containing the loaded <code>ZPersistent</code>
	 *         objects.
	 */
	private HashMap<String, ZPersistent> getLoadedObjects() {
		if (m_loadedObjects == null) {
			m_loadedObjects = new HashMap<String, ZPersistent>();
		}
		return m_loadedObjects;
	}

	/**
	 * Get the key to search in the loadedObjects dictionary.
	 * 
	 * @param meta
	 * @param id
	 * @return the key to search in the loadedObjects dictionary
	 */
	private String getLoadedObjectsKey(ZPersistentMeta meta, String id) {
		return meta.getTableName() + ':' + id;
	}

	/**
	 * Get a single <code>ZPersistent</code> object.
	 * 
	 * @param meta
	 * @param id
	 * @param fetchFromDb
	 *            if true, fetch the necesary fields from the database
	 * @param fields
	 *            the fields to be fetched from the database; if null, only the
	 *            autofetched fields are retrieved from the database, and only
	 *            if the object did not exist in cache
	 * @return
	 */
	private ZPersistent getSingle(ZPersistentMeta meta, String id,
			boolean fetchFromDb, ZField[] fields) {
		String key = getLoadedObjectsKey(meta, id);
		ZPersistent ob = getLoadedObjects().get(key);
		boolean obInLoadedObjects = (ob != null);
		if (obInLoadedObjects) {
			if (fields == null) {
				return ob;
			}
		} else {
			ob = FactoryManager.newInstance(meta.getPersistentType());
			ZStringField idField = meta.getIdField();
			validateFieldValue(idField, id);
			ob.setFieldValueInternal(idField, id);
			ob.setNew(false);
		}
		if (fetchFromDb) {
			if (fields == null) {
				fetchFromDb(ob, meta.getAutoFetchedFields());
			} else {
				fetchFromDb(ob, getNotInitializedFields(ob, fields));
			}
		}
		if (!obInLoadedObjects) {
			ob.attach(this);
		}
		return ob;
	}

	/**
	 * Fetch the specified fields of the <code>ZPersistent</code> object.
	 * 
	 * @param ob
	 * @param fields
	 */
	private void fetchFromDb(ZPersistent ob, ZField[] fields) {
		if (fields.length == 0) {
			return;
		}
		ZPersistentMeta meta = ob.getMeta();
		String query = constructSelectQuery(meta, ob.getId(), fields);
		logQuery(query);
		Statement stmt = getSqlStatement();
		ResultSet rs = null;
		boolean missingObject = false;
		try {
			rs = stmt.executeQuery(query);
			if (rs.next()) {
				int i = 1;
				for (ZField field : fields) {
					setFieldFromSqlValue(ob, field, rs.getObject(i++));
				}
			} else {
				missingObject = true;
			}
			if (rs.next()) {
				throw new ZormException(
						"More than 1 row was returned while trying to to read persistent object: "
								+ ob.toString()
								+ " from SQL database. This means that the id field specified is not a primary key.");
			}
		} catch (Exception e) {
			throw new ZormException("Error trying to read persistent object: "
					+ ob.toString() + " from SQL database.", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				throw new ZormException("Error closing the SQL ResultSet.", e);
			} finally {
				rs = null;
			}
		}
		if (missingObject) {
			throw new ZormPersistentObjectNotFoundException(ob);
		}
	}

	/**
	 * Save the specified new <code>ZPersistent</code> object to database.
	 * 
	 * @param ob
	 */
	private void saveNewToDb(ZPersistent ob) {
		ZPersistentMeta meta = ob.getMeta();
		ZStringField idField = meta.getIdField();
		boolean mustReadIdField = (idField != null)
				&& idField.isAutoGenerated() && !ob.isFieldInitialized(idField);

		// check if all non-autogenerated fields are initialized
		for (ZField field : meta.getAllFields()) {
			if (!field.isAutoGenerated() && !ob.isFieldInitialized(field)) {
				throw new ZormException("Non-autogenerated field: " + field
						+ " of persistent object: " + ob
						+ " must be initialized before saving.");
			}
		}

		String query = constructInsertQuery(ob);
		logQuery(query);
		Statement stmt = getSqlStatement();
		ResultSet rs = null;
		try {
			int rowsAffected = (mustReadIdField) ? stmt.executeUpdate(query)
					: stmt.executeUpdate(query, new String[] { idField
							.getName() });
			if (rowsAffected != 1) {
				throw new ZormException(
						rowsAffected
								+ " rows were affected while trying to save the persistent object: "
								+ ob.toString() + '.');
			}
			if (mustReadIdField) {
				rs = stmt.getGeneratedKeys();
				if (rs.next()) {
					setFieldFromSqlValue(ob, idField, rs.getObject(1));

				} else {
					throw new ZormException(
							"Error while trying to process the autogenerated id field: "
									+ idField + '.');
				}
			}
		} catch (Exception e) {
			throw new ZormException(
					"Error trying to save the new persistent object: "
							+ ob.toString() + " from SQL database", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				throw new ZormException("Error closing the SQL ResultSet.", e);
			} finally {
				rs = null;
			}
		}

		// persistent objects without id fields remain in the new state
		if (idField != null) {
			ob.setNew(false);
			ob.setModified(false);
			getLoadedObjects().put(getLoadedObjectsKey(meta, ob.getId()), ob);
		}
	}

	/**
	 * Save the specified non-new <code>ZPersistent</code> object to database.
	 * 
	 * @param ob
	 */
	private void saveExistingToDb(ZPersistent ob) {
		if (!ob.isModified()) {
			return;
		}
		String query = constructUpdateQuery(ob);
		logQuery(query);
		Statement stmt = getSqlStatement();
		boolean missingObject = false;
		try {
			int rowsAffected = stmt.executeUpdate(query);
			if (rowsAffected == 0) {
				missingObject = true;
			} else if (rowsAffected > 1) {
				throw new ZormException(
						rowsAffected
								+ " were affected while trying to save the persistent object: "
								+ ob.toString()
								+ ". This means that the id field specified is not a primary key.");
			}
		} catch (Exception e) {
			throw new ZormException("Error trying to save persistent object: "
					+ ob.toString() + " from SQL database", e);
		}
		if (missingObject) {
			throw new ZormPersistentObjectNotFoundException(ob);
		}
		ob.setModified(false);
		String key = getLoadedObjectsKey(ob.getMeta(), ob.getId());
		if (getLoadedObjects().get(key) != null) {
			getLoadedObjects().put(key, ob);
		}
	}

	/**
	 * Set the specified value into the field of the ZPersistent object with
	 * validation.
	 * 
	 * @param persistent
	 * @param field
	 * @param sqlValue
	 */
	private void setFieldFromSqlValue(ZPersistent persistent, ZField field,
			Object sqlValue) {
		Object value = fromSqlValue(field, sqlValue);
		validateFieldValue(field, value);
		persistent.setFieldValueInternal(field, value);
	}

	/**
	 * Convert an object returned by the JDBC interface to an object specific to
	 * this field. If conversion fails, a ZormInvalidSqlFieldValueException is
	 * throwned.
	 * 
	 * @param field
	 * @param value
	 * @return the converted value
	 */
	private Object fromSqlValue(ZField field, Object value) {
		try {
			return field.fromSqlValue(value);
		} catch (Exception e) {
			throw new ZormInvalidSqlFieldValueException(field, value, e);
		}
	}

	/**
	 * Check if the specified value is valid for a particular field. If not, a
	 * ZormInvalidFieldValueException is throwned.
	 * 
	 * @param field
	 * @param value
	 */
	private void validateFieldValue(ZField field, Object value) {
		try {
			field.validate(value);
		} catch (Exception e) {
			throw new ZormInvalidFieldValueException(field, value, e);
		}
	}

}

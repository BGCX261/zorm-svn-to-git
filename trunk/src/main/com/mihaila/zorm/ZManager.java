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
import java.sql.DriverManager;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import com.mihaila.zorm.exception.ZormException;


/**
 * Manages the <code>ZSession</code> and <code>java.sql.Connection</code>
 * objects.
 */
public class ZManager {

	/**
	 * DataSource provider for SQL Connections
	 */
	private static DataSource m_dataSource;

	/**
	 * Contains the jdbc url used to get connections from the
	 * <code>java.sql.DriverManager</code>. This url must include the user
	 * and password to connect for the SQL database authentification.
	 */
	private static String m_jdbcUrl;

	/**
	 * If true, the sessions created by the <code>ZManager</code> will permit
	 * the automatically fetching from database of the uninitialized fields when
	 * they are read.
	 */
	private static boolean m_autoFetchingFieldsOnRead; // default false

	/**
	 * the logger instance used in all the ZORM framework.
	 */
	private static Logger m_logger = Logger.getLogger("zorm");

	/**
	 * Contains the id of the last session created. This field cannot be used to
	 * count the sessions created because not all the session "pulls" a session
	 * id (only the ones that need one).
	 */
	private static int m_lastSessionId; // initially 0;

	/**
	 * Returns the DataSource provider for SQL connections.
	 * 
	 * @return the DataSource provider for SQL connections
	 */
	public static DataSource getDataSource() {
		return m_dataSource;
	}

	/**
	 * Set the DataSource provider for SQL connections. If this value is null,
	 * than <code>java.sql.DriverManager</code> will use it for creating new
	 * SQL connections, otherwise <code>DriverManager</code> it's used.
	 * 
	 * @param dataSource
	 */
	public static void setDataSource(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	/**
	 * Returns the jdbc url used to get connections from the
	 * <code>java.sql.DriverManager</code>.
	 * 
	 * @return the jdbc url used to get connections from the
	 *         <code>java.sql.DriverManager</code>
	 */
	public static String getJdbcUrl() {
		return m_jdbcUrl;
	}

	/**
	 * Set the the jdbc url used to get connections from the
	 * <code>java.sql.DriverManager</code>. This is necessary only if the the
	 * DataSource is not set (by calling <code>setDataSource(DataSource)</code>).
	 * 
	 * @param url
	 */
	public static void setJdbcUrl(String url) {
		m_jdbcUrl = url;
	}

	/**
	 * Returns a new <code>ZSession</code> object.
	 * 
	 * @return a new <code>ZSession</code> object
	 */
	public static ZSession getNewSession() {
		return new ZSession();
	}

	/**
	 * Returns a new <code>ZSession</code> object having associated the
	 * specified SQL connection.
	 * 
	 * @param sqlConn
	 * @return a new <code>ZSession</code> object having associated the
	 *         specified SQL connection
	 */
	public static ZSession getNewSession(Connection sqlConn) {
		ZSession session = new ZSession();
		session.setSqlConnection(sqlConn);
		return session;
	}

	/**
	 * Get a new SQL connection.
	 * 
	 * @return a new SQL connection
	 */
	public static Connection getNewSqlConnection() {
		if (m_dataSource == null && m_jdbcUrl == null) {
			throw new ZormException(
					"You must set a dataSource or a jdbcUrl to the ZManager.");
		}
		Connection sqlConn;
		try {
			sqlConn = (m_dataSource == null) ? DriverManager
					.getConnection(m_jdbcUrl) : m_dataSource.getConnection();

		} catch (Exception e) {
			throw new ZormException("Error getting a SQL connection", e);
		}
		return sqlConn;
	}

	/**
	 * Returns the AutoFetchingFieldsOnRead state. If true, the sessions created
	 * by the <code>ZManager</code> will permit the automatically fetching
	 * from database of the uninitialized fields when they are read.
	 * 
	 * @return the defaultAutoFetchFieldsOnRead state
	 */
	public static boolean isAutoFetchingFieldsOnRead() {
		return m_autoFetchingFieldsOnRead;
	}

	/**
	 * Set the AutoFetchingFieldsOnRead state. If true, the sessions created by
	 * the <code>ZManager</code> will permit the automatically fetching from
	 * database of the uninitialized fields when they are read.
	 * 
	 * @param autoFetchingFieldsOnRead
	 */
	public static void setAutoFetchingFieldsOnRead(
			boolean autoFetchingFieldsOnRead) {
		m_autoFetchingFieldsOnRead = autoFetchingFieldsOnRead;
	}

	/**
	 * Returns a reference for the ZORM logger object.
	 * 
	 * @return the reference for the ZORM logger object
	 */
	public static Logger getLogger() {
		return m_logger;
	}

	/**
	 * Returns a new session id (increments the lastSessionId and returns it).
	 * 
	 * @return a new session id
	 */
	static synchronized int getNewSessionId() {
		return ++m_lastSessionId;
	}

}

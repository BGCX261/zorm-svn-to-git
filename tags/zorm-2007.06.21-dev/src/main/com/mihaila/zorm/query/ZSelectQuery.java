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

package com.mihaila.zorm.query;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.mihaila.zorm.ZField;
import com.mihaila.zorm.ZPersistentMeta;
import com.mihaila.zorm.ZSession;
import com.mihaila.zorm.exception.ZormException;
import com.mihaila.zorm.field.ZStringField;
import com.mihaila.zorm.query.Z.Join;


/**
 * Permits easy building and execution of SQL select queries.
 */
public class ZSelectQuery {

	/**
	 * Specifies the initial extra capacity of the query
	 * <code>StringBuilder</code>.
	 */
	private final static int QUERY_EXTRA_BUFFER_CAPACITY = 256;

	/**
	 * Specifies the initial capacity of the <code>StringBuilder</code> used
	 * for the SELECT and FROM clauses.
	 */
	private final static int CLAUSE_BUFFER_CAPACITY = 128;

	/**
	 * The session associated used by this query.
	 */
	private ZSession m_session; // initially false;

	/**
	 * If set to true, adding a <code>ZPersistentMeta</code> object to the
	 * SELECT clause will automatically add it to the from clause, false
	 * otherwise.
	 */
	private boolean m_autoAddToFrom = true; // default true

	/**
	 * Contains the <code>SelectInfo</code> objects used in constructing the
	 * SELECT clause and retrieving the results. This field is lazy loaded.
	 */
	private LinkedList<SelectInfo> m_selectInfoList;

	/**
	 * Contains the SELECT clause (except the items present in the
	 * m_selectInfoList. This field is lazy loaded.
	 */
	private StringBuilder m_selectClause;

	/**
	 * If set to true, the SQL query will contain the DISTINCT clause.
	 * 
	 */
	private boolean m_distinct; // default false;

	/**
	 * Contains the FROM clause. This field is not lazy-loaded because is used
	 * every time with the exception of the custom query.
	 */
	private StringBuilder m_fromClause = new StringBuilder(
			CLAUSE_BUFFER_CAPACITY);

	/**
	 * Contains the WHERE clause.
	 */
	private ZExpression m_whereExpr;

	/**
	 * Contains the ORDER BY items.
	 */
	private Object[] m_orderByArray;

	/**
	 * Contains the HAVING expression.
	 */
	private ZExpression m_havingExpr;

	/**
	 * Contains the GROUP BY items.
	 */
	private Object[] m_groupByArray;

	/**
	 * Contains the number of rows to skip from the results.
	 */
	private int m_skip; // default 0

	/**
	 * Contains the maximum number of rows to take from the results.
	 */
	private int m_take = Integer.MAX_VALUE;

	/**
	 * Contains the extra fragment to be appended at the end of the query.
	 */
	private String m_extra;

	/**
	 * Contains the query after it was executed (or it was specifyed with a
	 * <code>custom(String)</code> function call.
	 */
	private String m_query;

	/**
	 * Structure to hold information about an SELECT item specified by an
	 * <code>ZPersistentMeta</code> object.
	 */
	private final static class SelectInfo {

		/**
		 * The <code>ZPersistentMeta</code> object corresponding to this
		 * SELECT item.
		 */
		public ZPersistentMeta meta;

		/**
		 * The fields to be retrieved from the database.
		 */
		public ZField[] fields;

		/**
		 * The alias of this SELECT item.
		 */
		public String tableAlias;

	}

	/**
	 * Returns the session object used by this query.
	 * 
	 * @return the session object used by this query
	 */
	public ZSession getSession() {
		return m_session;
	}

	/**
	 * Set the session object used by this query.
	 * 
	 * @param session
	 */
	public final void setSession(ZSession session) {
		m_session = session;
	}

	/**
	 * If set to true, adding a <code>ZPersistentMeta</code> object to the
	 * SELECT clause will automatically add it to the from clause, false
	 * otherwise.
	 * 
	 * @param value
	 * @return this
	 */
	public final ZSelectQuery autoAddToFrom(boolean value) {
		m_autoAddToFrom = value;
		return this;
	}

	/**
	 * Add a <code>ZPersistentMeta</code> object to the SELECT clause with the
	 * implicit table alias and auto-fetched fields.
	 * 
	 * @param meta
	 * @return this
	 */
	public final ZSelectQuery select(ZPersistentMeta meta) {
		select(meta, meta.getAutoFetchedFields());
		return this;
	}

	/**
	 * Add a <code>ZPersistentMeta</code> object to the SELECT clause with the
	 * implicit table alias and the the specified fields to fetch from the
	 * database.
	 * 
	 * @param meta
	 * @param fields
	 * @return this
	 */
	public final ZSelectQuery select(ZPersistentMeta meta, ZField[] fields) {
		return select(meta, fields, meta.getTableAlias());
	}

	/**
	 * Add a <code>ZPersistentMeta</code> object to the SELECT clause with the
	 * specified table alias and implicit auto-fetched fields.
	 * 
	 * @param meta
	 * @param tableAlias
	 * @return this
	 */
	public final ZSelectQuery select(ZPersistentMeta meta, String tableAlias) {
		select(meta, meta.getAutoFetchedFields(), tableAlias);
		return this;
	}

	/**
	 * Add a <code>ZPersistentMeta</code> object to the SELECT clause with the
	 * specified table alias and fields to fetch from the database.
	 * 
	 * @param meta
	 * @param tableAlias
	 * @return this
	 */
	public final ZSelectQuery select(ZPersistentMeta meta, ZField[] fields,
			String tableAlias) {
		SelectInfo selectInfo = new SelectInfo();
		selectInfo.meta = meta;
		if ((fields == null) || (meta.getIdField() == null)) {
			selectInfo.fields = meta.getAutoFetchedFields();
		} else {
			selectInfo.fields = filterIdField(meta, fields);
		}
		selectInfo.tableAlias = tableAlias;
		getSelectInfoList().add(selectInfo);
		if (m_autoAddToFrom && (m_query == null)) {
			String tableName = meta.getTableName();
			if (tableAlias.equals(tableName)) {
				from(tableAlias);
			} else {
				from(tableName, tableAlias);
			}
		}
		return this;
	}

	/**
	 * Add the specified expression to the SELECT clause.
	 * 
	 * @param selectExpr
	 * @return this
	 */
	public final ZSelectQuery select(Object selectExpr) {
		StringBuilder selectClause = getSelectClause();
		if (selectClause.length() != 0) {
			selectClause.append(", ");
		}
		selectClause.append(selectExpr);
		return this;
	}

	/**
	 * Add the specified expression and alias to the SELECT clause.
	 * 
	 * @param selectExpr
	 * @param alias
	 * @return this
	 */
	public final ZSelectQuery select(Object selectExpr, Object alias) {
		StringBuilder selectClause = getSelectClause();
		if (selectClause.length() != 0) {
			selectClause.append(", ");
		}
		selectClause.append(selectExpr);
		selectClause.append(' ');
		selectClause.append(alias);
		return this;
	}

	/**
	 * If set to true, the SQL query will contain the DISTINCT clause.
	 * 
	 * @param value
	 * @return this
	 */
	public final ZSelectQuery distinct(boolean value) {
		m_distinct = value;
		return this;
	}

	/**
	 * Set the SQL query to contain the DISTINCT clause.
	 * 
	 * @return this
	 */
	public final ZSelectQuery distinct() {
		distinct(true);
		return this;
	}

	/**
	 * Add the specified <code>ZPersistentMeta</code> object to the FROM
	 * clause with the implicit table alias.
	 * 
	 * @param meta
	 * @return this
	 */
	public final ZSelectQuery from(ZPersistentMeta meta) {
		String tableName = meta.getTableName();
		String tableAlias = meta.getTableAlias();
		// it's safe to compare by alias
		if (tableAlias == tableName) {
			from(tableName);
		} else {
			from(tableName, tableAlias);
		}
		return this;
	}

	/**
	 * Add the specified <code>ZPersistentMeta</code> object to the FROM
	 * clause overwriting the implicit alias
	 * 
	 * @param meta
	 * @param tableAlias
	 * @return this
	 */
	public final ZSelectQuery from(ZPersistentMeta meta, String tableAlias) {
		from(meta.getTableName(), tableAlias);
		return this;
	}

	/**
	 * Add the expression to the from clause.
	 * 
	 * @param fromExpr
	 * @return this
	 */
	public final ZSelectQuery from(Object fromExpr) {
		if (m_fromClause.length() != 0) {
			m_fromClause.append(", ");
		}
		m_fromClause.append(fromExpr);
		return this;
	}

	/**
	 * Add the specified table name and table alias to the FROM clause.
	 * 
	 * @param tableName
	 * @param tableAlias
	 * @return this;
	 */
	public final ZSelectQuery from(Object tableName, Object tableAlias) {
		if (m_fromClause.length() != 0) {
			m_fromClause.append(", ");
		}
		m_fromClause.append(tableName);
		m_fromClause.append(' ');
		m_fromClause.append(tableAlias);
		return this;
	}

	/**
	 * Add a join expression to the FROM clause by specifying tableName and
	 * tableAlias.
	 * 
	 * @param joinKind
	 * @param tableName
	 * @param tableAlias
	 * @param field1
	 * @param field2
	 * @return this
	 */
	public final ZSelectQuery join(Join joinKind, Object tableName,
			Object tableAlias, Object field1, Object field2) {
		m_fromClause.append(' ');
		m_fromClause.append(joinKind);
		m_fromClause.append(" JOIN ");
		m_fromClause.append(tableName);
		if (tableAlias != null) {
			m_fromClause.append(' ');
			m_fromClause.append(tableAlias);
		}
		m_fromClause.append(" ON (");
		m_fromClause.append(field1);
		m_fromClause.append(" = ");
		m_fromClause.append(field2);
		m_fromClause.append(')');
		return this;
	}

	/**
	 * Add a join expression to the FROM clause by specifying a
	 * <code>ZPersistentMeta</code> object for the table object.
	 * 
	 * @param joinKind
	 * @param meta
	 * @param field1
	 * @param field2
	 * @return this
	 */
	public final ZSelectQuery join(Join joinKind, ZPersistentMeta meta,
			Object field1, Object field2) {
		String tableName = meta.getTableName();
		String tableAlias = meta.getTableAlias();
		// it's safe to compare by alias
		if (tableAlias == tableName) {
			join(joinKind, tableName, null, field1, field2);
		} else {
			join(joinKind, tableName, tableAlias, field1, field2);
		}
		return this;
	}

	/**
	 * Add the specified fragment to the WHERE clause.
	 * 
	 * @param fragment1
	 * @return this
	 */
	public final ZSelectQuery where(Object fragment1) {
		getWhereExpr().expr(fragment1);
		return this;
	}

	/**
	 * Add the specified fragments to the WHERE clause.
	 * 
	 * @param fragment1
	 * @param fragment2
	 * @return this
	 */
	public final ZSelectQuery where(Object fragment1, Object fragment2) {
		getWhereExpr().expr(fragment1, fragment2);
		return this;
	}

	/**
	 * Add the specified fragments to the WHERE clause.
	 * 
	 * @param fragment1
	 * @param fragment2
	 * @param fragment3
	 * @return this
	 */
	public final ZSelectQuery where(Object fragment1, Object fragment2,
			Object fragment3) {
		getWhereExpr().expr(fragment1, fragment2, fragment3);
		return this;
	}

	/**
	 * Add the specified fragments to the WHERE clause.
	 * 
	 * @param fragments
	 * @return this
	 */
	public final ZSelectQuery where(Object... fragments) {
		getWhereExpr().expr(fragments);
		return this;
	}

	/**
	 * Add a BETWEEN SQL expression to the the WHERE clause.
	 * 
	 * @param element
	 * @param lowLimit
	 * @param highLimit
	 * @return this
	 */
	public final ZSelectQuery whereBetween(Object element, Object lowLimit,
			Object highLimit) {
		getWhereExpr().between(element, lowLimit, highLimit);
		return this;
	}

	/**
	 * Add an IN SQL expression to the the WHERE clause.
	 * 
	 * @param element
	 * @param values
	 * @return this
	 */
	public final ZSelectQuery whereIn(Object element, Object... values) {
		getWhereExpr().in(element, values);
		return this;
	}

	/**
	 * Add the specified expressions to the GROUB BY clause.
	 * 
	 * @param groupByExprs
	 * @return this
	 */
	public final ZSelectQuery groupBy(Object... groupByExprs) {
		m_groupByArray = groupByExprs;
		return this;
	}

	/**
	 * Add the specified fragment to the HAVING clause.
	 * 
	 * @param fragment1
	 * @return this
	 */
	public final ZSelectQuery having(Object fragment1) {
		getHavingExpr().expr(fragment1);
		return this;
	}

	/**
	 * Add the specified fragments to the HAVING clause.
	 * 
	 * @param fragment1
	 * @param fragment2
	 * @return this
	 */
	public final ZSelectQuery having(Object fragment1, Object fragment2) {
		getHavingExpr().expr(fragment1, fragment2);
		return this;
	}

	/**
	 * Add the specified fragments to the HAVING clause.
	 * 
	 * @param fragment1
	 * @param fragment2
	 * @param fragment3
	 * @return this
	 */
	public final ZSelectQuery having(Object fragment1, Object fragment2,
			Object fragment3) {
		getHavingExpr().expr(fragment1, fragment2, fragment3);
		return this;
	}

	/**
	 * Add the specified fragments to the HAVING clause.
	 * 
	 * @param fragments
	 * @return this
	 */
	public final ZSelectQuery having(Object... fragments) {
		getHavingExpr().expr(fragments);
		return this;
	}

	/**
	 * Add the specified expressions to the ORDER BY clause.
	 * 
	 * @param sortByExprs
	 * @return this
	 */
	public final ZSelectQuery orderBy(Object... sortByExprs) {
		m_orderByArray = sortByExprs;
		return this;
	}

	/**
	 * Specify the number of rows to skip from the results.
	 * 
	 * @param value
	 * @return this
	 */
	public final ZSelectQuery skip(int value) {
		m_skip = value;
		return this;
	}

	/**
	 * Specify the maximum number of rows to take from the results.
	 * 
	 * @param value
	 * @return this
	 */
	public final ZSelectQuery take(int value) {
		m_take = value;
		return this;
	}

	/**
	 * Specify an extra fragment to be appended at the end of the query.
	 * 
	 * @param extraExpr
	 * @return this
	 */
	public final ZSelectQuery extra(String extraExpr) {
		m_extra = extraExpr;
		return this;
	}

	/**
	 * Specify a complete custom query to be used to access the database. The
	 * select clause can be used to specify <code>ZPersistentMeta</code>
	 * objects.
	 * 
	 * @param query
	 * @return this
	 */
	public final ZSelectQuery customQuery(String query) {
		m_query = query;
		return this;
	}

	/**
	 * Clear all information about the current query.
	 * 
	 * @return this
	 */
	public final ZSelectQuery clear() {
		clearSelect();
		clearFrom();
		clearWhere();
		clearHaving();
		m_distinct = false;
		m_groupByArray = null;
		m_orderByArray = null;
		m_extra = null;
		m_query = null;
		m_skip = 0;
		m_take = Integer.MAX_VALUE;
		return this;
	}

	/**
	 * Clear the SELECT clause.
	 * 
	 * @return this
	 */
	public final ZSelectQuery clearSelect() {
		if (m_selectInfoList != null) {
			m_selectInfoList.clear();
		}
		if (m_selectClause != null) {
			m_selectClause.delete(0, m_selectClause.length());
		}
		return this;
	}

	/**
	 * Clear the FROM clause.
	 * 
	 * @return this
	 */
	public final ZSelectQuery clearFrom() {
		m_fromClause.delete(0, m_fromClause.length());
		return this;
	}

	/**
	 * Clear the WHERE clause.
	 * 
	 * @return this
	 */
	public final ZSelectQuery clearWhere() {
		if (m_whereExpr != null) {
			m_whereExpr.clear();
		}
		return this;
	}

	/**
	 * Clear the HAVING clause.
	 * 
	 * @return this
	 */
	public final ZSelectQuery clearHaving() {
		if (m_havingExpr != null) {
			m_havingExpr.clear();
		}
		return this;
	}

	/**
	 * Returns the WHERE <code>ZExpression</code> object.
	 * 
	 * @return the WHERE <code>ZExpression</code> object
	 */
	public final ZExpression getWhereExpr() {
		if (m_whereExpr == null) {
			m_whereExpr = new ZExpression();
		}
		return m_whereExpr;
	}

	/**
	 * Returns the HAVING <code>ZExpression</code> object.
	 * 
	 * @return the HAVING <code>ZExpression</code> object
	 */
	public final ZExpression getHavingExpr() {
		if (m_havingExpr == null) {
			m_havingExpr = new ZExpression();
		}
		return m_havingExpr;
	}

	/**
	 * Executes the query and returns only one object as the result (the first
	 * selected item from the first row). A NULL value is returned if the query
	 * didn't retrieve any results.
	 * 
	 * @return the result of the query
	 */
	public final Object executeUnique() {
		return (Object) executeQuery(true, true);
	}

	/**
	 * Executes the query and returns an array containing the first selected
	 * item from all the rows. An empty array is returned if the query didn't
	 * retrieve any results.
	 * 
	 * @return the result of the query
	 */
	public final Object[] executeUniqueSelect() {
		return (Object[]) executeQuery(true, false);
	}

	/**
	 * Executes the query and returns a map with all the selected items. Each
	 * map value represents the object from the first row (or NULL if the query
	 * didn't retrieve any rows). The keys in the map correspond to the alias
	 * values specified in the select clause (or the field name if the alias was
	 * not specified).
	 * 
	 * @return the result of the query
	 */
	@SuppressWarnings("unchecked")
	// @SuppressWarnings because of the cast with generics
	public final Map<String, Object> executeUniqueRow() {
		return (Map<String, Object>) executeQuery(false, true);
	}

	/**
	 * Executes the query and returns a map with all the selected items. Each
	 * map value represents an array with the objects corresponding to that
	 * particular selected item. If the query didn't returned any rows, than all
	 * these arrays will have zerro length. The keys in the map correspond to
	 * the alias values specified in the select clause (or the field name if the
	 * alias was not specified).
	 * 
	 * @return the result of the query
	 */
	@SuppressWarnings("unchecked")
	// @SuppressWarnings because of the cast with generics
	public final Map<String, Object[]> execute() {
		return (Map<String, Object[]>) executeQuery(false, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (m_query != null) {
			return m_query;
		}
		// estimate the length of the final query
		int queryLength = QUERY_EXTRA_BUFFER_CAPACITY;
		queryLength += m_fromClause.length();
		if (m_whereExpr != null) {
			queryLength += m_whereExpr.getStringBuilder().length();
		}
		if (m_havingExpr != null) {
			queryLength += m_havingExpr.getStringBuilder().length();
		}
		StringBuilder query = new StringBuilder(queryLength);
		query.append("SELECT ");
		if (m_distinct) {
			query.append("DISTINCT ");
		}
		// SELECT clause
		boolean selectClauseEmpty = true;
		if (m_selectInfoList != null) {
			for (SelectInfo selectInfo : m_selectInfoList) {
				ZStringField idField = selectInfo.meta.getIdField();
				if (idField != null) {
					if (selectClauseEmpty) {
						selectClauseEmpty = false;
					} else {
						query.append(", ");
					}
					query.append(selectInfo.tableAlias);
					query.append('.');
					query.append(idField.getName());
				}
				for (ZField field : selectInfo.fields) {
					if (selectClauseEmpty) {
						selectClauseEmpty = false;
					} else {
						query.append(", ");
					}
					query.append(selectInfo.tableAlias);
					query.append('.');
					query.append(field.getName());
				}
			}
		}
		if (m_selectClause != null) {
			if (selectClauseEmpty) {
				selectClauseEmpty = false;
			} else {
				query.append(", ");
			}
			query.append(m_selectClause);
		}
		// FROM clause
		query.append(" FROM ");
		query.append(m_fromClause);
		// WHERE clause
		if (m_whereExpr != null) {
			query.append(" WHERE ");
			query.append(m_whereExpr.getStringBuilder());
		}
		// GROUP BY clause
		if ((m_groupByArray != null) && (m_groupByArray.length > 0)) {
			query.append(" GROUP BY ");
			int i = 0;
			for (Object groupByExpr : m_groupByArray) {
				if (i++ > 0) {
					query.append(", ");
				}
				query.append(groupByExpr);
			}
		}
		// HAVING clause
		if (m_havingExpr != null) {
			query.append(" HAVING ");
			query.append(m_havingExpr.getStringBuilder());
		}
		// ORDER BY clause
		if ((m_orderByArray != null) && (m_orderByArray.length > 0)) {
			int i = 0;
			query.append(" ORDER BY ");
			for (Object orderByExpr : m_orderByArray) {
				if (i++ > 0) {
					query.append(", ");
				}
				query.append(orderByExpr);
			}
		}
		// LIMIT clause
		if (m_skip == 0) {
			if (m_take != Integer.MAX_VALUE) {
				query.append(" LIMIT ");
				query.append(m_take);
			}
		} else {
			query.append(" LIMIT ");
			query.append(m_skip);
			query.append(", ");
			query.append(m_take);
		}
		// extra
		if (m_extra != null) {
			query.append(' ');
			query.append(m_extra);
		}
		return query.toString();
	}

	/**
	 * Returns the selectInfoList (lazy initialized).
	 * 
	 * @return the selectInfoList (lazy initialized)
	 */
	private final LinkedList<SelectInfo> getSelectInfoList() {
		if (m_selectInfoList == null) {
			m_selectInfoList = new LinkedList<SelectInfo>();
		}
		return m_selectInfoList;
	}

	/**
	 * Returns the SELECT clause <code>StringBuffer</code> (lazy initialized).
	 * 
	 * @return the SELECT clause <code>StringBuffer</code> (lazy initialized)
	 */
	private final StringBuilder getSelectClause() {
		if (m_selectClause == null) {
			m_selectClause = new StringBuilder(CLAUSE_BUFFER_CAPACITY);
		}
		return m_selectClause;
	}

	/**
	 * Returns an array of fields containing all the fields from the specified
	 * array, except the id field.
	 * 
	 * @param meta
	 * @param fields
	 * @return an array of fields containing all the fields from the specified
	 *         array, except the id field
	 */
	private final ZField[] filterIdField(ZPersistentMeta meta, ZField[] fields) {
		int n = 0;
		for (ZField field : fields) {
			if (field != meta.getIdField()) {
				n++;
			}
		}
		if (n == fields.length) {
			return fields;
		}
		ZField[] filteredFields = new ZField[n];
		n = 0;
		for (ZField field : fields) {
			if (field != meta.getIdField()) {
				filteredFields[n++] = field;
			}
		}
		return filteredFields;
	}

	/**
	 * Executes the SELECT query and returns the corect object (based on the
	 * uniqueSelect and uniqueRow parameters).
	 * 
	 * @see ZSelectQuery.executeUnique()
	 * @see ZSelectQuery.executeUniqueSelect()
	 * @see ZSelectQuery.executeUniqueRow()
	 * @see ZSelectQuery.execute()
	 * 
	 * @param uniqueSelect
	 * @param uniqueRow
	 * @return the object result.
	 */
	private final Object executeQuery(boolean uniqueSelect, boolean uniqueRow) {
		if (m_session == null) {
			throw new ZormException("The session is not set.");
		}
		m_query = toString();
		m_session.logQuery(m_query);
		Statement stmt = m_session.getSqlStatement();
		ResultSet rs = null;

		try {
			rs = stmt.executeQuery(m_query);
			ResultSetMetaData rsm = rs.getMetaData();

			// determine the number of selected items and colums
			int nSelected = 0;
			int nNeededColumns = 0;
			if (m_selectInfoList != null) {
				for (SelectInfo selectInfo : m_selectInfoList) {
					if (selectInfo.meta.getIdField() != null) {
						nNeededColumns++;
					}
					nNeededColumns += selectInfo.fields.length;
					nSelected++;
				}
			}
			int nColumns = rsm.getColumnCount();
			if (nNeededColumns > rsm.getColumnCount()) {
				throw new ZormException(
						"Insufficient columns returned by the query: expected at least: "
								+ nNeededColumns + "; actual: " + nColumns
								+ '.');
			}
			nSelected += (nColumns - nNeededColumns);
			if (nSelected == 0) {
				throw new ZormException(
						"The number of selected items is 0. This means that the select clause of the query was empty.");
			}
			if (uniqueSelect) {
				nSelected = 1;
			}

			// determine the number of rows
			int nRows = 0;
			if (rs.last()) {
				nRows = rs.getRow();
			}
			rs.first();
			if (uniqueRow && nRows > 1) {
				nRows = 1;
			}

			// get data
			Object[][] resultMatrix = new Object[nSelected][nRows];
			for (int i = 0; i < nRows; i++) {
				int iSelected = 0;
				int iColumn = 1;
				if (m_selectInfoList != null) {
					for (SelectInfo selectInfo : m_selectInfoList) {
						resultMatrix[iSelected++][i] = m_session
								.getAndFetchFromResultSet(selectInfo.meta,
										selectInfo.fields, rs, iColumn);
						if (selectInfo.meta.getIdField() != null) {
							iColumn++;
						}
						iColumn += selectInfo.fields.length;
						if (iSelected == nSelected) {
							// this can happen only when uniqueSelect is true
							break;
						}
					}
				}
				while (iSelected < nSelected) {
					resultMatrix[iSelected++][i] = rs.getObject(iColumn++);
				}
				if (!rs.next() && i != nRows - 1) {
					throw new ZormException(
							"Insufficient number of rows: expected: " + nRows
									+ "; actual: " + i + '.');
				}
			}

			// prepare the result
			if (uniqueSelect) {
				if (uniqueRow) {
					return (nRows == 0) ? null : resultMatrix[0][0];
				} else {
					return resultMatrix[0];
				}
			} else {
				Map<String, Object> resultMap = new HashMap<String, Object>();
				int iSelected = 0;
				int iColumn = 1;
				if (m_selectInfoList != null) {
					for (SelectInfo selectInfo : m_selectInfoList) {
						ZPersistentMeta meta = selectInfo.meta;
						if (meta.getIdField() != null) {
							iColumn++;
						}
						Object value;
						if (uniqueRow) {
							value = (nRows == 0) ? null
									: resultMatrix[iSelected][0];
						} else {
							value = resultMatrix[iSelected];
						}
						resultMap.put(selectInfo.tableAlias, value);
						iSelected++;
						iColumn += selectInfo.fields.length;
					}
				}
				while (iSelected < nSelected) {
					String label = rsm.getColumnLabel(iColumn);
					if (resultMap.containsKey(label)) {
						throw new ZormException(
								"Duplicate selected item name: " + label + '.');
					}
					Object value;
					if (uniqueRow) {
						value = (nRows == 0) ? null
								: resultMatrix[iSelected][0];
					} else {
						value = resultMatrix[iSelected];
					}
					resultMap.put(label, value);
					iColumn++;
					iSelected++;
				}
				return resultMap;
			}
		} catch (Exception e) {
			throw new ZormException("Error executing the query : " + m_query
					+ '.', e);
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
	}
}

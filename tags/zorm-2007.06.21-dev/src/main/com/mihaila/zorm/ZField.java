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

import com.mihaila.zorm.exception.ZormException;

/**
 * Generic class that corresponds to a field in the table and the
 * <code>ZPersistent</code> object. It is used to adapt the SQL Types with
 * application types.
 * 
 * This class does not make impose constrains on the SQL object returned by the
 * JDBC interface. It represents a generic field that works with
 * <code>Object</code> objects.
 * 
 * Other classes, inheriting from <code>ZField</code> (such as
 * <code>ZIdField</code> or <code>ZStringField</code>), should be used to
 * define fields.
 */
public class ZField {

	/**
	 * The column name in the SQL table.
	 */
	private String m_name;

	/**
	 * The associated <code>ZPersistentMeta</code> object
	 */
	private ZPersistentMeta m_persistentMeta;

	/**
	 * If true, this field is automatically fetched from the database.
	 */
	private boolean m_autoFetched = true; // default true

	/**
	 * If true, this field is automatically generated by the database server.
	 */
	private boolean m_autoGenerated; // default false

	/**
	 * If true, the value corresponding to this field cand be null;
	 */
	private boolean m_nullValid; // default false

	/**
	 * The position of this field in the <code>ZPersistent</code> object.
	 */
	private int m_index;

	/**
	 * The value that is returned by the <code>toString()</code> function.
	 */
	private String m_toStringValue;

	public ZField() {
	}

	/**
	 * @param name
	 *            the column name in the SQL table
	 */
	public ZField(String name) {
		m_name = name;
	}

	/**
	 * Returns the the column name in the SQL table.
	 * 
	 * @return the the column name in the SQL table
	 */
	public final String getName() {
		return m_name;
	}

	/**
	 * Set the column name in the SQL table for this field.
	 * 
	 * @param name
	 */
	public final void setName(String name) {
		m_name = name;
	}

	/**
	 * Returns the associated <code>ZPersistentMeta</code> object.
	 * 
	 * @return the associeaed <code>ZPersistentMeta</code> object
	 */
	public final ZPersistentMeta getPersistentMeta() {
		return m_persistentMeta;
	}

	/**
	 * Returns the position of this field in the <code>ZPersistent</code>
	 * object.
	 * 
	 * @return the position of this field in the <code>ZPersistent</code>
	 *         object
	 */
	public final int getIndex() {
		return m_index;
	}

	/**
	 * Returns the state of auto mode. If true, this field is automatically
	 * fetched from the database. The default value is true.
	 * 
	 * @return the state of autoFetch mode
	 */
	public final boolean isAutoFetched() {
		return m_autoFetched;
	}

	/**
	 * Enable/disable the auto mode. If true, this field is automatically
	 * fetched from the database. The default value is true.
	 * 
	 * @param autoFetched
	 */
	public final void setAutoFetched(boolean autoFetched) {
		m_autoFetched = autoFetched;
	}

	/**
	 * Returns true if this field is automatically generated on the database.
	 * The default value is false.
	 * 
	 * @return true if this field is automatically generated on the database
	 */
	public final boolean isAutoGenerated() {
		return m_autoGenerated;
	}

	/**
	 * Set if this field is generated or not on the database. The default value
	 * is false.
	 * 
	 * @param autoGenerated
	 */
	public final void setAutoGenerated(boolean autoGenerated) {
		m_autoGenerated = autoGenerated;
	}

	/**
	 * Returns true if null is a valid value for this field. The default value
	 * is false.
	 * 
	 * @return true if null is a valid value for this field.
	 */
	public final boolean isNullValid() {
		return m_nullValid;
	}

	/**
	 * Sets this field to accept or not null values. The default value is false.
	 * 
	 * @param nullValid
	 */
	public final void setNullValid(boolean nullValid) {
		m_nullValid = nullValid;
	}

	/**
	 * Convert an object returned by the JDBC interface to an object specific to
	 * this field. The specified object can be null.
	 * 
	 * @param sqlValue
	 */
	public Object fromSqlValue(Object sqlValue) {
		return sqlValue;
	}

	/**
	 * Convert the object specific to this field to a string to used in SQL
	 * commands. the specified object can be null.
	 * 
	 * @param value
	 */
	public String toSqlValue(Object value) {
		return (value == null) ? null : value.toString();
	}

	/**
	 * Convert the object specific to this field to a string thare represents an
	 * SQL expression to update this field in the database. This function is
	 * used by the Zorm framework when there is a need to save the object to
	 * database only if <code>useSqlExprForUpdate()</code> returns true.
	 * 
	 * @param value
	 */
	public String toSqlExpr(Object value) {
		throw new AssertionError(
				"You must provide an implementation for toSqlExpr() function in an class inheriting from ZField");
	}

	/**
	 * Specify if <code>toSqlExpr(Object)</code> is used instead of
	 * <code>toSqlValue(Object)</code> for updating the field in the database.
	 * This feature useful with field that have to update the value based on the
	 * previous value in the database, such as counters ("counter = counter + 1"
	 * instead of an absolute value).
	 * 
	 */
	public boolean isUsingSqlExprForUpdate() {
		return false;
	}

	/**
	 * Validates the specified object.
	 * 
	 * @param value
	 */
	public void validate(Object value) {
		if (!isNullValid() && value == null) {
			throw new ZormException(
					"Cannot set a null value to a non-null field (" + this
							+ ").");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return m_toStringValue;
	}

	/**
	 * Set the <code>ZPersistentMeta</code> object. This function is
	 * automatically called by the <code>ZPersistentMeta</code> object when
	 * this field is added to it (via <code>ZPersistentMeta.setFields()</code>).
	 * 
	 * @param persistentMeta
	 */
	final void setPersistentMeta(ZPersistentMeta persistentMeta) {
		m_persistentMeta = persistentMeta;
		m_toStringValue = getPersistentMeta().getTableAlias() + '.' + getName();

	}

	/**
	 * Set the position of this field in the <code>ZPersistent</code> object.
	 * 
	 * @param index
	 */
	final void setIndex(int index) {
		m_index = index;
	}

	/**
	 * Same as <code>ZPersistent.setFieldValueInternal(ZField, Object)</code>
	 * 
	 * @param persistent
	 * @param value
	 */
	protected final void setFieldValueInternal(ZPersistent persistent,
			Object value) {
		persistent.setFieldValueInternal(this, value);
	}

	/**
	 * Same as <code>ZPersistent.setFieldValueInternal(ZField, Object)</code>
	 * 
	 * @param persistent
	 * @param value
	 */
	protected final void setFieldModified(ZPersistent persistent,
			boolean modified) {
		persistent.setFieldModified(this, modified);
	}

	/**
	 * Check if null is a valid value for this field. If not, a ZormException is
	 * throwned.
	 */
	protected final void checkIfNullValid() {
		if (!isNullValid()) {
			throw new ZormException("Null is not a valid value.");
		}
	}

}

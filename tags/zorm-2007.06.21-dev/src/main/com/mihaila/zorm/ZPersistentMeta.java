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
import com.mihaila.zorm.field.ZStringField;

/**
 * Holds the information about a class inheriting <code>ZPersistent</code>.
 * Each <code>ZPersistentMeta</code> object corresponds to a table in the SQL
 * Database.
 */
public class ZPersistentMeta {

	public ZPersistentMeta() {
	}

	/**
	 * @param persistentType
	 *            the corresponding class type inheriting from
	 *            <code>ZPersistent</code>.
	 * @param tableName
	 *            the corresponding table name
	 */
	public ZPersistentMeta(Class<? extends ZPersistent> persistentType,
			String tableName) {
		setPersistentType(persistentType);
		setTableName(tableName);
	}

	/**
	 * @param persistentType
	 *            the corresponding class type inheriting from
	 *            <code>ZPersistent</code>.
	 * @param tableName
	 *            the corresponding table name
	 * @param tableAlias
	 *            the alias of the table (the value used in queries instead of
	 *            table name)
	 */
	public ZPersistentMeta(Class<? extends ZPersistent> persistentType,
			String tableName, String tableAlias) {
		setPersistentType(persistentType);
		setTableName(tableName);
		setTableAlias(tableAlias);
	}

	/**
	 * Returns the corresponding type inheriting from <code>ZPersistent</code>.
	 * 
	 * @return the corresponding type inheriting from <code>ZPersistent</code>
	 */
	public final Class<? extends ZPersistent> getPersistentType() {
		return m_persistentType;
	}

	/**
	 * Set the corresponding type inheriting from <code>ZPersistent</code>.
	 * 
	 * @param persistentType
	 */
	public final void setPersistentType(
			Class<? extends ZPersistent> persistentType) {
		m_persistentType = persistentType;
	}

	/**
	 * Returns the corresponded SQL table name.
	 * 
	 * @return the corresponded SQL table name
	 */
	public final String getTableName() {
		return m_tableName;
	}

	/**
	 * Set the corresponded SQL table name.
	 * 
	 * @param tableName
	 */
	public final void setTableName(String tableName) {
		m_tableName = tableName;
	}

	/**
	 * Returns the table alias (the value used in queries instead of table
	 * name).
	 * 
	 * @return the table alias
	 */
	public final String getTableAlias() {
		return m_tableAlias;
	}

	/**
	 * Set the table alias (the value used in queries instead of table name)
	 * 
	 * @param tableAlias
	 */
	public final void setTableAlias(String tableAlias) {
		m_tableAlias = tableAlias;
	}

	/**
	 * Returns the field with the specified index.
	 * 
	 * @param index
	 * @return he field with the specified index
	 */
	public final ZField getField(int index) {
		return m_fields[index];
	}

	/**
	 * Returns the identification field associated with
	 * <code>ZPersistentMeta</code> object.
	 * 
	 * @return the identification field associated with
	 *         <code>ZPersistentMeta</code> object.
	 */
	public final ZStringField getIdField() {
		return m_idField;
	}

	/**
	 * Set the identification field associated with <code>ZPersistentMeta</code>
	 * object. If a <code>ZStringField</code> with the name "id" is associated
	 * with this <code>ZPersistentMeta</code> object, than it is automatically
	 * set as the identification field; in this case, a call of this function is
	 * unnecessary.
	 * 
	 * @param idField
	 */
	public final void setIdField(ZStringField idField) {
		if (idField.isNullValid()) {
			throw new ZormException("The id field: " + idField
					+ "cannot have null as a valid value.");
		}
		m_idField = idField;
	}

	/**
	 * Returns an array with all the fields associated with this
	 * <code>ZPersistentMeta</code> object.
	 * 
	 * @return an array with all the fields associated with this
	 *         <code>ZPersistentMeta</code> object
	 */
	public final ZField[] getAllFields() {
		return m_fields;
	}

	/**
	 * Set the fields associated with this <code>ZPersistentMeta</code> object
	 * (columns of the table).
	 * 
	 * @param fields
	 *            the fields associated with this <code>ZPersistentMeta</code>
	 *            object
	 */
	public final void setFields(ZField... fields) {
		if (fields.length > 32) {
			throw new ZormException(
					"ZORM does not support <code>ZPersistent</code> objects with more than 32 fields");
		}
		m_fields = fields;

		// initialize fields
		int index = 0;
		int numAutoFields = 0;
		int numAutoGenerated = 0;
		// autodetect the id field (if it was not set
		if (getIdField() == null) {
			for (ZField field : fields) {
				// set the index field
				if ((field instanceof ZStringField)
						&& "id".equals(field.getName())) {
					setIdField((ZStringField) field);
				}
			}
		}
		for (ZField field : fields) {
			// set meta and index to field
			field.setPersistentMeta(this);
			field.setIndex(index++);
			// count autofetched fields (the id field is excluded)
			if (field.isAutoFetched() && (field != getIdField())) {
				numAutoFields++;
			}
			// count autogenerated fields
			if (field.isAutoGenerated()) {
				numAutoGenerated++;
			}
		}
		m_autoFetchedFields = new ZField[numAutoFields];
		m_autoGeneratedFields = new ZField[numAutoGenerated];
		numAutoFields = 0;
		numAutoGenerated = 0;
		for (ZField field : fields) {
			if (field.isAutoFetched() && (field != getIdField())) {
				m_autoFetchedFields[numAutoFields++] = field;
			}
			if (field.isAutoGenerated()) {
				m_autoGeneratedFields[numAutoGenerated++] = field;
			}
		}
	}

	/**
	 * Returns an array with all the fields that are automatically fetched from
	 * the database. The id field is excluded from this list.
	 * 
	 * @return an array with all the fields that are automatically fetched from
	 *         the database
	 */
	public final ZField[] getAutoFetchedFields() {
		return m_autoFetchedFields;
	}

	/**
	 * Returns an array with all the fields that are generated on the database.
	 * 
	 * @return an array with all the fields that are generated on the database
	 */
	public final ZField[] getAutoGeneratedFields() {
		return m_autoGeneratedFields;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getTableAlias();
	}

	/**
	 * The corresponding type inheriting from <code>ZPersistent</code>.
	 */
	private Class<? extends ZPersistent> m_persistentType;

	/**
	 * The corresponding SQL table name.
	 */
	private String m_tableName;

	/**
	 * The alias value that is used in queries insteand of the tableName.
	 */
	private String m_tableAlias;

	/**
	 * The identification field associated with <code>ZPersistentMeta</code>
	 * object.
	 */
	private ZStringField m_idField; // default null;

	/**
	 * Contains the fields associated with this <code>ZPersistentMeta</code>
	 * object (columns of the table).
	 */
	private ZField[] m_fields;

	/**
	 * Contains the fields that are automatically fetched from the database.
	 */
	private ZField[] m_autoFetchedFields;

	/**
	 * Contains the fields that are generated in the database.
	 */
	private ZField[] m_autoGeneratedFields;

}

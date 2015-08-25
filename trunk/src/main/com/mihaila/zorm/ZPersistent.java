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
 * Base class to be extended by any class that need database persistence
 */
public abstract class ZPersistent {

	/**
	 * Contains the value of each field in this <code>ZPersistent</code>
	 * object.
	 */
	private Object[] m_fieldValues;

	/**
	 * Specifies which field is initialized (a bit for each field).
	 */
	private int m_initializedFields; // default 0

	/**
	 * Specifies which field is modified (a bit for each field).
	 */
	private int m_modifiedFields; // default 0

	/**
	 * Specifies if this object is new or not.
	 */
	private boolean m_isNew = true; // default true;

	/**
	 * The session to which this object is attached (null if the object is not
	 * attached to any session).
	 */
	private ZSession m_session; // initially null;

	public ZPersistent() {
		m_fieldValues = new Object[getMeta().getAllFields().length];
	}

	/**
	 * Returns the meta information associated with this object.
	 * 
	 * @return meta information associated with this object.
	 */
	public abstract ZPersistentMeta getMeta();

	/**
	 * Returns the id of this <code>ZPersistent</code> object.
	 * 
	 * @return the id of this <code>ZPersistent</code> object.
	 */
	public final String getId() {
		ZStringField idField = getMeta().getIdField();
		if (idField == null) {
			return null;
		}
		return idField.getValue(this);
	}

	/**
	 * Returns the session to which this object is attached. Null is returned if
	 * this object is not attached to any session.
	 * 
	 * @return the session to which this object is attached
	 */
	public final ZSession getSession() {
		return m_session;
	}

	/**
	 * Set the session to which this object is attached. Null is returned if
	 * this object is not attached to any session. This method is used by
	 * <code>ZSession.attach(ZPersistent)</code>.
	 * 
	 * @param session
	 */
	final void setSessionBackdoor(ZSession session) {
		m_session = session;
	}

	/**
	 * Returns true if this object is attached to a session, false otherwise.
	 * 
	 * @return true if this object is attached to a session
	 */
	public final boolean isAttached() {
		return (m_session != null);
	}

	/**
	 * Attach the specified <code>ZPersistent</code> object to this session
	 * (add it to the object cache and replace an existing object, if any).
	 */
	public void attach(ZSession session) {
		if (m_session == session) {
			return;
		}
		dettach();
		m_session = session;
		if (!isNew()) {
			m_session.addToCache(this);
		}
	}

	/**
	 * Dettach this object from the current session (remove it to the object
	 * cache).
	 */
	public void dettach() {
		if (m_session != null) {
			if (!isNew()) {
				m_session.removeFromCache(this);
			}
		}
	}

	/**
	 * Get the value of the specified field. This function can be overwritten if
	 * for example you need to know when a field is read.
	 * 
	 * @param field
	 * @return the value of the specified field
	 */
	public Object getFieldValue(ZField field) {
		if (!isFieldInitialized(field)) {
			checkIfAttached();
			checkIfNotNew();
			m_session.fetchFieldInternal(this, field);
		}
		return m_fieldValues[field.getIndex()];
	}

	/**
	 * Get the value of the specified field. If the field is not initialized,
	 * null is returned.
	 * 
	 * @param field
	 * @return the value of the specified field
	 */
	Object getFieldValueInternal(ZField field) {
		return m_fieldValues[field.getIndex()];
	}

	/**
	 * Set the value of the specified field. This function is used internally by
	 * <code>ZField</code> objects. This function can be overwritten if for
	 * example you need to know when a field is set.
	 * 
	 * @param field
	 * @param value
	 */
	public void setFieldValue(ZField field, Object value) {
		ZField idField = getMeta().getIdField();
		if (!isNew() && (idField != null) && (idField == field)) {
			throw new ZormException(
					"Modification of the id field for non new persistent object is not permitted");
		}
		field.validate(value);
		setFieldValueInternal(field, value);
		setFieldModified(field, true);
	}

	/**
	 * Set the value of the specified field.
	 * 
	 * @param field
	 * @param value
	 */
	void setFieldValueInternal(ZField field, Object value) {
		m_fieldValues[field.getIndex()] = value;
		setFieldInitialized(field, true);
	}

	/**
	 * Clear the value of the specified field.
	 * 
	 * @param field
	 */
	public final void clearFieldValue(ZField field) {
		if (field == getMeta().getIdField()) {
			throw new ZormException("Identification field: " + field
					+ " cannot be cleared");
		}
		m_fieldValues[field.getIndex()] = null;
		setFieldInitialized(field, false);
		setFieldModified(field, false);
	}

	/**
	 * Clear all field values from this <code>ZPersistent</code> object,
	 * except for the identification field.
	 * 
	 */
	public final void clearAllFieldValues() {
		for (ZField field : getMeta().getAllFields()) {
			if (field == getMeta().getIdField() && !isNew()) {
				continue;
			}
			m_fieldValues[field.getIndex()] = null;
			setFieldInitialized(field, false);
			setFieldModified(field, false);
		}
	}

	/**
	 * Returs true if the specified field is initialized, false otherwise.
	 * 
	 * @param field
	 * @return if the specified field is initialized, false otherwise.
	 */
	public final boolean isFieldInitialized(ZField field) {
		return (m_initializedFields & (1 << field.getIndex())) != 0;
	}

	/**
	 * Set the state of the initialized flag for the specified field.
	 * 
	 * @param field
	 * @param initialized
	 */
	final void setFieldInitialized(ZField field, boolean loaded) {
		if (loaded) {
			m_initializedFields |= (1 << field.getIndex());
		} else {
			m_initializedFields &= ~(1 << field.getIndex());

		}
	}

	/**
	 * Returs true if the specified field is modified, false otherwise.
	 * 
	 * @param field
	 * @return if the specified field is modified, false otherwise.
	 */
	public final boolean isFieldModified(ZField field) {
		return (m_modifiedFields & (1 << field.getIndex())) != 0;
	}

	/**
	 * Set the state of the modified flag for the specified field.
	 * 
	 * @param field
	 * @param modified
	 */
	final void setFieldModified(ZField field, boolean modified) {
		if (modified) {
			m_modifiedFields |= (1 << field.getIndex());
		} else {
			m_modifiedFields &= ~(1 << field.getIndex());
		}
	}

	/**
	 * Returns true if this persistent object has modified fields, false
	 * otherwise.
	 * 
	 * @return true if this persistent object has modified fields, false
	 *         otherwise
	 */
	public final boolean isModified() {
		return m_modifiedFields != 0;
	}

	/**
	 * Set the state of the modified flags. If the specified value is true, than
	 * all the initialized fields are marked as modified (except the id). If the
	 * value is false, than all the fields are marked as not modified.
	 * 
	 * @param modified
	 */
	public final void setModified(boolean modified) {
		if (modified) {
			m_modifiedFields = m_initializedFields;
			// mask the ifField modified flag
			ZField idField = getMeta().getIdField();
			if (idField != null) {
				m_modifiedFields &= ~(1 << idField.getIndex());
			}
		} else {
			m_modifiedFields = 0;
		}
	}

	/**
	 * Returns true if this is a new object that was never saved or retrived
	 * from the dabase, false otherwise.
	 * 
	 * @return true if this is a new object that was never saved or retrived
	 *         from the dabase, false otherwise
	 */
	public final boolean isNew() {
		return m_isNew;
	}

	/**
	 * Set the new state of this object.
	 * 
	 * @param value
	 */
	final void setNew(boolean value) {
		m_isNew = value;
	}

	/**
	 * Fetch the non-initialized auto fetched fields from the database.
	 */
	public final void fetch() {
		checkIfAttached();
		checkIfNotNew();
		m_session.fetchAutoFetchedFieldsInternal(this);
	}

	/**
	 * Fetch the non-initialized specified field from the database.
	 * 
	 * @param field
	 */
	public final void fetch(ZField field) {
		checkIfAttached();
		checkIfNotNew();
		m_session.fetchFieldInternal(this, field);
	}

	/**
	 * Fetch the non-initialized specified fields from the database.
	 * 
	 * @param fields
	 */
	public final void fetch(ZField[] fields) {
		checkIfAttached();
		checkIfNotNew();
		m_session.fetchFieldsInternal(this, fields);
	}

	/**
	 * Save this <code>ZPersistent</code> object to database. This function
	 * can be overriden if for example you want to make extra operations when
	 * this object is saved.
	 */
	public void save() {
		checkIfAttached();
		m_session.saveInternal(this);
	}

	/**
	 * Delete this <code>ZPersistent</code> object from the database. This
	 * function can be overriden if for example you want to make extra
	 * operations when this object is deleted.
	 * 
	 * @return true, if this object was deleted from the database, false if it
	 *         didn't exist.
	 */
	public boolean delete() {
		checkIfAttached();
		checkIfNotNew();
		return m_session.deleteInternal(this.getMeta(), this.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		ZField idField = getMeta().getIdField();
		if ((idField == null) || !isFieldInitialized(idField)) {
			return getMeta().getTableName() + ":[NEW]";
		} else {
			return getMeta().getTableName() + ':' + getId();
		}
	}

	/**
	 * Checks if this object is attached to a session. If it is not attached, an
	 * exception is throwned.
	 */
	private void checkIfAttached() {
		if (m_session == null) {
			throw new ZormException(
					"Persistent object: "
							+ this
							+ " must be attached to a session to perform this operation.");
		}
	}

	/**
	 * Checks if this object is not new. If it is new, an exception is throwned.
	 */
	private void checkIfNotNew() {
		if (isNew()) {
			throw new ZormException("Persistent object: " + this
					+ " must not be new to perform this operation.");
		}
	}

}

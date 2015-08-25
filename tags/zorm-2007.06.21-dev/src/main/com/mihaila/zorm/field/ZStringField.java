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

package com.mihaila.zorm.field;

import com.mihaila.zorm.ZField;
import com.mihaila.zorm.ZPersistent;

/**
 * ZField corresponding to <code>java.lang.String</code> type. This field (and
 * the inheriting classes) can be used as identification fields for the
 * <code>ZPersistent</code> objects.
 */
public class ZStringField extends ZField {

	public ZStringField() {
		super();
	}

	/**
	 * @param name
	 *            the column name in the SQL table
	 */
	public ZStringField(String name) {
		super(name);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see zorm.ZField#validate(java.lang.Object)
	 */
	@Override
	public void validate(Object value) {
		if (value == null) {
			checkIfNullValid();
		} else {
			@SuppressWarnings("unused")
			String v = (String) value;
		}
	}
	
	/**
	 * Get the <code>String</code> value of the specified
	 * <code>ZPersistent</code> object.
	 * 
	 * @param persistent
	 * @return the Id value of the specified <code>ZPersistent</code> object
	 */
	public String getValue(ZPersistent persistent) {
		return (String) persistent.getFieldValue(this);
	}

	/**
	 * Set the <code>String</code> value of the specified
	 * <code>ZPersistent</code> object. An ZormInvalidFieldValueException is
	 * throwned if the value is not valid.
	 * 
	 * @param persistent
	 * @param value
	 */
	public void setValue(ZPersistent persistent, String value) {
		persistent.setFieldValue(this, value);
	}

}

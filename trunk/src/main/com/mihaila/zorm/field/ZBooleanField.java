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
 * ZField corresponding to <code>java.lang.Boolean</code> type.
 */
public class ZBooleanField extends ZField {

	public ZBooleanField() {
		super();
	}

	/**
	 * @param name
	 *            the column name in the SQL table
	 */
	public ZBooleanField(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see zorm.ZField#toSqlValue(java.lang.Object)
	 */
	@Override
	public String toSqlValue(Object ob) {
		if (ob == null) {
			return null;
		}
		return ((Boolean) ob) ? "1" : "0"; 
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
			boolean v = (Boolean) value;
		}
	}
	
	/**
	 * Returns the <code>Boolean</code> value of this field from the specified
	 * <code>ZPersistent</code> object.
	 * 
	 * @param persistent
	 * @return the <code>Boolean</code> value of this field from the specified
	 *         <code>ZPersistent</code> object.
	 */
	public Boolean getValue(ZPersistent persistent) {
		return (Boolean) persistent.getFieldValue(this);
	}

	/**
	 * Set the <code>Boolean</code> value of this field from the specified
	 * <code>ZPersistent</code> object.
	 * 
	 * @param persistent
	 * @param value
	 */
	public void setValue(ZPersistent persistent, Boolean value) {
		persistent.setFieldValue(this, value);
	}


}

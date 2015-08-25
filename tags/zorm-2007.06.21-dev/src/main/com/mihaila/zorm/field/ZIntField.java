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
 * ZField corresponding to <code>java.lang.Integer</code> type.
 */
public class ZIntField extends ZField {

	public ZIntField() {
		super();
	}

	/**
	 * @param name
	 *            the column name in the SQL table
	 */
	public ZIntField(String name) {
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
			int v = (Integer) value;
		}
	}

	/**
	 * Returns the <code>Integer</code> value of this field from the specified
	 * <code>ZPersistent</code> object.
	 * 
	 * @param persistent
	 * @return the <code>Integer</code> value of this field from the specified
	 *         <code>ZPersistent</code> object.
	 */
	public Integer getValue(ZPersistent persistent) {
		return (Integer) persistent.getFieldValue(this);
	}

	/**
	 * Set the <code>Integer</code> value of this field from the specified
	 * <code>ZPersistent</code> object.
	 * 
	 * @param persistent
	 * @param value
	 */
	public void setValue(ZPersistent persistent, Integer value) {
		persistent.setFieldValue(this, value);
	}

}

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

import com.mihaila.zorm.ZPersistent;

/**
 * ZField corresponding to <code>java.lang.Integer</code> type but which holds
 * the value as a <code>String</code>.
 */
public class ZStringIntField extends ZStringField {

	public ZStringIntField() {
		super();
	}

	/**
	 * @param name
	 */
	public ZStringIntField(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see zorm.ZField#fromSqlValue(java.lang.Object)
	 */
	@Override
	public Object fromSqlValue(Object sqlValue) {
		if (sqlValue == null) {
			return null;
		} else {
			// accept any numeric type 
			return sqlValue.toString();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see zorm.field.ZStringField#validate(java.lang.Object)
	 */
	@Override
	public void validate(Object value) {
		if (value == null) {
			checkIfNullValid();
		} else {
			@SuppressWarnings("unused")
			String v = (String) value;
			Integer.parseInt(v);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see zorm.field.ZStringField#setValue(zorm.ZPersistent, java.lang.String)
	 */
	@Override
	public void setValue(ZPersistent persistent, String value) {
		persistent.setFieldValue(this, value);
	}

}

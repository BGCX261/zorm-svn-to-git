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

public class ZExpression {

	/**
	 * Add the specified fragment to the expression.
	 * 
	 * @param fragment1
	 * @return this
	 */
	public ZExpression expr(Object fragment1) {
		if (m_expr.length() != 0) {
			m_expr.append(" AND ");
		}
		m_expr.append('(');
		m_expr.append(fragment1);
		m_expr.append(')');
		return this;
	}

	/**
	 * Add the specified fragments to the expression.
	 * 
	 * @param fragment1
	 * @param fragment2
	 * @return this
	 */
	public ZExpression expr(Object fragment1, Object fragment2) {
		if (m_expr.length() != 0) {
			m_expr.append(" AND ");
		}
		m_expr.append('(');
		m_expr.append(fragment1);
		m_expr.append(' ');
		m_expr.append(fragment2);
		m_expr.append(')');
		return this;
	}

	/**
	 * Add the specified fragments to the expression.
	 * 
	 * @param fragment1
	 * @param fragment2
	 * @param fragment3
	 * @return this
	 */
	public ZExpression expr(Object fragment1, Object fragment2, Object fragment3) {
		if (m_expr.length() != 0) {
			m_expr.append(" AND ");
		}
		m_expr.append('(');
		m_expr.append(fragment1);
		m_expr.append(' ');
		m_expr.append(fragment2);
		m_expr.append(' ');
		m_expr.append(fragment3);
		m_expr.append(')');
		return this;
	}

	/**
	 * Add the specified fragments to the expression.
	 * 
	 * @param fragments
	 * @return this
	 */
	public ZExpression expr(Object... fragments) {
		if (m_expr.length() != 0) {
			m_expr.append(" AND ");
		}
		m_expr.append('(');
		int i = 0;
		for (Object fragment : fragments) {
			if (i++ > 0) {
				m_expr.append(' ');
			}
			m_expr.append(fragment);
		}
		return this;
	}

	/**
	 * Add a BETWEEN SQL expression.
	 * 
	 * @param element
	 * @param lowLimit
	 * @param highLimit
	 * @return this
	 */
	public final ZExpression between(Object element, Object lowLimit,
			Object highLimit) {
		if (m_expr.length() != 0) {
			m_expr.append(" AND ");
		}
		m_expr.append('(');
		m_expr.append(element);
		m_expr.append(" BETWEEN ");
		m_expr.append(lowLimit);
		m_expr.append(" AND ");
		m_expr.append(highLimit);
		m_expr.append(')');
		return this;
	}

	/**
	 * Add a IN SQL expression.
	 * 
	 * @param element
	 * @param values
	 * @return this
	 */
	public final ZExpression in(Object element, Object... values) {
		if (m_expr.length() != 0) {
			m_expr.append(" AND ");
		}
		m_expr.append('(');
		m_expr.append(element);
		m_expr.append(" IN (");
		int i = 0;
		for (Object value : values) {
			if (i++ > 0) {
				m_expr.append(", ");
			}
			m_expr.append(value);
		}
		m_expr.append("))");
		return this;
	}

	/**
	 * Clear the expression.
	 * 
	 * @return this
	 */
	public ZExpression clear() {
		m_expr.delete(0, m_expr.length());
		return this;
	}

	/**
	 * Get the internal string builder holding the expression.
	 * 
	 * @return the internal string builder holding the expression
	 */
	public StringBuilder getStringBuilder() {
		return m_expr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return m_expr.toString();
	}

	private final static int BUFFER_CAPACITY = 128;

	private StringBuilder m_expr = new StringBuilder(BUFFER_CAPACITY);

}

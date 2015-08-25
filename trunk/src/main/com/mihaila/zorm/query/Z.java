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

/**
 * Contains static functions and constants used for constructing SQL queries.
 */
public class Z {

	/*
	 * SQL KEYWORDS
	 */

	// SQL values
	public final static String NULL = "NULL";

	// Logical operators

	public final static String NOT = "NOT";

	public final static String AND = "AND";

	public final static String OR = "OR";

	// Arithmetic operators

	public final static String PLUS = "+";

	public final static String MINUS = "-";

	public final static String MULTIPLY = "*";

	public final static String DIVIDE = "/";

	// Comparison operators

	public final static String EQUALS = "=";

	public final static String NOT_EQUALS = "<>";

	public final static String LESS = "<";

	public final static String LESS_EQUAL = "<=";

	public final static String GREATER = ">";

	public final static String GREATER_EQUAL = ">=";

	public final static String IS_NULL = "IS NULL";

	public final static String IS_NOT_NULL = "IS NOT NULL";

	public final static String LIKE = "LIKE";

	// Other operators

	public final static String CONCATENATE = "||";

	public final static String IN = "IN";

	public final static String BETWEEN = "BETWEEN";

	// Join kind
	public enum Join {
		INNER, LEFT, RIGHT
	};

	/**
	 * Construct an expression using the specified fragment.
	 * 
	 * @param fragment1
	 * @return the constructed expression
	 */
	public static String expr(Object fragment1) {
		return "(" + fragment1 + ')';
	}

	/**
	 * Construct an expression using the specified fragments.
	 * 
	 * @param fragment1
	 * @param fragment2
	 * @return the constructed expression
	 */
	public static String expr(Object fragment1, Object fragment2) {
		return "(" + fragment1 + ' ' + fragment2 + ')';
	}

	/**
	 * Construct an expression using the specified fragments.
	 * 
	 * @param fragment1
	 * @param fragment2
	 * @param fragment3
	 * @return the constructed expression
	 */
	public static String expr(Object fragment1, Object fragment2,
			Object fragment3) {
		return "(" + fragment1 + ' ' + fragment2 + ' ' + fragment2 + ')';
	}

	/**
	 * Construct an expression using the specified fragments.
	 * 
	 * @param fragments
	 * @return the constructed expression
	 */
	public static String expr(Object... fragments) {
		StringBuilder expr = new StringBuilder();
		expr.append('(');
		int i = 0;
		for (Object fragment : fragments) {
			if (i++ > 0) {
				expr.append(' ');
			}
			expr.append(fragment);
		}
		return expr.toString();
	}

	/**
	 * Construct a BETWEEN SQL expression
	 * 
	 * @param value
	 * @param lowLimit
	 * @param highLimit
	 * @return the constructed expression
	 */
	public final String between(Object value, Object lowLimit, Object highLimit) {
		return "(" + value + " BETWEEN " + lowLimit + " AND " + highLimit + ')';
	}

	/**
	 * Construct an IN SQL expression
	 * 
	 * @param ob
	 * @param values
	 * @return the constructed expression
	 */
	public final String in(Object ob, Object... values) {
		StringBuilder expr = new StringBuilder();
		expr.append('(');
		expr.append(ob);
		expr.append(" IN (");
		int i = 0;
		for (Object value : values) {
			if (i++ > 0) {
				expr.append(", ");
			}
			expr.append(value);
		}
		expr.append("))");
		return expr.toString();
	}

	/**
	 * Returns the sql representation of the the specified object value.
	 * 
	 * @param ob
	 * @return the sql representation of the the specified object value
	 */
	public static String value(Object ob) {
		return com.mihaila.zutil.text.EncodeUtil.sqlEncode(ob.toString());
	}

	/**
	 * Returns a string representing the specified field with DESC keyword
	 * appended, to be used in the SQL ORDER BY clause.
	 * 
	 * @param field
	 * @return a string representing the specified field with DESC keyword
	 *         appended
	 */
	public static String desc(Object field) {
		return field.toString() + " DESC";
	}

	/**
	 * Returns a string representing the specified object (e.g. table field or
	 * table name) prefixed with "t[index]_" to be used in SQL queries. Example:
	 * <code>other("item", 3)</code> will return "t3_item".
	 * 
	 * @param ob
	 * @param index
	 * @return a string representing the specified object (e.g. table field or
	 *         table name) prefixed with "t[index]_" to be used in SQL queries
	 */
	public static String other(Object ob, int index) {
		return 't' + String.valueOf(index) + '_' + ob;
	}

	/**
	 * Returns a string representing the specified object (e.g. table field or
	 * table name) prefixed with "t2_" to be used in SQL queries. Example:
	 * <code>second("article")</code> will return "t2_article".
	 * 
	 * @param value
	 * @return a string representing the specified object (e.g. table field or
	 *         table name) prefixed with "t2_" to be used in SQL queries
	 */
	public static String second(Object value) {
		return other(value, 2);
	}

}

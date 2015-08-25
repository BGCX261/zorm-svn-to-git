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

package com.mihaila.zormtest;

import static com.mihaila.zorm.query.Z.EQUALS;
import static com.mihaila.zorm.query.Z.desc;
import static com.mihaila.zorm.query.Z.Join.LEFT;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mihaila.zorm.ZField;
import com.mihaila.zorm.query.ZSelectQuery;
import com.mihaila.zormtest.helper.Item;
import com.mihaila.zormtest.helper.User;


public class QueryBuildingTest {

	@Test
	/**
	 * Test the following query construction features: select specifying fields, distinct, where, groupBy, having, order join, skip,
	 * take.
	 */
	public void testFeatures1() {
		String s = new ZSelectQuery()
				.select(Item.META,	new ZField[] { Item.NAME })
				.distinct()
				.where(Item.ACTIVE, EQUALS, Item.ACTIVE.toSqlValue(true))
				.whereBetween(Item.RATING, 5, 10)
				.groupBy(Item.NAME, Item.RATING)
				.having(Item.ACTIVE, EQUALS, Item.ACTIVE.toSqlValue(false))
				.orderBy(Item.ID, desc(Item.NAME))
				.skip(10)
				.take(20)
				.toString();
		Assert.assertEquals(s, "SELECT DISTINCT i.id, i.name"
				+ " FROM item i"
				+ " WHERE (i.active = 1) AND (i.rating BETWEEN 5 AND 10)"
				+ " GROUP BY i.name, i.rating"
				+ " HAVING (i.active = 0)"
				+ " ORDER BY i.id, i.name DESC"
				+ " LIMIT 10, 20"
			);
		
	}
	
	@Test
	/**
	 * Test the following query construction features: autoAddToFrom, join, 
	 * take without skip.
	 */
	public void testFeatures2() {
		String s = new ZSelectQuery()
				.autoAddToFrom(false)
				.select(Item.META)
				.from(Item.META)
				.join(LEFT, User.META, Item.AUTHOR_ID, User.ID)
				.where(Item.ACTIVE, EQUALS, Item.ACTIVE.toSqlValue(true))
				.take(30)
				.toString();
		Assert.assertEquals(s, "SELECT i.id, i.name, i.rating, i.author_id"
				+ " FROM item i LEFT JOIN user u ON (i.author_id = u.id)"
				+ " WHERE (i.active = 1)"
				+ " LIMIT 30"
			);
	}

}

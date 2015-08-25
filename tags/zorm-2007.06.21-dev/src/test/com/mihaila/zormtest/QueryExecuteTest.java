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
import static com.mihaila.zorm.query.Z.second;
import static com.mihaila.zorm.query.Z.value;
import static com.mihaila.zorm.query.Z.Join.LEFT;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mihaila.zormtest.helper.Item;
import com.mihaila.zormtest.helper.User;
import com.mihaila.zormtest.helper.ZormDbTest;


/**
 * Test the query API execute functionality.
 */
public class QueryExecuteTest extends ZormDbTest {

	@Test
	public void testExecuteUnique() {
		// this query actualy returns 2 rows
		Item item = (Item) getSession().getSelectQuery()
			.select(Item.META)
			.select(Item.RATING)
			.select(User.META)
			.where(Item.ID, EQUALS, value(1))
			.where(Item.ID, EQUALS, 1)
			.executeUnique();

		Assert.assertTrue(item.isFieldInitialized(Item.ID));
		Assert.assertTrue(item.isFieldInitialized(Item.NAME));
		Assert.assertTrue(item.isFieldInitialized(Item.RATING));
		Assert.assertFalse(item.isFieldInitialized(Item.ACTIVE));
		Assert.assertTrue(item.isFieldInitialized(Item.AUTHOR_ID));
		Assert.assertEquals(item.getId(), "1");
		Assert.assertEquals(item.getName(), "item1");
		Assert.assertEquals(item.getRating(), 2);
		Assert.assertEquals(item.getAuthorId(), "john");
	}

	@Test
	public void testExecuteUniqueSelect() {
		Object[] items = getSession().getSelectQuery()
				.autoAddToFrom(false)
				.select(Item.META)
				.select(Item.RATING)
				.select(User.META)
				.from(Item.META)
				.join(LEFT, User.META, Item.AUTHOR_ID, User.ID)
				.whereIn(Item.ID, 1, 2)
				.executeUniqueSelect();

		Assert.assertEquals(items.length, 2);
		Item item = (Item) items[0];
		Assert.assertEquals(item.getId(), "1");
		Assert.assertEquals(item.getName(), "item1");
		Assert.assertEquals(item.getRating(), 2);
		Assert.assertEquals(item.getAuthorId(), "john");
		Item item2 = (Item) items[1];
		Assert.assertEquals(item2.getId(), "2");
		Assert.assertEquals(item2.getName(), "item2");
		Assert.assertEquals(item2.getRating(), 3);
		Assert.assertEquals(item2.getAuthorId(), null);
	}

	@Test
	public void testExecuteUniqueRow() {
		Map<String, Object> result = getSession().getSelectQuery().autoAddToFrom(false)
				.select(Item.META)
				.select(Item.RATING)
				.select(User.NAME, "test")
				.select("1+1")
				.select("1+2", "test2")
				.select(User.META).from(Item.META)
				.join(LEFT, User.META, Item.AUTHOR_ID, User.ID)
				.where(Item.ID, EQUALS, 1)
				.executeUniqueRow();

		Assert.assertEquals(result.size(), 6);

		Item item = (Item) result.get(Item.META.toString());
		Assert.assertEquals(item.getId(), "1");
		Assert.assertEquals(item.getName(), "item1");
		Assert.assertEquals(item.getRating(), 2);
		Assert.assertEquals(item.getAuthorId(), "john");

		int itemRating = (Integer) result.get(Item.RATING.getName());
		Assert.assertEquals(itemRating, 2);

		String test = (String) result.get("test");
		Assert.assertEquals(test, "John Doe");

		long onePlusOne = (Long) result.get("1+1");
		Assert.assertEquals(onePlusOne, 2);

		long test2 = (Long) result.get("test2");
		Assert.assertEquals(test2, 3);

		User user = (User) result.get(User.META.toString());
		Assert.assertEquals(user.getId(), "john");
		Assert.assertEquals(user.getName(), "John Doe");
	}

	@Test
	public void testExecute() {
		Map<String, Object[]> result = getSession().getSelectQuery()
				.autoAddToFrom(false)
				.select(Item.META, second(Item.META))
				.select(User.META)
				.select("1+1")
				.from(Item.META, second(Item.META))
				.join(LEFT, User.META, second(Item.AUTHOR_ID), User.ID)
				.whereIn(second(Item.ID), 1, 2)
				.execute();

		Assert.assertEquals(result.size(), 3);
		
		Object[] items = result.get(second(Item.META));
		Assert.assertEquals(items.length, 2);
		Item item = (Item) items[0];
		Assert.assertEquals(item.getId(), "1");
		Assert.assertEquals(item.getName(), "item1");
		Assert.assertEquals(item.getRating(), 2);
		Assert.assertEquals(item.getAuthorId(), "john");
		Item item2 = (Item) items[1];
		Assert.assertEquals(item2.getId(), "2");
		Assert.assertEquals(item2.getName(), "item2");
		Assert.assertEquals(item2.getRating(), 3);
		Assert.assertEquals(item2.getAuthorId(), null);

		Object[] users = result.get(User.META.toString());
		Assert.assertEquals(users.length, 2);
		User user = (User) users[0];
		Assert.assertEquals(user.getId(), "john");
		Assert.assertEquals(user.getName(), "John Doe");
		User user2 = (User) users[1];
		Assert.assertNull(user2);

		Object[] onePlusOnes = result.get("1+1");
		Assert.assertEquals(onePlusOnes.length, 2);
		long onePlusOne = (Long) onePlusOnes[0];
		Assert.assertEquals(onePlusOne, 2);
		long onePlusOne2 = (Long) onePlusOnes[01];
		Assert.assertEquals(onePlusOne2, 2);
	}

	@Test
	public void testCustomQuery() {
		Map<String, Object> result = getSession().getSelectQuery()
				.customQuery("SELECT i.id, i.name, i.rating, i.author_id, u.name authorName"
						+ " FROM item i LEFT JOIN user u ON (i.author_id = u.id)"
						+ " WHERE (i.id = 1)")
				.select(Item.META)
				.executeUniqueRow();

		Assert.assertEquals(result.size(), 2);

		Item item = (Item) result.get(Item.META.toString());
		Assert.assertEquals(item.getId(), "1");
		Assert.assertEquals(item.getName(), "item1");
		Assert.assertEquals(item.getRating(), 2);
		Assert.assertEquals(item.getAuthorId(), "john");

		String authorName  = (String) result.get("authorName");
		Assert.assertEquals(authorName, "John Doe");

	}
}

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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.mihaila.zorm.ZField;
import com.mihaila.zorm.ZSession;
import com.mihaila.zormtest.helper.Item;
import com.mihaila.zormtest.helper.ZormDbTest;


/**
 * Test the get functionality.
 * 
 * <pre>
 *     Used data:
 *     Item1:
 *     id  name   rating  active  author_id
 *     1   item1  2       1       john
 *     2   item2  3       0       \N
 * </pre>
 * 
 */
public class GetTest extends ZormDbTest {

	@Test
	/**
	 * Test basic </code>Session.get(ZPersistentMeta meta, String id)</code>
	 * operation.
	 */
	public void testGetBasic() {
		ZSession session = getSession();
		int numQueries = 0;
		Item item = (Item) session.get(Item.META, "1");
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		Assert.assertTrue(item.isFieldInitialized(Item.ID));
		Assert.assertTrue(item.isFieldInitialized(Item.NAME));
		Assert.assertTrue(item.isFieldInitialized(Item.RATING));
		Assert.assertFalse(item.isFieldInitialized(Item.ACTIVE));
		Assert.assertTrue(item.isFieldInitialized(Item.AUTHOR_ID));
		Assert.assertEquals(item.getId(), "1");
		Assert.assertEquals(item.getName(), "item1");
		Assert.assertEquals(item.getRating(), 2);
		Assert.assertEquals(item.getAuthorId(), "john");

		Item item2 = (Item) session.get(Item.META, "2");
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		Assert.assertTrue(item2.isFieldInitialized(Item.ID));
		Assert.assertTrue(item2.isFieldInitialized(Item.NAME));
		Assert.assertTrue(item2.isFieldInitialized(Item.RATING));
		Assert.assertFalse(item2.isFieldInitialized(Item.ACTIVE));
		Assert.assertTrue(item2.isFieldInitialized(Item.AUTHOR_ID));
		Assert.assertEquals(item2.getId(), "2");
		Assert.assertEquals(item2.getName(), "item2");
		Assert.assertEquals(item2.getRating(), 3);
		Assert.assertEquals(item2.getAuthorId(), null);
	}

	@Test
	/**
	 * Test </code>Session.get(ZPersistentMeta meta, String id, ZField[]
	 * fields)</code>.
	 */
	public void testGetWithSpecifiedFelds() {
		ZSession session = getSession();
		int numQueries = 0;
		Item item = (Item) session.get(Item.META, "1", new ZField[] { Item.ID,
				Item.ACTIVE, Item.AUTHOR_ID });
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		Assert.assertTrue(item.isFieldInitialized(Item.ID));
		Assert.assertFalse(item.isFieldInitialized(Item.NAME));
		Assert.assertFalse(item.isFieldInitialized(Item.RATING));
		Assert.assertTrue(item.isFieldInitialized(Item.ACTIVE));
		Assert.assertTrue(item.isFieldInitialized(Item.AUTHOR_ID));
		Assert.assertEquals(item.getId(), "1");
		Assert.assertEquals(item.isActive(), true);
		Assert.assertEquals(item.getAuthorId(), "john");

		Item item2 = (Item) session.get(Item.META, "2", new ZField[] {
				Item.NAME, Item.AUTHOR_ID });
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		Assert.assertTrue(item2.isFieldInitialized(Item.ID));
		Assert.assertTrue(item2.isFieldInitialized(Item.NAME));
		Assert.assertFalse(item2.isFieldInitialized(Item.RATING));
		Assert.assertFalse(item2.isFieldInitialized(Item.ACTIVE));
		Assert.assertTrue(item2.isFieldInitialized(Item.AUTHOR_ID));
		Assert.assertEquals(item2.getId(), "2");
		Assert.assertEquals(item2.getName(), "item2");
		Assert.assertEquals(item2.getAuthorId(), null);

	}

	@Test
	/**
	 * Test </code>Session.getShallow(ZPersistentMeta meta, String id)</code>
	 * operation.
	 */
	public void testGetShallow() {
		ZSession session = getSession();
		int numQueries = 0;
		Item item = (Item) session.getShallow(Item.META, "1");
		Assert.assertEquals(session.getNumQueries(), numQueries);
		Assert.assertTrue(item.isFieldInitialized(Item.ID));
		Assert.assertFalse(item.isFieldInitialized(Item.NAME));
		Assert.assertFalse(item.isFieldInitialized(Item.RATING));
		Assert.assertFalse(item.isFieldInitialized(Item.ACTIVE));
		Assert.assertFalse(item.isFieldInitialized(Item.AUTHOR_ID));
	}

	@Test
	/**
	 * Test if objects are retrieved from cache when using
	 * <code>Session.get(ZPersistentMeta meta, String id)</code> the second
	 * time.
	 */
	public void testObjectCache() {
		ZSession session = getSession();
		int numQueries = 0;
		Item item = (Item) session.get(Item.META, "1");
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		Item sameItem = (Item) session.get(Item.META, "1");
		Assert.assertEquals(session.getNumQueries(), numQueries);
		Assert.assertEquals(item, sameItem);
		session.dettachAll();
		item = (Item) session.get(Item.META, "1");
		Assert.assertEquals(session.getNumQueries(), ++numQueries);

		Item item2 = (Item) session.get(Item.META, "2");
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		Item sameItem2 = (Item) session.get(Item.META, "2");
		Assert.assertEquals(numQueries, session.getNumQueries());
		Assert.assertEquals(item2, sameItem2);
	}

	@Test(expectedExceptions = com.mihaila.zorm.exception.ZormPersistentObjectNotFoundException.class)
	/**
	 * Test <code>Session.get(ZPersistentMeta meta, String id)</code> to
	 * retrive a non-existing persistent object.
	 */
	public void testNonExisting() {
		getSession().get(Item.META, "0");
	}

	@Test
	public void testFetch() {
		ZSession session = getSession();
		int numQueries = 0;
		Item item = (Item) session.get(Item.META, "1",
				new ZField[] { Item.NAME, });
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		Assert.assertTrue(item.isFieldInitialized(Item.ID));
		Assert.assertTrue(item.isFieldInitialized(Item.NAME));
		Assert.assertFalse(item.isFieldInitialized(Item.RATING));
		Assert.assertFalse(item.isFieldInitialized(Item.ACTIVE));
		Assert.assertFalse(item.isFieldInitialized(Item.AUTHOR_ID));
		Assert.assertEquals(item.getName(), "item1");
		item.fetch(new ZField[] { Item.RATING });
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		item.fetch(new ZField[] { Item.RATING });
		Assert.assertEquals(session.getNumQueries(), numQueries);
		Assert.assertTrue(item.isFieldInitialized(Item.ID));
		Assert.assertTrue(item.isFieldInitialized(Item.NAME));
		Assert.assertTrue(item.isFieldInitialized(Item.RATING));
		Assert.assertFalse(item.isFieldInitialized(Item.ACTIVE));
		Assert.assertFalse(item.isFieldInitialized(Item.AUTHOR_ID));
		Assert.assertEquals(item.getRating(), 2);
		// write authorId to test that fetch does not overwrite this
		item.setAuthorId("janet");
		item.fetch(Item.META.getAllFields());
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		Assert.assertTrue(item.isFieldInitialized(Item.ID));
		Assert.assertTrue(item.isFieldInitialized(Item.NAME));
		Assert.assertTrue(item.isFieldInitialized(Item.RATING));
		Assert.assertTrue(item.isFieldInitialized(Item.ACTIVE));
		Assert.assertTrue(item.isFieldInitialized(Item.AUTHOR_ID));
		Assert.assertTrue(item.isFieldModified(Item.AUTHOR_ID));
		Assert.assertEquals(item.isActive(), true);
		Assert.assertEquals(item.getAuthorId(), "janet");
	}
}

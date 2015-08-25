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

import com.mihaila.zorm.ZSession;
import com.mihaila.zorm.exception.ZormPersistentObjectNotFoundException;
import com.mihaila.zormtest.helper.Item;
import com.mihaila.zormtest.helper.ZormDbTest;


/**
 * Test the delete functionality.
 */
public class DeleteTest extends ZormDbTest {

	@Test
	/**
	 * Test <code>item.delete()</code> when the object exists.
	 */
	public void testDeleteExisting() {
		ZSession session = getSession();
		int numQueries = 0;
		Item item = (Item) session.get(Item.META, "1");
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		boolean deleted = item.delete();
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		Assert.assertTrue(deleted);
		boolean objectNotFound = false;
		try {
			item = (Item) session.get(Item.META, "1");
		}
		catch (ZormPersistentObjectNotFoundException e) {
			objectNotFound = false;
		}
		Assert.assertFalse(objectNotFound);
	}
	@Test
	/**
	 * Test </code>item.delete()</code> when the object does not exist.
	 */
	public void testDeleteNonexisting() {
		ZSession session = getSession();
		int numQueries = 0;
		Item item = (Item) session.getShallow(Item.META, "0");
		Assert.assertEquals(session.getNumQueries(), numQueries);
		boolean deleted = item.delete();
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		Assert.assertFalse(deleted);
	}

}

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

import com.mihaila.zorm.ZManager;
import com.mihaila.zorm.ZSession;
import com.mihaila.zormtest.helper.Item;
import com.mihaila.zormtest.helper.ZormDbTest;


public class BasicTest extends ZormDbTest {

	@Test
	public void testValidSession() {
		ZSession session = ZManager.getNewSession(getConnection());
		Assert.assertNotNull(session);
		Assert.assertNotNull(session.getSqlConnection());
		session.close();
		Assert.assertNotNull(session.isClosed());
	}

	@Test
	public void testCommit() {
		ZSession session = getSession();
		int numQueries = 0;
		Item item = (Item) session.get(Item.META, "1");
		Assert.assertEquals(item.getId(), "1");
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		item.setName("testtest2");
		// commit will actually do a rollback in testing (to avoid corrupting
		// the database)
		session.saveAllAndCommit();
		Assert.assertEquals(session.getNumQueries(), ++numQueries);

		Item item2 = (Item) session.get(Item.META, "2");
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		Assert.assertEquals(item2.getId(), "2");
		session.close();
	}

	@Test
	public void testRollback() {
		ZSession session = getSession();
		int numQueries = 0;
		Item item = (Item) session.get(Item.META, "1");
		Assert.assertEquals(item.getId(), "1");
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		item.setName("testtest2");
		item.save();
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		session.rollback();
		Item item2 = (Item) session.get(Item.META, "2");
		Assert.assertEquals(session.getNumQueries(), ++numQueries);
		Assert.assertEquals(item2.getId(), "2");
		session.close();
	}
}

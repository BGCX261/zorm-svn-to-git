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

import com.mihaila.zormtest.helper.Item;


public class WithoutSessionTest {

	@Test
	/**
	 * Check the correct position of the fields in META.
	 */
	public void testMeta() {
		Assert.assertEquals(Item.META.getTableName(), "item");
		Assert.assertEquals(Item.ID.getIndex(), 0);
		Assert.assertEquals(Item.NAME.getIndex(), 1);
		Assert.assertEquals(Item.RATING.getIndex(), 2);
		Assert.assertEquals(Item.ACTIVE.getIndex(), 3);
		Assert.assertEquals(Item.AUTHOR_ID.getIndex(), 4);
	}

	@Test
	/**
	 * Test property accesses.
	 */
	public void testBasicPropertyAcess() {
		Item item = new Item();
		// all fields are not initialized and not modified in a new object
		Assert.assertFalse(item.isFieldInitialized(Item.ID));
		Assert.assertFalse(item.isFieldInitialized(Item.NAME));
		Assert.assertFalse(item.isFieldInitialized(Item.RATING));
		Assert.assertFalse(item.isFieldInitialized(Item.ACTIVE));
		Assert.assertFalse(item.isFieldInitialized(Item.AUTHOR_ID));
		Assert.assertFalse(item.isFieldModified(Item.ID));
		Assert.assertFalse(item.isFieldModified(Item.NAME));
		Assert.assertFalse(item.isFieldModified(Item.RATING));
		Assert.assertFalse(item.isFieldModified(Item.ACTIVE));
		Assert.assertFalse(item.isFieldModified(Item.AUTHOR_ID));
		item.setId("10");
		Assert.assertTrue(item.isFieldInitialized(Item.ID));
		Assert.assertTrue(item.isFieldModified(Item.ID));
		item.setName("name1");
		Assert.assertTrue(item.isFieldInitialized(Item.NAME));
		Assert.assertTrue(item.isFieldModified(Item.NAME));
		item.setRating(10);
		Assert.assertTrue(item.isFieldInitialized(Item.RATING));
		Assert.assertTrue(item.isFieldModified(Item.RATING));

		item.clearAllFieldValues();
		Assert.assertFalse(item.isFieldInitialized(Item.ID));
		Assert.assertFalse(item.isFieldInitialized(Item.NAME));
		Assert.assertFalse(item.isFieldInitialized(Item.RATING));
		Assert.assertFalse(item.isFieldInitialized(Item.ACTIVE));
		Assert.assertFalse(item.isFieldInitialized(Item.AUTHOR_ID));
		Assert.assertFalse(item.isFieldModified(Item.ID));
		Assert.assertFalse(item.isFieldModified(Item.NAME));
		Assert.assertFalse(item.isFieldModified(Item.RATING));
		Assert.assertFalse(item.isFieldModified(Item.ACTIVE));
		Assert.assertFalse(item.isFieldModified(Item.AUTHOR_ID));
	}
}

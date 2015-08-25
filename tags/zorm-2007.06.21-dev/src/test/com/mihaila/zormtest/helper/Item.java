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

package com.mihaila.zormtest.helper;

import com.mihaila.zorm.ZPersistent;
import com.mihaila.zorm.ZPersistentMeta;
import com.mihaila.zorm.field.*;


public final class Item extends ZPersistent {

	public final static ZPersistentMeta META = new ZPersistentMeta(Item.class,
			"item", "i");

	public final static ZStringIntField ID = new ZStringIntField("id");

	public final static ZStringField NAME = new ZStringField("name");

	public final static ZIntField RATING = new ZIntField("rating");

	public final static ZBooleanField ACTIVE = new ZBooleanField("active");

	public final static ZStringField AUTHOR_ID = new ZStringField("author_id");

	static { // fields initialization
		ID.setAutoGenerated(true);
		RATING.setAutoGenerated(true);
		ACTIVE.setAutoFetched(false);
		AUTHOR_ID.setNullValid(true);
		META.setFields(ID, NAME, RATING, ACTIVE, AUTHOR_ID);
	}

	@Override
	public ZPersistentMeta getMeta() {
		return META;
	}

	public void setId(String id) {
		ID.setValue(this, id);
	}

	public String getName() {
		return NAME.getValue(this);
	}

	public void setName(String value) {
		NAME.setValue(this, value);
	}

	public int getRating() {
		return RATING.getValue(this);
	}

	public void setRating(int value) {
		RATING.setValue(this, value);
	}

	public boolean isActive() {
		return ACTIVE.getValue(this);
	}

	public void setActive(boolean value) {
		ACTIVE.setValue(this, value);
	}

	public String getAuthorId() {
		return AUTHOR_ID.getValue(this);
	}

	public void setAuthorId(String value) {
		AUTHOR_ID.setValue(this, value);
	}

	public User getAuthor() {
		return (User)getSession().get(User.META, getAuthorId());
	}

	public void setAuthor(User author) {
		setAuthorId(author.getId());
	}

}

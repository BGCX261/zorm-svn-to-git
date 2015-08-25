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

package com.mihaila.zorm;

import java.sql.Connection;

/**
 * Implementds ThreadLocal pattern for <code>ZSession</code> objects..
 */
public class ZManagerThreadLocal {

	/**
	 * Holds the thread local <code>ZSession</code> objects.
	 */
	private static ThreadLocal<ZSession> m_threadLocalSessions = new ThreadLocal<ZSession>();

	/**
	 * Returns a new <code>ZSession</code> associated with this thread. The
	 * previously opened session (if any) is automatically closed.
	 * 
	 * @return a new <code>ZSession</code> associated with this thread
	 */
	public static ZSession getNewSession() {
		closeSession();
		ZSession session = new ZSession();
		m_threadLocalSessions.set(session);
		return session;
	}

	/**
	 * Returns a new <code>ZSession</code> associated with this thread with
	 * the specifed SQL connection. The previously opened session (if any) is
	 * automatically closed.
	 * 
	 * @param sqlConn
	 * @return a new <code>ZSession</code> associated with this thread with
	 *         the specifed SQL connection
	 */
	public static ZSession getNewSession(Connection sqlConn) {
		ZSession session = getNewSession();
		session.setSqlConnection(sqlConn);
		return session;
	}

	/**
	 * Returns the <code>ZSession</code> associated with this thread (if the
	 * <code>ZSession</code> object does not exist, a new one is created.
	 * 
	 * @return the <code>ZSession</code> associated with this thread
	 */
	public static ZSession getSession() {
		return m_threadLocalSessions.get();
	}

	/**
	 * Close the <code>ZSession</code> associated with this thread.
	 */
	public static void closeSession() {
		ZSession session = m_threadLocalSessions.get();
		if (session != null) {
			session.close();
			m_threadLocalSessions.remove();
		}
	}

}

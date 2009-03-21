/*
 * @(#)ConsoleLogger.java
 * @author  Kenneth J. Pouncey
 *
 * Copyright:    Copyright (c) 2001, 2002, 2003
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.tools.logging;

/**
 * An implementation of the TN5250jLogger to provide logger instances to the
 * console - System.out or System.err.
 */
public final class ConsoleLogger implements TN5250jLogger {

	private int logLevel = TN5250jLogger.WARN;

	private String clazz = null;

	/*
	 * Package level access only  
	 */
	ConsoleLogger() {

	}

	public void initialize(final String clazz) {
		this.clazz = clazz;
	}

	public void debug(Object message) {
		if (isDebugEnabled())
			System.out.println("DEBUG [" + clazz + "] " + message);
	}

	public void debug(Object message, Throwable throwable) {
		if (isDebugEnabled())
			System.out.println("DEBUG ["
					+ clazz
					+ "] "
					+ new StringBuffer(32).append(message).append(
							throwable.getMessage()));
	}
	
	public void info(Object message) {
		if (isInfoEnabled())
			System.out.println("INFO [" + clazz + "] " + message);

	}

	public void info(Object message, Throwable throwable) {
		if (isInfoEnabled())
			System.out.println("INFO ["
					+ clazz
					+ "] "
					+ new StringBuffer(32).append(message).append(
							throwable.getMessage()));
	}
	
	public void warn(Object message) {
		if (isWarnEnabled())
			System.out.println("WARN [" + clazz + "] " + message);

	}

	public void warn(Object message, Throwable throwable) {
		if (isWarnEnabled())
			System.out.println("WARN ["
					+ clazz
					+ "] "
					+ new StringBuffer(32).append(message).append(
							throwable.getMessage()));
	}

	public void error(Object message) {
		if (isErrorEnabled())
			System.err.println("ERROR [" + clazz + "] " + message);

	}

	public void error(Object message, Throwable throwable) {
		if (isErrorEnabled())
			System.out.println("ERROR ["
					+ clazz
					+ "] "
					+ new StringBuffer(32).append(message).append(
							throwable.getMessage()));
	}

	public void fatal(Object message) {
		if (isFatalEnabled())
			System.err.println("FATAL [" + clazz + "] " + message);

	}

	public void fatal(Object message, Throwable throwable) {
		if (isFatalEnabled())
			System.out.println("FATAL ["
					+ clazz
					+ "] "
					+ new StringBuffer(32).append(message).append(
							throwable.getMessage()));
	}

	public boolean isErrorEnabled() {
		return (logLevel >= ERROR);
	}

	public boolean isFatalEnabled() {
		return (logLevel >= FATAL);
	}

	public boolean isWarnEnabled() {
		return (logLevel >= WARN);
	}

	public boolean isDebugEnabled() {
		return (logLevel >= DEBUG);
	}

	public boolean isInfoEnabled() {
		return (logLevel >= INFO);
	}

	public int getLevel() {
		return logLevel;
	}

	public void setLevel(int newLevel) {
		logLevel = newLevel;
	}

}
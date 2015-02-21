/*
 * @(#)Log4jLogger.java
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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * An implementation of the TN5250jLogger to provide log4j logger instances.
 */
public final class Log4jLogger implements TN5250jLogger {

	private Logger log = null;

	/*
	 * Package level access only
	 */
	Log4jLogger() {

	}

	public void initialize(final String clazz) {
		log = Logger.getLogger(clazz);
	}

	public void debug(Object message) {
		log.debug(message);
	}

	public void debug(Object message, Throwable throwable) {
		log.debug(message, throwable);
	}

	public void info(Object message) {
		log.info(message);
	}
	
	public void info(Object message, Throwable throwable) {
		log.info(message, throwable);
	}

	public void warn(Object message) {
		log.warn(message);
	}

	public void warn(Object message, Throwable throwable) {
		log.warn(message, throwable);
	}

	public void error(Object message) {
		log.error(message);
	}
	
	public void error(Object message, Throwable throwable) {
		log.error(message, throwable);
	}

	public void fatal(Object message) {
		log.fatal(message);
	}
	
	public void fatal(Object message, Throwable throwable) {
		log.fatal(message, throwable);
	}

	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}
	
	public boolean isWarnEnabled() {
		return (Level.WARN.equals(log.getLevel()));
	}

	public boolean isFatalEnabled() {
		return (Level.FATAL.equals(log.getLevel()));
	}
	
	public boolean isErrorEnabled() {
		return (Level.ERROR.equals(log.getLevel()));
	}
	
	public void setLevel(int newLevel) {

		switch (newLevel) {
		case OFF:
			log.setLevel(Level.OFF);
			break;

		case DEBUG:
			log.setLevel(Level.DEBUG);
			break;

		case INFO:
			log.setLevel(Level.INFO);
			break;

		case WARN:
			log.setLevel(Level.WARN);
			break;

		case ERROR:
			log.setLevel(Level.ERROR);
			break;

		case FATAL:
			log.setLevel(Level.FATAL);
			break;
		}

	}

	public int getLevel() {

		switch (log.getLevel().toInt()) {

		case (org.apache.log4j.Level.DEBUG_INT):
			return DEBUG;

		case (org.apache.log4j.Level.INFO_INT):
			return INFO;

		case (org.apache.log4j.Level.WARN_INT):
			return WARN;

		case (org.apache.log4j.Level.ERROR_INT):
			return ERROR;

		case (org.apache.log4j.Level.FATAL_INT):
			return FATAL;
		default:
			return WARN;
		}

	}
}
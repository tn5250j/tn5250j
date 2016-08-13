package org.tn5250j.logging;

import org.tn5250j.Session5250;

/**
 * Default implementation of LoggingListenerFactory that provides
 * an HTML logging listener.
 */
public class DefaultLoggingListenerFactory implements LoggingListenerFactory{

	@Override
	public LoggingListener createInstance(final Session5250 session){
		return new HTMLLoggingListener(session);
	}
}

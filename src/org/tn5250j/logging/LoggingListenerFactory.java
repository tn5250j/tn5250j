package org.tn5250j.logging;

import org.tn5250j.Session5250;

/**
 *   Interface to support pluggable logging listeners.
 */
public interface LoggingListenerFactory {

	LoggingListener createInstance(Session5250 session);
}

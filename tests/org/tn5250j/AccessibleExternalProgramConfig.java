/**
 *
 */
package org.tn5250j;

import java.util.Properties;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
class AccessibleExternalProgramConfig extends ExternalProgramConfig {
    static final String UNIX_SUFFIX = ExternalProgramConfig.UNIX_SUFFIX;
    static final String WINDOW_SUFFIX = ExternalProgramConfig.WINDOW_SUFFIX;
    static final String NAME_SUFFIX = ExternalProgramConfig.NAME_SUFFIX;
    static final String PREFIX = ExternalProgramConfig.PREFIX;

    Properties getEtnPgmProps() {
        return etnPgmProps;
    }

    @Override
    protected Properties loadExternalProgramSettings() {
        // not load
        return new Properties();
    }
}

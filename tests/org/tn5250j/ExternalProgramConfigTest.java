/**
 *
 */
package org.tn5250j;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.tn5250j.AccessibleExternalProgramConfig.NAME_SUFFIX;
import static org.tn5250j.AccessibleExternalProgramConfig.PREFIX;
import static org.tn5250j.AccessibleExternalProgramConfig.UNIX_SUFFIX;
import static org.tn5250j.AccessibleExternalProgramConfig.WINDOW_SUFFIX;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.tn5250j.connectdialog.ExternalProgram;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ExternalProgramConfigTest {
    private AccessibleExternalProgramConfig config;

    @Before
    public void setUp() {
        config = new AccessibleExternalProgramConfig();
    }

    @Test
    public void testAddProgram() {
        final String name = "junit";
        final ExternalProgram program = new ExternalProgram(name, "", "");
        config.programUpdated(program);

        assertEquals(1, config.getPrograms().size());

        final Properties props = config.getEtnPgmProps();
        assertEquals(name, props.get(PREFIX + 0 + NAME_SUFFIX));
        assertNotNull(props.get(PREFIX + 0 + WINDOW_SUFFIX));
        assertNotNull(props.get(PREFIX + 0 + UNIX_SUFFIX));
    }
    @Test
    public void testAddProgramOrder() {
        final String a = "a";
        final String b = "b";

        config.programUpdated(new ExternalProgram(b, "", ""));
        config.programUpdated(new ExternalProgram(a, "", ""));

        assertEquals(a, config.getPrograms().get(0).getName());
        assertEquals(b, config.getPrograms().get(1).getName());

        final Properties props = config.getEtnPgmProps();

        assertEquals(a, props.get(PREFIX + 0 + NAME_SUFFIX));
        assertEquals(b, props.get(PREFIX + 1 + NAME_SUFFIX));
    }
    @Test
    public void testUpdateProgram() {
        final String name = "junit";
        final String wCommand = "wCommand";
        final String uCommand = "uCommand";

        config.programUpdated(new ExternalProgram(name, "", ""));
        config.programUpdated(new ExternalProgram(name, wCommand, uCommand));

        assertEquals(1, config.getPrograms().size());

        final Properties props = config.getEtnPgmProps();
        assertEquals(name, props.get(PREFIX + 0 + NAME_SUFFIX));
        assertEquals(wCommand, props.get(PREFIX + 0 + WINDOW_SUFFIX));
        assertEquals(uCommand, props.get(PREFIX + 0 + UNIX_SUFFIX));
    }
    @Test
    public void testRemoveProgram() {
        final String a = "a";
        final String b = "b";
        final String wCommand = "wCommand";
        final String uCommand = "uCommand";

        config.programUpdated(new ExternalProgram(b, wCommand, uCommand));
        config.programUpdated(new ExternalProgram(a, "", ""));

        config.remove(a);

        assertEquals(b, config.getPrograms().get(0).getName());

        final Properties props = config.getEtnPgmProps();

        assertEquals(b, props.get(PREFIX + 0 + NAME_SUFFIX));
        assertEquals(wCommand, props.get(PREFIX + 0 + WINDOW_SUFFIX));
        assertEquals(uCommand, props.get(PREFIX + 0 + UNIX_SUFFIX));
    }
}

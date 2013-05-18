package org.tn5250j;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class My5250Test {

	@Test
	public void loadLastSessionViewNamesFrom_loads_session_names_from_command_line() {
		String[] commandlineArgs = new String[] {
				"-s", "foo 1",
				"-h",
				"-s", "-s",
				"-s", "foobar"};
		List<String> sessionNames = My5250.loadLastSessionViewNamesFrom(commandlineArgs);

		String[] expected = new String[] { "foo 1", "foobar"};
		assertArrayEquals(expected, sessionNames.toArray());
	}

	@Test
	public void loadLastSessionViewNames_is_able_to_load_session_names_containing_blanks() {
		My5250.getSessions().setProperty("emul.startLastView", "");
		My5250.getSessions().setProperty("emul.view", "-s foo 1  -s foo bar -s lastone ");

		List<String> sessionNames = My5250.loadLastSessionViewNames();

		String[] expected = new String[] { "foo 1", "foo bar", "lastone" };
		assertArrayEquals(expected, sessionNames.toArray());
	}

	@Test
	public void loadLastSessionViewNames_will_return_emptyList_if_option_startLastView_is_null() {
		My5250.getSessions().remove("emul.startLastView");
		My5250.getSessions().setProperty("emul.view", "-s foo 1  -s foo bar -s lastone ");

		List<String> sessionNames = My5250.loadLastSessionViewNames();

		assertEquals(0, sessionNames.size());
	}
	
	@Test
	public void loadLastSessionViewNames_will_return_emptyList_if_no_session_was_saved() {
		My5250.getSessions().setProperty("emul.startLastView", "");
		My5250.getSessions().setProperty("emul.view", "");

		List<String> sessionNames = My5250.loadLastSessionViewNames();

		assertEquals(0, sessionNames.size());
	}
	
	@Test
	public void filterExistingViewNames_only_passes_ViewNames_with_existing_configuration() {
		My5250.getSessions().setProperty("Session1", "as400.local");
		My5250.getSessions().setProperty("Session3", "other.server.com");
		
		List<String> lastViewNames = Arrays.asList("Session1", "Session2", "Session3");
		
		List<String> sessionNames = My5250.filterExistingViewNames(lastViewNames );

		String[] expected = new String[] { "Session1", "Session3"};
		assertArrayEquals(expected, sessionNames.toArray());
	}

}

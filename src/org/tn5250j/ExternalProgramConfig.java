package org.tn5250j;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.tn5250j.connectdialog.ExternalProgram;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public class ExternalProgramConfig {

    protected static final String UNIX_SUFFIX = ".command.unix";
    protected static final String WINDOW_SUFFIX = ".command.window";
    protected static final String NAME_SUFFIX = ".command.name";
    protected static final String PREFIX = "etn.pgm.";

    private static TN5250jLogger log =
        TN5250jLogFactory.getLogger("org.tn5250j.ExternalProgramConfig");

	private static ExternalProgramConfig etnConfig;
	private static final String EXTERNAL_PROGRAM_REGISTRY_KEY = "etnPgmProps";
	private static final String EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME = "tn5250jExternalProgram.properties";
	private static final String EXTERNAL_PROGRAM_HEADER = "External Program Settings";

	protected final Properties etnPgmProps;
    private final List<ExternalProgram> programs = new LinkedList<>();

	public static ExternalProgramConfig getInstance(){
		if(etnConfig == null){
			etnConfig = new ExternalProgramConfig();
		}
		return etnConfig;
	}

	protected ExternalProgramConfig(){
		etnPgmProps = loadExternalProgramSettings();
		settingsToPrograms();
		sort();
	}

    protected Properties loadExternalProgramSettings() {
        Properties etnProps = null;
        try {
            etnProps = ConfigureFactory.getInstance().getProperties(EXTERNAL_PROGRAM_REGISTRY_KEY,
                    EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME, false, "Default Settings");

            log.info("begin loading external program settings");

            if (etnProps.isEmpty()) {
                final URL file = getClass().getClassLoader().getResource(EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME);
                etnProps.load(file.openStream());

                ConfigureFactory.getInstance().saveSettings(EXTERNAL_PROGRAM_REGISTRY_KEY,
                        EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME, EXTERNAL_PROGRAM_HEADER);
            }

        } catch (final IOException ioe) {
            log.error(ioe.getMessage());
        } catch (final SecurityException se) {
            log.error(se.getMessage());
        }

        return etnProps;
    }

    /**
     * @param program external program
     */
    public synchronized void programUpdated(final ExternalProgram program) {
        final ExternalProgram existing = valueOf(program.getName());
        if (existing != null) {
            programs.remove(existing);
        }
        programs.add(program);
        sort();
        programsToProperties();
    }

    public synchronized void remove(final String name) {
        final ExternalProgram existing = valueOf(name);
        if (existing != null) {
            programs.remove(existing);
        }
        sort();
        programsToProperties();
    }

    /**
     * @param name program command name.
     * @return external program if found or null;
     */
    public synchronized ExternalProgram valueOf(final String name) {
        if (name == null) {
            return null;
        }

        for (final ExternalProgram p : programs) {
            if (name.equals(p.getName())) {
                return p;
            }
        }

        return null;
    }

    private void programsToProperties() {
        etnPgmProps.clear();

        int order = 0;
        for (final ExternalProgram p : programs) {
            etnPgmProps.setProperty(PREFIX + order + NAME_SUFFIX, p.getName());
            etnPgmProps.setProperty(PREFIX + order + WINDOW_SUFFIX, p.getWCommand());
            etnPgmProps.setProperty(PREFIX + order + UNIX_SUFFIX, p.getUCommand());

            order++;
        }
    }

    private void settingsToPrograms() {
        final List<Integer> nums = getExternalProgramNumbers();
        nums.sort(Comparator.naturalOrder());

        for (final Integer num : nums) {
            final String program = etnPgmProps.getProperty(PREFIX + num + NAME_SUFFIX);
            final String wCommand = etnPgmProps.getProperty(PREFIX + num + WINDOW_SUFFIX);
            final String uCommand = etnPgmProps.getProperty(PREFIX + num + UNIX_SUFFIX);
            programs.add(new ExternalProgram(program, wCommand, uCommand));
        }
    }

    private List<Integer> getExternalProgramNumbers() {
        final int offset = PREFIX.length();
        final int reminder = NAME_SUFFIX.length();
        return etnPgmProps.keySet().stream()
                .map(Object::toString)
                .filter(this::isCommandName)
                .map(k -> k.substring(offset, k.length() - reminder))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    private boolean isCommandName(final String k) {
        return k.startsWith(PREFIX) && k.endsWith(NAME_SUFFIX);
    }

    private void sort() {
        programs.sort(Comparator.naturalOrder());
    }

    public synchronized List<ExternalProgram> getPrograms() {
        return new LinkedList<>(programs);
    }

    public synchronized void save() {
        programsToProperties();
        ConfigureFactory.getInstance().saveSettings(
                EXTERNAL_PROGRAM_REGISTRY_KEY,
                EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME,
                EXTERNAL_PROGRAM_HEADER);
    }
}

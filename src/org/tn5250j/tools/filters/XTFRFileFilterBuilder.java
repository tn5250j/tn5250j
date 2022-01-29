package org.tn5250j.tools.filters;

import org.tn5250j.gui.TN5250jFileFilterBuilder;

/**
 *
 * This is taken from Sun's demo ExampleFileFiler.java
 *
 * A convenience implementation of FileFilter that filters out
 * all files except for those type extensions that it knows about.
 *
 * Extensions are of the type ".foo", which is typically found on
 * Windows and Unix boxes, but not on Macinthosh. Case is ignored.
 *
 * Example - create a new filter that filerts out all files
 * but gif and jpg image files:
 *
 *     JFileChooser chooser = new JFileChooser();
 *     XTFRFileFilter filter = new XTFRFileFilter(
 *                   new String{"gif", "jpg"}, "JPEG & GIF Images")
 *     chooser.addChoosableFileFilter(filter);
 *     chooser.showOpenDialog(this);
 *
 * @version 1.10 05/17/01
 * @author Jeff Dinkins
 */
public class XTFRFileFilterBuilder extends TN5250jFileFilterBuilder {
    private String outputFilterClassName;
    private Object o;

    /**
     * Creates a file filter. If no filters are added, then all
     * files are accepted.
     *
     * @see #addExtension
     */
    public XTFRFileFilterBuilder() {
        super();
    }

    /**
     * Creates a file filter that accepts files with the given extension.
     * Example: new XTFRFileFilter("jpg");
     *
     * @see #addExtension
     */
    public XTFRFileFilterBuilder(final String extension) {
        super(extension, null);
    }

    /**
     * Creates a file filter that accepts the given file type.
     * Example: new XTFRFileFilter("jpg", "JPEG Image Images");
     *
     * Note that the "." before the extension is not needed. If
     * provided, it will be ignored.
     *
     * @see #addExtension
     */
    public XTFRFileFilterBuilder(final String extension, final String description) {
        super(extension, description);
    }

    /**
     * Creates a file filter from the given string array.
     * Example: new XTFRFileFilter(String {"gif", "jpg"});
     *
     * Note that the "." before the extension is not needed adn
     * will be ignored.
     *
     * @see #addExtension
     */
    public XTFRFileFilterBuilder(final String[] filters) {
        super(filters);
    }

    /**
     * Creates a file filter from the given string array and description.
     * Example: new XTFRFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
     *
     * Note that the "." before the extension is not needed and will be ignored.
     *
     * @see #addExtension
     */
    public XTFRFileFilterBuilder(final String[] filters, final String description) {
        super(filters, description);
    }

    public void setOutputFilterName(final String className) {

        outputFilterClassName = className;
    }

    public OutputFilterInterface getOutputFilterInstance() {
        try {
            if (o == null) {
                final Class<?> c = Class.forName(outputFilterClassName);
                o = c.newInstance();
            }
        } catch (final Exception e) {
            System.err.println(e);
        }

        return (OutputFilterInterface) o;
    }
}

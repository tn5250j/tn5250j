package org.tn5250j.tools.filters;

/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.1
 *
 * Description:
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
import java.io.File;
import java.util.Hashtable;
import java.util.Enumeration;
import javax.swing.filechooser.*;

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
public class XTFRFileFilter extends FileFilter {

   private static String TYPE_UNKNOWN = "Type Unknown";
   private static String HIDDEN_FILE = "Hidden File";

   private Hashtable filters = null;
   private String description = null;
   private String fullDescription = null;
   private boolean useExtensionsInDescription = true;
   private String outputFilterClassName;
   private Object o;

   /**
   * Creates a file filter. If no filters are added, then all
   * files are accepted.
   *
   * @see #addExtension
   */
   public XTFRFileFilter() {
      this.filters = new Hashtable();
   }

   /**
   * Creates a file filter that accepts files with the given extension.
   * Example: new XTFRFileFilter("jpg");
   *
   * @see #addExtension
   */
   public XTFRFileFilter(String extension) {
      this(extension,null);
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
   public XTFRFileFilter(String extension, String description) {
      this();
      if(extension!=null)
         addExtension(extension);
      if(description!=null)
         setDescription(description);
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
   public XTFRFileFilter(String[] filters) {
      this(filters, null);
   }

   /**
   * Creates a file filter from the given string array and description.
   * Example: new XTFRFileFilter(String {"gif", "jpg"}, "Gif and JPG Images");
   *
   * Note that the "." before the extension is not needed and will be ignored.
   *
   * @see #addExtension
   */
   public XTFRFileFilter(String[] filters, String description) {
      this();
      for (int i = 0; i < filters.length; i++) {
       // add filters one by one
       addExtension(filters[i]);
      }
      if(description!=null)
         setDescription(description);
   }

   /**
   * Return true if this file should be shown in the directory pane,
   * false if it shouldn't.
   *
   * Files that begin with "." are ignored.
   *
   * @see #getExtension
   * @see FileFilter#accepts
   */
   public boolean accept(File f) {
      if(f != null) {
         if(f.isDirectory()) {
            return true;
         }
         String extension = getExtension(f);
         if(extension != null && filters.get(getExtension(f)) != null) {
            return true;
         }
      }
      return false;
   }

   /**
   * Return the extension portion of the file's name .
   *
   * @see #getExtension
   * @see FileFilter#accept
   */
   public String getExtension(File f) {
      if(f != null) {
         return getExtension(f.getName());
      }
      return null;
   }

   public String getExtension(String filename) {

      if(filename != null) {
         int i = filename.lastIndexOf('.');
         if(i>0 && i<filename.length()-1) {
            return filename.substring(i+1).toLowerCase();
         }
      }
      return null;
   }

  /**
   * Adds a filetype "dot" extension to filter against.
   *
   * For example: the following code will create a filter that filters
   * out all files except those that end in ".jpg" and ".tif":
   *
   *   XTFRFileFilter filter = new XTFRFileFilter();
   *   filter.addExtension("jpg");
   *   filter.addExtension("tif");
   *
   * Note that the "." before the extension is not needed and will be ignored.
   */
   public void addExtension(String extension) {
      if(filters == null) {
         filters = new Hashtable(5);
      }
      filters.put(extension.toLowerCase(), this);
      fullDescription = null;
   }


   /**
   * Returns the human readable description of this filter. For
   * example: "JPEG and GIF Image Files (*.jpg, *.gif)"
   *
   * @see setDescription
   * @see setExtensionListInDescription
   * @see isExtensionListInDescription
   * @see FileFilter#getDescription
   */
   public String getDescription() {
      if(fullDescription == null) {
         if(description == null || isExtensionListInDescription()) {
            fullDescription = description==null ? "(" : description + " (";
            // build the description from the extension list
            Enumeration extensions = filters.keys();
            if(extensions != null) {
               fullDescription += "." + (String) extensions.nextElement();
               while (extensions.hasMoreElements()) {
                  fullDescription += ", ." + (String) extensions.nextElement();
               }
            }
            fullDescription += ")";
            } else {
               fullDescription = description;
            }
      }
      return fullDescription;
   }

   /**
   * Sets the human readable description of this filter. For
   * example: filter.setDescription("Gif and JPG Images");
   *
   * @see setDescription
   * @see setExtensionListInDescription
   * @see isExtensionListInDescription
   */
   public void setDescription(String description) {
      this.description = description;
      fullDescription = null;
   }

   /**
   * Determines whether the extension list (.jpg, .gif, etc) should
   * show up in the human readable description.
   *
   * Only relevent if a description was provided in the constructor
   * or using setDescription();
   *
   * @see getDescription
   * @see setDescription
   * @see isExtensionListInDescription
   */
   public void setExtensionListInDescription(boolean b) {
      useExtensionsInDescription = b;
      fullDescription = null;
   }

   /**
   * Returns whether the extension list (.jpg, .gif, etc) should
   * show up in the human readable description.
   *
   * Only relevent if a description was provided in the constructor
   * or using setDescription();
   *
   * @see getDescription
   * @see setDescription
   * @see setExtensionListInDescription
   */
   public boolean isExtensionListInDescription() {
      return useExtensionsInDescription;
   }

   /**
    * Set the extension to be used for this type if one is not provided
    *    This will append the first key of the filter contained in the list
    */
   public String setExtension(File f) {

      return setExtension(f.getAbsolutePath());
   }

   public String setExtension(String f) {

      if (f != null & getExtension(f) == null) {
         Enumeration e = filters.keys();
         String ext = (String)e.nextElement();
         // just a little extra check for html documents
         if (ext.equals("htm"))
            ext = "html";
         f += "." + ext.toLowerCase();
      }
      return f;
   }

   /**
    *
    */
   public boolean isExtensionInList(String filename) {

      String ext = null;
      if (filename == null)
         return false;
      int i = filename.lastIndexOf('.');
      if(i>0 && i<filename.length()-1) {
         ext = filename.substring(i+1).toLowerCase();
      }
      if (ext == null)
         return false;
      else
         // check if extension is within this filter list
         return filters.containsKey(ext);
   }

   public void setOutputFilterName(String className) {

      outputFilterClassName = className;
   }

   public OutputFilterInterface getOutputFilterInstance() {

      try {
         if (o == null) {
            Class c = Class.forName(outputFilterClassName);
            o = c.newInstance();
         }
      }
      catch (Exception e) {
         System.err.println(e);
      }

      return (OutputFilterInterface) o;
   }
}

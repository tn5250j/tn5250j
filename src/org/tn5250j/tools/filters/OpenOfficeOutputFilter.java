package org.tn5250j.tools.filters;

/*
 * @(#)OpenOfficeOutputFilter.java
 * Copyright:    Copyright (c) 2001
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

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.ArrayList;
import org.tn5250j.tools.*;

public class OpenOfficeOutputFilter implements OutputFilterInterface {

   private int row = 0;
   StringBuffer sb = new StringBuffer();

   ZipOutputStream fout = null;

   // create instance of file for output
   public void createFileInstance(String fileName) throws
                              FileNotFoundException {


      fout = new ZipOutputStream(new FileOutputStream(fileName));
      fout.setMethod(ZipOutputStream.DEFLATED);
      writeManifestEntry();
   }

   private void writeManifestEntry() {
      final String manifest =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<!DOCTYPE manifest:manifest" +
      " PUBLIC \"-//OpenOffice.org//DTD Manifest 1.0//EN\" \"Manifest.dtd\" >\n" +
      "<manifest:manifest xmlns:manifest=\"http://openoffice.org/2001/manifest\">\n" +
      " <manifest:file-entry" +
      " manifest:media-type=\"application/vnd.sun.xml.calc\" manifest:full-path=\"/\"/>\n" +
      " <manifest:file-entry manifest:media-type=\"\" manifest:full-path=\"Pictures/\"/>\n" +
      " <manifest:file-entry manifest:media-type=\"text/xml\" manifest:full-path=\"content.xml\"/>\n"+
      "</manifest:manifest>\n";

      ZipEntry zipentry = new ZipEntry("meta-inf\\manifest.xml");
      zipentry.setTime(System.currentTimeMillis());
      try {
         fout.putNextEntry(zipentry);
         fout.write(manifest.getBytes());
         fout.closeEntry();
      }
      catch (Exception e) {
         System.out.println(e.getMessage());
      }
   }

   /**
    * Write the html header of the output file
    */
   public void parseFields(byte[] cByte, ArrayList ffd, StringBuffer rb) {

      FileFieldDef f;

      // write out the xml record information for each field that is selected

      row++;
      int c = 0;
      rb.append ("   <table:table-row table:style-name=\"ro1\">\n");

      for (int x = 0; x < ffd.size(); x++,c++) {
         f = (FileFieldDef)ffd.get(x);
         if (f.isWriteField()) {

            switch (f.getFieldType()) {

               case 'P':
               case 'S':
                  rb.append("    <table:table-cell table:style-name=\"ce" + c + "\" table:value-type=\"float\"" +
                              " table:value=\"" + tr2xml(f.parseData(cByte)) +
                              "\">\n");
                  break;
               default:
                  rb.append("    <table:table-cell>\n");
                  break;

            }
            rb.append("     <text:p>" );
            rb.append(tr2xml(f.parseData(cByte)));
            rb.append ("</text:p>\n");
            rb.append ("    </table:table-cell>\n");

         }
      }
      rb.append("   </table:table-row>\n");

      try {
         fout.write(rb.toString().getBytes());
         fout.flush();
      }
      catch (IOException ioe) {

         System.out.println("parse fields " + ioe.getMessage());
      }
   }

   private String tr2xml(String s) {

      sb.setLength(0);

      for (int x =0;x < s.length(); x++) {

         switch (s.charAt(x)) {

            case '<':
               sb.append("&lt;");
               break;
            case '>':
               sb.append("&gt;");
               break;
            case '&':
               sb.append("&amp;");
               break;
            default:
               sb.append(s.charAt(x));
         }
      }
      return sb.toString();
   }
   /**
    * Write the html header of the output file
    */
   public void writeHeader(String fileName, String host,
                                 ArrayList ffd, char decChar) {

      final String header1 =
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
      "<!DOCTYPE office:document-content" +
      " PUBLIC \"-//OpenOffice.org//DTD OfficeDocument 1.0//EN\" \"office.dtd\" >\n" +
      "<office:document-content xmlns:office=\"http://openoffice.org/2000/office\"" +
      " xmlns:style=\"http://openoffice.org/2000/style\" xmlns:text=\"http://openoffice.org/2000/text\"" +
      " xmlns:table=\"http://openoffice.org/2000/table\" xmlns:draw=\"http://openoffice.org/2000/drawing\"" +
      " xmlns:fo=\"http://www.w3.org/1999/XSL/Format\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"" +
      " xmlns:number=\"http://openoffice.org/2000/datastyle\" xmlns:svg=\"http://www.w3.org/2000/svg\"" +
      " xmlns:chart=\"http://openoffice.org/2000/chart\" xmlns:dr3d=\"http://openoffice.org/2000/dr3d\"" +
      " xmlns:math=\"http://www.w3.org/1998/Math/MathML\" xmlns:form=\"http://openoffice.org/2000/form\"" +
      " xmlns:script=\"http://openoffice.org/2000/script\" office:class=\"spreadsheet\" office:version=\"1.0\">\n"+
      " <office:script/>\n" +
      " <office:font-decls>\n" +
      "  <style:font-decl style:name=\"Arial Unicode MS\" fo:font-family=\"&apos;Arial Unicode MS&apos;\" " +
      "style:font-pitch=\"variable\"/>\n" +
      "  <style:font-decl style:name=\"HG Mincho Light J\" fo:font-family=\"&apos;HG Mincho Light J&apos;\" " +
      "style:font-pitch=\"variable\"/>\n" +
      "  <style:font-decl style:name=\"Albany\" fo:font-family=\"Albany\" style:font-family-generic=\"swiss\" " +
      "style:font-pitch=\"variable\"/> \n" +
      " </office:font-decls>\n" +
      " <office:automatic-styles>\n" +
      "  <style:style style:name=\"co1\" style:family=\"table-column\">\n" +
      "   <style:properties fo:break-before=\"auto\" style:column-width=\"0.8925inch\"/>\n" +
      "  </style:style>\n" +
      "  <style:style style:name=\"ro1\" style:family=\"table-row\">\n"+
      "   <style:properties fo:break-before=\"auto\"/>\n" +
      "  </style:style>\n" +
      "  <style:style style:name=\"ta1\" style:family=\"table\" style:master-page-name=\"Default\">\n" +
      "   <style:properties table:display=\"true\"/>\n" +
      "  </style:style>\n";

      final String header2 =
      " </office:automatic-styles>\n" +
      " <office:body>\n" +
      "  <table:table table:name=\"Sheet1\" table:style-name=\"ta1\">\n" +
      "   <table:table-column table:style-name=\"co1\" table:number-columns-repeated=\"251\" table:default-cell-style-name=\"Default\"/>\n";


      ZipEntry zipentry = new ZipEntry("content.xml");
      zipentry.setTime(System.currentTimeMillis());
      try {
         fout.putNextEntry(zipentry);

         // write out header 1
         fout.write(header1.getBytes());

         // write out the header names
         FileFieldDef f;

         // lets write out some formats for numeric values.
         int n100 = 100;
         int ce = 0;
         String s = "";
         for (int k = 0; k < ffd.size(); k++,ce++) {
            f = (FileFieldDef)ffd.get(k);
            if (f.isWriteField() ) {
               if (f.getFieldType() == 'P' ||
                   f.getFieldType() == 'S') {
                  s = "  <number:number-style style:name=\"N" + (n100 + ce) + "\" style:family=\"data-style\" number:title=\"User-defined\">\n" +
      "   <number:number number:decimal-places=\"" + f.getPrecision() + "\" number:min-integer-digits=\"1\"/>\n" +
      "  </number:number-style>\n" +
      "  <style:style style:name=\"ce" + ce + "\" style:family=\"table-cell\"" +
      " style:parent-style-name=\"Default\" style:data-style-name=\"N" + + (n100 + ce) + "\"/>\n";
                  fout.write(s.getBytes());
               }
            }
         }

         // write out the rest of the header after filling in the formatting
         fout.write(header2.getBytes());

         // write out the record information for each field that is selected

         row++;
         int column = 1;
         fout.write("   <table:table-row table:style-name=\"ro1\">\n".getBytes());

         for (int x = 0; x < ffd.size(); x++) {
            f = (FileFieldDef)ffd.get(x);
            if (f.isWriteField()) {
               fout.write("    <table:table-cell>\n".getBytes());
               fout.write("     <text:p>".getBytes() );
               fout.write(f.getFieldName().getBytes());
               fout.write ("</text:p>\n".getBytes());
               fout.write ("    </table:table-cell>\n".getBytes());
            }
         }
         fout.write("   </table:table-row>\n".getBytes());

      }
      catch (Exception e) {
         System.out.println(e.getMessage());
      }
   }


   /**
    * write the footer of the xml output
    */
   public void writeFooter(ArrayList ffd) {

      final String footer =
         "  </table:table>\n" +
         "  <table:table table:name=\"Sheet2\" table:style-name=\"ta1\">\n" +
         "   <table:table-column table:style-name=\"co1\" table:default-cell-style-name=\"Default\"/>\n" +
         "   <table:table-row table:style-name=\"ro1\">\n" +
         "    <table:table-cell/>\n" +
         "   </table:table-row>\n" +
         "  </table:table>\n" +
         "  <table:table table:name=\"Sheet3\" table:style-name=\"ta1\">\n" +
         "   <table:table-column table:style-name=\"co1\" table:default-cell-style-name=\"Default\"/>\n" +
         "   <table:table-row table:style-name=\"ro1\">\n" +
         "    <table:table-cell/>\n" +
         "   </table:table-row>\n" +
         "  </table:table>\n" +
         " </office:body>\n" +
         "</office:document-content>";

      try {
         fout.write(footer.getBytes());
         fout.flush();
         fout.close();
      }
      catch (Exception e) {
         System.out.println(e.getMessage());
      }

   }


}
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

   private int row;
   StringBuffer sb;

   ZipOutputStream fout = null;

   // create instance of file for output
   public void createFileInstance(String fileName) throws
                              FileNotFoundException {


      fout = new ZipOutputStream(new FileOutputStream(fileName));
      fout.setMethod(ZipOutputStream.DEFLATED);
      writeManifestEntry();

      // initialize work variables
      row = 0;
      sb = new StringBuffer();

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

            case '\u003c': // <
               sb.append("&lt;");
               break;
            case '\u003e': // >
               sb.append("&gt;");
               break;
            case '\u0026': // &
               sb.append("&amp;");
               break;
            case '\u00c0': // À  // &Agrave;  À  capital A, grave accent
               sb.append("&Agrave;");
               break;
            case '\u00e0': // à  // &agrave;  à  small a, grave accent
               sb.append("&agrave;");
               break;
            case '\u00c1': // Á
               sb.append("&Aacute;");
               break;
            case '\u00e1': // á
               sb.append("&aacute;");
               break;
            case '\u00c2': // Â
               sb.append("&Acirc;");
               break;
            case '\u00e2': // â
               sb.append("&acirc;");
               break;
            case '\u00c3': // Ã
               sb.append("&Atilde;");
               break;
            case '\u00e3': // ã
               sb.append("&atilde;");
               break;
            case '\u00c4': // Ä
               sb.append("&Auml;");
               break;
            case '\u00e4': // ä
               sb.append("&auml;");
               break;
            case '\u00c5': // Å
               sb.append("&Aring;");
               break;
            case '\u00e5': // å
               sb.append("&aring;");
               break;
            case '\u00c6': // Æ
               sb.append("&AElig;");
               break;
            case '\u00e6': // æ
               sb.append("&aelig;");
               break;
            case '\u00c7': // Ç
               sb.append("&Ccedil;");
               break;
            case '\u00e7': // ç
               sb.append("&ccedil;");
               break;
            case '\u00c8': // È
               sb.append("&Egrave;");
               break;
            case '\u00e8': // è
               sb.append("&egrave;");
               break;
            case '\u00c9': // É
               sb.append("&Eacute;");
               break;
            case '\u00e9': // é
               sb.append("&eacute;");
               break;
            case '\u00ca': // Ê
               sb.append("&Ecirc;");
               break;
            case '\u00ea': // ê
               sb.append("&ecirc;");
               break;
            case '\u00cb': // Ë
               sb.append("&Euml;");
               break;
            case '\u00eb': // ë
               sb.append("&euml;");
               break;
            case '\u00cc': // Ì
               sb.append("&Igrave;");
               break;
            case '\u00ec': // ì
               sb.append("&igrave;");
               break;
            case '\u00cd': // Í
               sb.append("&Iacute;");
               break;
            case '\u00ed': // í
               sb.append("&iacute;");
               break;
            case '\u00ce': // Î
               sb.append("&Icirc;");
               break;
            case '\u00ee': // î
               sb.append("&icirc;");
               break;
            case '\u00cf': // Ï
               sb.append("&Iuml;");
               break;
            case '\u00ef': // ï
               sb.append("&iuml;");
               break;
            case '\u00d0': // Ð
               sb.append("&ETH;");
               break;
            case '\u00f0': // ð
               sb.append("&eth;");
               break;
            case '\u00d1': // Ñ
               sb.append("&Ntilde;");
               break;
            case '\u00f1': // ñ
               sb.append("&ntilde;");
               break;
            case '\u00d2': // Ò
               sb.append("&Ograve;");
               break;
            case '\u00f2': // ò
               sb.append("&ograve;");
               break;
            case '\u00d3': // Ó
               sb.append("&Oacute;");
               break;
            case '\u00f3': // ó
               sb.append("&oacute;");
               break;
            case '\u00d4': // Ô
               sb.append("&Ocirc;");
               break;
            case '\u00f4': // ô
               sb.append("&ocirc;");
               break;
            case '\u00d5': // Õ
               sb.append("&Otilde;");
               break;
            case '\u00f5': // õ
               sb.append("&otilde;");
               break;
            case '\u00d6': // Ö
               sb.append("&Ouml;");
               break;
            case '\u00f6': // ö
               sb.append("&ouml;");
               break;
            case '\u00d8': // Ø
               sb.append("&Oslash;");
               break;
            case '\u00f8': // ø
               sb.append("&oslash;");
               break;
            case '\u00d9': // Ù
               sb.append("&Ugrave;");
               break;
            case '\u00f9': // ù
               sb.append("&ugrave;");
               break;
            case '\u00da': // Ú
               sb.append("&Uacute;");
               break;
            case '\u00fa': // ú
               sb.append("&uacute;");
               break;
            case '\u00db': // Û
               sb.append("&Ucirc;");
               break;
            case '\u00fb': // û
               sb.append("&ucirc;");
               break;
            case '\u00dc': // Ü
               sb.append("&Uuml;");
               break;
            case '\u00fc': // ü
               sb.append("&uuml;");
               break;
            case '\u00dd': // Ý
               sb.append("&Yacute;");
               break;
            case '\u00fd': // ý
               sb.append("&yacute;");
               break;
            case '\u00de': // Þ
               sb.append("&THORN;");
               break;
            case '\u00fe': // þ
               sb.append("&thorn;");
               break;
            case '\u00df': // ß
               sb.append("&szlig;");
               break;
            case '\u00ff': // ÿ
               sb.append("&yuml;");
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

   public boolean isCustomizable() {
      return false;
   }

   public void setCustomProperties() {

   }

}
package org.tn5250j.tools;

/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.4
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

import org.tn5250j.CharMappings;

public class CodePage {

   private int ascii[] = new int[256];
   private int ebcdic[] = new int[256];
   private String codePage = "";
   private char unicode[] = new char[256];
   private boolean convert;

   private static final char[] uni1250 = {
      '\u0000', '\u0001', '\u0002', '\u0003' , '\u0004', '\u0005', '\u0006', '\u0007',	/*   0 -   7  */
      '\u0008', '\u0009', 0x0A,     '\u000B' , '\u000C', 0x0D,     '\u000E', '\u000F', /*   8 -  15  */
      '\u0010', '\u0011', '\u0012', '\u0013' , '\u0014', '\u0015', '\u0016', '\u0017',	/*  16 -  23  */
      '\u0018', '\u0019', '\u001A', '\u001B' , '\u001C', '\u001D', '\u001E', '\u001F',	/*  24 -  31  */
      '\u0020', '\u0021', '\u0022', '\u0023' , '\u0024', '\u0025', '\u0026', 0x27,	   /*  32 -  39  */
      '\u0028', '\u0029', '\u002A', '\u002B' , '\u002C', '\u002D', '\u002E', '\u002F',	/*  40 -  47  */
      '\u0030', '\u0031', '\u0032', '\u0033' , '\u0034', '\u0035', '\u0036', '\u0037',	/*  48 -  55  */
      '\u0038', '\u0039', '\u003A', '\u003B' , '\u003C', '\u003D', '\u003E', '\u003F',	/*  56 -  63  */
      '\u0040', '\u0041', '\u0042', '\u0043' , '\u0044', '\u0045', '\u0046', '\u0047',	/*  64 -  71  */
      '\u0048', '\u0049', '\u004A', '\u004B' , '\u004C', '\u004D', '\u004E', '\u004F',	/*  72 -  81  */
      '\u0050', '\u0051', '\u0052', '\u0053' , '\u0054', '\u0055', '\u0056', '\u0057',	/*  80 -  89  */
      '\u0058', '\u0059', '\u005A', '\u005B' , 0x5C,     '\u005D', '\u005E', '\u005F',	/*  88 -  95  */
      '\u0060', '\u0061', '\u0062', '\u0063' , '\u0064', '\u0065', '\u0066', '\u0067',	/*  96 - 103  */
      '\u0068', '\u0069', '\u006A', '\u006B' , '\u006C', '\u006D', '\u006E', '\u006F',	/* 104 - 111  */
      '\u0070', '\u0071', '\u0072', '\u0073' , '\u0074', '\u0075', '\u0076', '\u0077',	/* 112 - 119  */
      '\u0088', '\u0079', '\u007A', '\u007B' , '\u007C', '\u007D', '\u007E', '\u007F',	/* 120 - 127  */
      '\u20AC', '\u0000', '\u201A', '\u0000' , '\u201E', '\u2026', '\u2020', '\u2021',	/* 128 - 135  */
      '\u0000', '\u2030', '\u0160', '\u2039' , '\u015A', '\u0164', '\u017D', '\u0179',	/* 136 - 143  */
      '\u0000', '\u2018', '\u2019', '\u201C' , '\u201D', '\u2022', '\u2013', '\u2014',	/* 144 - 151  */
      '\u0000', '\u2122', '\u0161', '\u203A' , '\u015B', '\u0165', '\u017E', '\u017A',	/* 152 - 159  */
      '\u00A0', '\u20C7', '\u02D8', '\u0141' , '\u00A4', '\u0104', '\u00A6', '\u00A7',	/* 160 - 167  */
      '\u00A8', '\u00A9', '\u015E', '\u00AB' , '\u00AC', '\u00AD', '\u00AE', '\u017B',	/* 168 - 175  */
      '\u00B0', '\u00B1', '\u02DB', '\u0142' , '\u00B4', '\u00B5', '\u00B6', '\u00B7',	/* 176 - 183  */
      '\u00B8', '\u0105', '\u015F', '\u00BB' , '\u013D', '\u02DD', '\u013E', '\u017C',	/* 184 - 191  */
      '\u0154', '\u00C1', '\u00C2', '\u0102', '\u00C4' , '\u0139', '\u0106', '\u00C7', /* 192 - 199  */
      '\u010C', '\u00C9', '\u0118', '\u00CB', '\u011A' , '\u00CD', '\u00CE', '\u010E', /* 200 - 207  */
      '\u0110', '\u0143', '\u0147', '\u00D3', '\u00D4' , '\u0150', '\u00D6', '\u00D7', /* 208 - 215  */
      '\u0158', '\u016E', '\u00DA', '\u0170', '\u00DC' , '\u00DD', '\u0162', '\u00DF', /* 216 - 223  */
      '\u0155', '\u00E1', '\u00E2', '\u0103', '\u00E4' , '\u013A', '\u0107', '\u00E7', /* 224 - 231  */
      '\u010D', '\u00E9', '\u0119', '\u00EB', '\u011B' , '\u00ED', '\u00EE', '\u010F', /* 232 - 239  */
      '\u0111', '\u0144', '\u0148', '\u00F3', '\u00F4' , '\u0151', '\u00F6', '\u00F7', /* 240 - 247  */
      '\u0159', '\u016F', '\u00FA', '\u0171', '\u00FC' , '\u00FD', '\u0163', '\u02D9', /* 248 - 255  */
   };

   // Added 1025 code pages by Fomin, Leonid <leonid.fomin@mosnar.com>
   private static final char[] uni1025 = {
     '\u0000','\u0001','\u0002','\u0003','\u0004','\u0005','\u0006','\u0007',
     '\u0008','\u0009',0x0A,'\u000B','\u000C',0x0D,'\u000E','\u000F',
     '\u0010','\u0011','\u0012','\u0013','\u0014','\u0015','\u0016','\u0017',
     '\u0018','\u0019','\u001A','\u001b','\u001c','\u001d','\u001e','\u001f',
     '\u0020','\u0021','\u0022','\u0023','\u0024','\u0025','\u0026',0x27,
     '\u0028','\u0029','\u002a','\u002b','\u002c','\u002d','\u002e','\u002f',
     '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037',
     '\u0038','\u0039','\u003a','\u003b','\u003c','\u003d','\u003e','\u003f',
     '\u0040','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047',
     '\u0048','\u0049','\u004a','\u004b','\u004c','\u004d','\u004e','\u004f',
     '\u0050','\u0051','\u0052','\u0053','\u0054','\u0055','\u0056','\u0057',
     '\u0058','\u0059','\u005a','\u005b',0x5c,'\u005d','\u005e','\u005f',
     '\u0060','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067',
     '\u0068','\u0069','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F',
     '\u0070','\u0071','\u0072','\u0073','\u0074','\u0075','\u0076','\u0077',
     '\u0078','\u0079','\u007A','\u007B','\u007C','\u007D','\u007E','\u007F',
     '\u0402','\u0403','\u201A','\u0453','\u201E','\u2026','\u2020','\u2021',
     '\u20AC','\u2030','\u0409','\u2039','\u040A','\u040C','\u040B','\u040F',
     '\u0452','\u2018','\u2019','\u201C','\u201D','\u2022','\u2013','\u2014',
     '\u0098','\u2122','\u0459','\u203A','\u045A','\u045C','\u045B','\u045F',
     '\u00A0','\u040E','\u045E','\u0408','\u00A4','\u0490','\u00A6','\u00A7',
     '\u00A8','\u00A9','\u0404','\u00AB','\u00AC','\u00AD','\u00AE','\u0407',
     '\u00B0','\u00B1','\u0406','\u0456','\u0491','\u00B5','\u00B6','\u00B7',
     '\u0451','\u2116','\u0454','\u00BB','\u0458','\u0405','\u0455','\u0457',
     '\u0410','\u0411','\u0412','\u0413','\u0414','\u0415','\u0416','\u0417',
     '\u0418','\u0419','\u041A','\u041B','\u041C','\u041D','\u041E','\u041E',
     '\u0420','\u0421','\u0422','\u0423','\u0424','\u0425','\u0426','\u0427',
     '\u0428','\u0429','\u042a','\u042b','\u042c','\u042d','\u042e','\u042f',
     '\u0430','\u0431','\u0432','\u0433','\u0434','\u0435','\u0436','\u0437',
     '\u0438','\u0439','\u043a','\u043b','\u043c','\u043d','\u043e','\u043f',
     '\u0440','\u0441','\u0442','\u0443','\u0444','\u0445','\u0446','\u0447',
     '\u0448','\u0449','\u044a','\u044b','\u044c','\u044d','\u044E','\u044F'
     };

    private static final char[] uni1026 = {
        '\u0000','\u0001','\u0002','\u0003','\u0004','\u0005','\u0006','\u0007',
        '\b'    ,'\t'    ,'\n'    ,'\u000B','\f'    ,'\r'    ,'\u000E','\u000F',
        '\u0010','\u0011','\u0012','\u0013','\u0014','\u0015','\u0016','\u0017',
        '\u0018','\u0019','\u001A','\u001B','\u001C','\u001D','\u001E','\u001F',
        '\u0020','\u0021','\"'    ,'\u0023','\u0024','\u0025','\u0026','\''    ,
        '\u0028','\u0029','\u002A','\u002B','\u002C','\u002D','\u002E','\u002F',
        '\u0030','\u0031','\u0032','\u0033','\u0034','\u0035','\u0036','\u0037',
        '\u0038','\u0039','\u003A','\u003B','\u003C','\u003D','\u003E','\u003F',
        '\u0040','\u0041','\u0042','\u0043','\u0044','\u0045','\u0046','\u0047',
        '\u0048','\u0049','\u004A','\u004B','\u004C','\u004D','\u004E','\u004F',
        '\u0050','\u0051','\u0052','\u0053','\u0054','\u0055','\u0056','\u0057',
        '\u0058','\u0059','\u005A','\u005B','\\'    ,'\u005D','\u005E','\u005F',
        '\u0060','\u0061','\u0062','\u0063','\u0064','\u0065','\u0066','\u0067',
        '\u0068','\u0069','\u006A','\u006B','\u006C','\u006D','\u006E','\u006F',
        '\u0070','\u0071','\u0072','\u0073','\u0074','\u0075','\u0076','\u0077',
        '\u0078','\u0079','\u007A','\u007B','\u007C','\u007D','\u007E','\u007F',
        '\u20AC','\u0081','\u201A','\u0192','\u201E','\u2026','\u2020','\u2021',
        '\u02C6','\u2030','\u0160','\u2039','\u0152','\u008D','\u008E','\u008F',
        '\u0090','\u2018','\u2019','\u201C','\u201D','\u2022','\u2013','\u2014',
        '\u02DC','\u2122','\u0161','\u203A','\u0153','\u009D','\u009E','\u0178',
        '\u00A0','\u00A1','\u00A2','\u00A3','\u00A4','\u00A5','\u00A6','\u00A7',
        '\u00A8','\u00A9','\u00AA','\u00AB','\u00AC','\u00AD','\u00AE','\u00AF',
        '\u00B0','\u00B1','\u00B2','\u00B3','\u00B4','\u00B5','\u00B6','\u00B7',
        '\u00B8','\u00B9','\u00BA','\u00BB','\u00BC','\u00BD','\u00BE','\u00BF',
        '\u00C0','\u00C1','\u00C2','\u00C3','\u00C4','\u00C5','\u00C6','\u00C7',
        '\u00C8','\u00C9','\u00CA','\u00CB','\u00CC','\u00CD','\u00CE','\u00CF',
        '\u011E','\u00D1','\u00D2','\u00D3','\u00D4','\u00D5','\u00D6','\u00D7',
        '\u00D8','\u00D9','\u00DA','\u00DB','\u00DC','\u0130','\u015E','\u00DF',
        '\u00E0','\u00E1','\u00E2','\u00E3','\u00E4','\u00E5','\u00E6','\u00E7',
        '\u00E8','\u00E9','\u00EA','\u00EB','\u00EC','\u00ED','\u00EE','\u00EF',
        '\u011F','\u00F1','\u00F2','\u00F3','\u00F4','\u00F5','\u00F6','\u00F7',
        '\u00F8','\u00F9','\u00FA','\u00FB','\u00FC','\u0131','\u015F','\u00FF'
    };

   public CodePage(String codePage) {

      setCodePage(codePage);
   }
   /**
    * This routine will convert an ebcdic value to a displayable unicode
    *    value.
    *
    * If the codepage needs to be converted:
    *    1. first check if a conversion is to take place from ascii to unicode
    *    2. if conversion then first get the ascii value from the ebcdic to
    *          ascii lookup array then obtain the unicode value to from the ascii
    *          to unicode array
    *
    *    For example Code page 870 ebcdic will translate to ascii latin 2
    *       (ISO 8859-2) and we need to convert latin 2 (ISO 8859-2) to
    *       latin 1 (ISO 8859-1) unicode 1250.
    *
    *    So in reality we have an intermediate value to be converted to something
    *       we understand for display.
    *
    */

   public char ebcdic2uni (int index) {

      if (convert) {

         int ea = ebcdic[index & 0xff];
         if (ea > 0x7F) {
//            System.out.println("conversion found for index: " + index + " "  + ea + " "  + (char)ebcdic[index & 0xff] + " " + unicode[ea]);
            return unicode[ea];
         }
         else
            return (char)ea;

      }
      else
         return (char)ebcdic[index & 0xff];
   }
   public byte getASCII(int index) {

      return (byte)ascii[index];

   }
   public char getASCIIChar(int index) {
      return (char)ebcdic[index & 0xff];
   }
   public String getCodePage () {

      return codePage;
   }
   public byte getEBCDIC(int index) {
      return (byte)ascii[index & 0xff];

   }
   public char getEBCDICChar(int index) {
      return (char)ascii[index & 0xff];

   }
   public void setCodePage (String newCodePage) {

      if (!codePage.toLowerCase().equals(newCodePage.toLowerCase())) {
         codePage = newCodePage;

         int i = 0;
         int[] cp = CharMappings.getCodePage(codePage);
         do {

            ebcdic[i] = cp[i];
            ascii[cp[i]] = i;
         } while(++i < 256);

         if (codePage.toLowerCase().startsWith("870-pl")) {
            unicode = uni1250;
            convert = true;
//            System.out.println("using conversion");
         } else if(codePage.toLowerCase().startsWith("1025")) {
            unicode = uni1025;
            convert = true;
//            System.out.println("using conversion");
         } else if(codePage.toLowerCase().startsWith("1026")) {
             unicode = uni1026;
             convert = true;
             //            System.out.println("using conversion");
         } else {
            convert = false;
//            System.out.println("not using conversion");
         }
      }
   }
   /**
    * This routine will convert an unicode value to ebcdic value
    *
    * If the unicode values need to be converted:
    *    1. first check if a conversion is to take place from unicode to ascii
    *    2. if conversion then convert unicode values to ascii
    *    3. convert from ascii to ebcdic
    *
    *  See ebcdic2uni for a description but goes the opposite direction
    */

   public byte uni2ebcdic (char index) {

      if (convert) {
         int ind = index;
         int len = unicode.length;
         if (index > '\u007F')
            for (int x = 0x80; x < len; x++) {
               if (unicode[x] == index) {
//                  System.out.println("conversion found " + (char)index + " " + unicode[x]);
                  ind = x;
                  break;
               }
            }

//         System.out.println("using conversion");
         return (byte)ascii[ind & 0xff];
      }
      else
         return (byte)ascii[index & 0xff];
   }
}

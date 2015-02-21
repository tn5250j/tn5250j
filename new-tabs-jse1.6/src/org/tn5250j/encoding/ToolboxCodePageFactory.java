/**
 * Title: ToolboxCodePage
 * Copyright:   Copyright (c) 2001, 2002, 2003
 * Company:
 * @author  Kenneth J. Pouncey
 *          rewritten by LDC, WVL, Luc
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
package org.tn5250j.encoding;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

/* package */ class ToolboxCodePageFactory {

	private final static String[] CODEPAGES = { "Big5", "Cp037", "Cp273", "Cp277", "Cp278",
			"Cp280", "Cp284", "Cp285", "Cp297", "Cp420", "Cp424", "Cp437",
			"Cp500", "Cp737", "Cp775", "Cp838", "Cp850", "Cp852", "Cp855",
			"Cp856", "Cp857", "Cp858", "Cp860", "Cp861", "Cp862", "Cp863",
			"Cp864", "Cp865", "Cp866", "Cp868", "Cp869", "Cp870",
			"Cp871", "Cp874", "Cp875", "Cp918", "Cp921", "Cp922",
			"Cp923", // IBM Latin-9.
			"Cp930", "Cp933", "Cp935", "Cp937", "Cp939", "Cp942", "Cp943",
			"Cp948", "Cp949", "Cp950", "Cp964", "Cp970", "Cp1006", "Cp1025",
			"Cp1026", "Cp1046", "Cp1097", "Cp1098", "Cp1112", "Cp1122",
			"Cp1123", "Cp1124", "Cp1140", "Cp1141", "Cp1142", "Cp1143",
			"Cp1144", "Cp1145", "Cp1146", "Cp1147", "Cp1148", "Cp1149",
			"Cp1252", "Cp1250", "Cp1251", "Cp1253", "Cp1254", "Cp1255",
			"Cp1256", "Cp1257", "Cp1258", "Cp1381", "Cp1383", "Cp33722" };

	private static final String CONVERTER_NAME = "com.ibm.as400.access.CharConverter";
	private static final String TOBYTES_NAME = "stringToByteArray";
	private static final String TOSTRING_NAME = "byteArrayToString";

	private static ToolboxCodePageFactory singleton;

	private final TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

	private ToolboxCodePageFactory() {
		/* private for singleton */
	}

	public static synchronized ToolboxCodePageFactory getInstance() {
		if (singleton == null) {
			singleton = new ToolboxCodePageFactory();
		}
		return singleton;
	}

	/**
	 * @return
	 */
	public String[] getAvailableCodePages() {
		try {
			final ClassLoader loader = getClassLoader();
			Class.forName(CONVERTER_NAME, false, loader);
			return CODEPAGES;
		} catch (Exception e) {
			log.info("Couldn't locate JT400 Toolbox in classpath. Charset converters can't be used.");
			return new String[0];
		}
	}

	/**
	 * @param encoding
	 * @return
	 */
	public ICodePage getCodePage(String encoding) {
		try {
			ClassLoader loader = getClassLoader();
			Class<?> conv_class = Class.forName(CONVERTER_NAME, true, loader);
			Constructor<?> conv_constructor = conv_class.getConstructor(new Class[] { String.class });
			Method toBytes_method = conv_class.getMethod(TOBYTES_NAME, new Class[] { String.class });
			Method toString_method = conv_class.getMethod(TOSTRING_NAME, new Class[] { byte[].class });
			Object convobj = conv_constructor.newInstance(new Object[] { encoding });
			return new ToolboxConverterProxy(convobj, toBytes_method, toString_method);
		} catch (Exception e) {
			log.warn("Can't load charset converter from JT400 Toolbox for code page " + encoding, e);
			return null;
		}
	}

	private static final ClassLoader getClassLoader() {
		ClassLoader loader = ToolboxCodePageFactory.class.getClassLoader();
		if (loader == null) {
			loader = ClassLoader.getSystemClassLoader();
		}
		return loader;
	}

	private static class ToolboxConverterProxy implements ICodePage {

		private final Object converter;
		private final Method tobytesMethod;
		private final Method tostringMethod;

		private ToolboxConverterProxy(Object converterObject, Method tobytesMethod, Method tostringMethod) {
			super();
			this.converter = converterObject;
			this.tobytesMethod = tobytesMethod;
			this.tostringMethod = tostringMethod;
		}

		@Override
		public char ebcdic2uni(int index) {
			Object result;
			try {
				result = tostringMethod.invoke(converter, new Object[] { new byte[] { (byte) (index & 0xFF) } });
			} catch (Throwable t) {
				result = null;
			}

			if (result == null)
				return 0x00;

			return ((String) result).charAt(0);
		}

		@Override
		public byte uni2ebcdic(char index) {
			Object result;
			try {
				result = tobytesMethod.invoke(converter, new Object[] { new String(new char[] { index }) });
			} catch (Throwable t) {
				result = null;
			}

			if (result == null)
				return 0x00;

			return ((byte[]) result)[0];
		}
	}

}
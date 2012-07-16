package org.tn5250j.encoding.builtin;

import org.tn5250j.encoding.ICodePage;

/**
 * Interface for classes which do the translation from
 * EBCDIC bytes to Unicode characters and vice versa.
 * 
 */
public interface ICodepageConverter extends ICodePage {

	/**
	 * Returns an name/ID for this converter.
	 * Example '273' or 'CP1252'. This name should be unique,
	 * cause it's used in user settungs and so on.
	 * 
	 * @return
	 */
	public abstract String getName();
	
	/**
	 * Returns a short description for this converter.
	 * For Example '273 - German, EBCDIC'
	 * 
	 * @return
	 */
	public abstract String getDescription();
		
	/**
	 * Does special initialization stuff for this converter.
	 */
	public abstract ICodepageConverter init();
	
}
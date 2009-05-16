package org.tn5250j.cp;

/**
 * Interface for classes which do the translation from
 * EBCDIC bytes to Unicode characters and vice versa.
 * 
 */
public interface ICodepageConverter {

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
	public abstract void init();
	
	/**
	 * Convert a single byte (or maybe more bytes which representing one character) to a Unicode character.
	 * 
	 * @param index 
	 * @return
	 */
	public abstract char ebcdic2uni(int index);

	/**
	 * Convert a Unicode character in it's byte representation.
	 * Therefore, only 8bit codepages are supported.
	 * 
	 * @param index
	 * @return
	 */
	public abstract byte uni2ebcdic(char index);

}
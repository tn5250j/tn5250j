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
package org.tn5250j.framework.tn5250;

public class ScreenField {

	protected ScreenField(Screen5250 s) {

		this.s = s;

	}

	protected ScreenField setField(int attr, int len, int ffw1, int ffw2,
			int fcw1, int fcw2) {

		return setField(attr, s.getCurrentRow() - 1, s.getCurrentCol() - 1,
				len, ffw1, ffw2, fcw1, fcw2);
	}

	protected ScreenField setField(int attr, int row, int col, int len,
			int ffw1, int ffw2, int fcw1, int fcw2) {

		// startRow = row;
		// startCol = col;
		startPos = (row * s.getColumns()) + col;
		endPos = startPos + length - 1;
		cursorProg = 0;
		fieldId = 0;
		length = len;
		endPos = startPos + length - 1;
		this.attr = attr;
		setFFWs(ffw1, ffw2);
		setFCWs(fcw1, fcw2);

		next = null;
		prev = null;

		return this;

	}

	public int getAttr() {
		return attr;
	}

	public int getHighlightedAttr() {
		return (fcw2 & 0x0f) | 0x20;
	}

	public int getLength() {
		return length;
	}

	protected boolean setFFWs(int ffw1, int ffw2) {

		this.ffw1 = ffw1;
		this.ffw2 = ffw2;

		int adj = getAdjustment();

		if (adj > 0) {
			checkCanSend = true;

			switch (adj) {

			case 5:
			case 6:
				rightAdjd = false;
				break;
			case 7:
				manditoried = false;
				break;
			}

		}
		mdt = (ffw1 & 0x8) == 0x8;
		// if (mdt)
		// s.masterMDT = true;
		return mdt;
	}

	public int getFFW1() {
		return ffw1;
	}

	public int getFFW2() {
		return ffw2;
	}

	protected void setFCWs(int fcw1, int fcw2) {

		this.fcw1 = fcw1;
		this.fcw2 = fcw2;

		// if ((fcw1 & 0x88) == 0x88) {
		if (fcw1 == 0x88) {

			cursorProg = fcw2;
		}
	}

	public int getFCW1() {
		return fcw1;
	}

	public int getFCW2() {
		return fcw2;
	}

	public int getFieldLength() {
		return length;
	}

	public int getCursorProgression() {
		return cursorProg;
	}

	public int getFieldId() {
		return fieldId;
	}

	protected void setFieldId(int fi) {
		fieldId = fi;
	}

	public int getCursorRow() {

		return cursorPos / s.getColumns();
	}

	public int getCursorCol() {

		return cursorPos % s.getColumns();
	}

	protected void changePos(int i) {

		cursorPos += i;

	}

	protected String getText() {

		StringBuffer text = new StringBuffer();
		getKeyPos(endPos);
		int x = length;
		text.setLength(x);
		while (x-- > 0) {

			// here we manipulate the unicode characters a little for attributes
			// that are imbedded in input fields. We will offset them by unicode
			// \uff00. All routines that process these fields will have to
			// return them to their proper offsets.
			// example:
			// if we read an attribute byte of 32 for normal display the unicode
			// character for this is and the unicode character for
			// a space is also thus the offset.
			if (s.planes.isAttributePlace(cursorPos)) {
				text.setCharAt(x,
						(char) ('\uff00' + s.planes.getCharAttr(cursorPos)));
			} else {
				text.setCharAt(x, s.planes.getChar(cursorPos));
			}
			changePos(-1);

		}

		// Since only the mdt of the first continued field is set we will get
		// the text of the next continued field if we are dealing with continued
		// fields. See routine setMDT for the whys of this. This is only
		// executed if this is the first field of a continued field.
		if (isContinued() && isContinuedFirst()) {
			ScreenField sf = this;
			do {
				sf = sf.next;
				text.append(sf.getText());
			} while (!sf.isContinuedLast());

			sf = null;
		}

		return text.toString();

	}

	public String getString() {

		StringBuffer text = new StringBuffer();
		getKeyPos(endPos);
		int x = length;
		text.setLength(x);
		while (x-- > 0) {

			// here we manipulate the unicode characters a little for attributes
			// that are imbedded in input fields. We will offset them by unicode
			// \uff00. All routines that process these fields will have to
			// return them to their proper offsets.
			// example:
			// if we read an attribute byte of 32 for normal display the unicode
			// character for this is and the unicode character for
			// a space is also thus the offset.
			if (s.planes.isAttributePlace(cursorPos)) {
				text.setCharAt(x,
						(char) ('\uff00' + s.planes.getCharAttr(cursorPos)));
			} else {
				if (s.planes.getChar(cursorPos) < ' ') {
					text.setCharAt(x, ' ');
				} else {
					text.setCharAt(x, s.planes.getChar(cursorPos));
				}
			}
			changePos(-1);

		}

		// Since only the mdt of the first continued field is set we will get
		// the text of the next continued field if we are dealing with continued
		// fields. See routine setMDT for the whys of this. This is only
		// executed if this is the first field of a continued field.
		if (isContinued() && isContinuedFirst()) {
			ScreenField sf = this;
			do {
				sf = sf.next;
				text.append(sf.getString());
			} while (!sf.isContinuedLast());

			sf = null;
		}

		return text.toString();

	}

	public void setFieldChar(char c) {

		int x = length;
		cursorPos = startPos;
		while (x-- > 0) {
			s.planes.setChar(cursorPos, c);
			changePos(1);
		}

	}

	public void setFieldChar(int lastPos, char c) {

		int x = endPos - lastPos + 1;
		cursorPos = lastPos;
		while (x-- > 0) {
			s.planes.setChar(cursorPos, c);
			s.setDirty(cursorPos);
			changePos(1);
		}
	}

	protected void setRightAdjusted() {
		rightAdjd = true;
	}

	protected void setManditoryEntered() {

		manditoried = true;
	}

	protected void resetMDT() {
		mdt = false;

	}

	protected void setMDT() {

		// get the first field of a continued edit field if it is continued
		if (isContinued() && !isContinuedFirst()) {
			ScreenField sf = prev;
			while (sf.isContinued() && !sf.isContinuedFirst()) {

				sf = sf.prev;

			}
			sf.setMDT();
			sf = null;
		} else {
			mdt = true;
		}

	}

	public boolean isBypassField() {

		return (ffw1 & 0x20) == 0x20;

	}

	public int getAdjustment() {

		return (ffw2 & 0x7);
	}

	// is field exit required
	public boolean isFER() {

		return (ffw2 & 0x40) == 0x40;
	}

	// is field manditory enter
	public boolean isMandatoryEnter() {

		return (ffw2 & 0x8) == 0x8;

	}

	public boolean isToUpper() {

		return (ffw2 & 0x20) == 0x20;

	}

	// bits 5 - 7
	public int getFieldShift() {

		return (ffw1 & 0x7);

	}

	public boolean isHiglightedEntry() {

		return (fcw1 == 0x89);

	}

	public boolean isAutoEnter() {

		return (ffw2 & 0x80) == 0x80;

	}

	public boolean isSignedNumeric() {

		return (getFieldShift() == 7);

	}

	public boolean isRightToLeft() {
		return (getFieldShift() == 0x04);
	}

	public boolean isNumeric() {

		return (getFieldShift() == 3);

	}

	public boolean isDupEnabled() {

		return (ffw1 & 0x10) == 0x10;

	}

	public boolean isContinued() {

		return (fcw1 & 0x86) == 0x86 && (fcw2 >= 1 && fcw2 <= 3);

	}

	public boolean isContinuedFirst() {

		return (fcw1 & 0x86) == 0x86 && (fcw2 == 1);

	}

	public boolean isContinuedMiddle() {

		return (fcw1 & 0x86) == 0x86 && (fcw2 == 3);

	}

	public boolean isContinuedLast() {

		return (fcw1 & 0x86) == 0x86 && (fcw2 == 2);

	}

	protected boolean isCanSend() {

		int adj = getAdjustment();

		// here we need to check the Field Exit Required value first before
		// checking
		// the adjustments. If the last character has been entered and we are
		// now setting past the last position then we are allowed to process the
		// the field without continuing.
		if (isFER() && cursorPos > endPos) {
			return true;
		}

		// signed numeric fields need to be checked as well.
		if (isSignedNumeric() && cursorPos < endPos - 1) {
			return false;
		}

		if (adj > 0) {

			switch (adj) {

			case 5:
			case 6:
				return rightAdjd;
			case 7:
				return manditoried;
			default:
				return true;
			}

		}
		return true;
	}

	public boolean isSelectionField() {

		return isSelectionField;

	}

	public void setSelectionFieldInfo(int type, int index, int position) {

		selectionFieldType = type;
		selectionIndex = index;
		selectionPos = position;
		isSelectionField = true;

	}

	protected int getKeyPos(int row1, int col1) {

		int x = ((row1 * s.getColumns()) + col1);
		int y = x - startPos();
		cursorPos = x;

		return y;
	}

	protected int getKeyPos(int pos) {

		int y = pos - startPos();
		cursorPos = pos;

		return y;
	}

	public int getCurrentPos() {

		return cursorPos;
	}

	public boolean withinField(int pos) {

		if (pos >= startPos && pos <= endPos) {
			return true;
		}
		return false;

	}

	public int startPos() {

		return startPos;
	}

	/**
	 * Get the starting row of the field. Offset is 0 so row 6 returned is row 7
	 * mapped to screen
	 * 
	 * @return int starting row of the field offset 0
	 */
	public int startRow() {

		return startPos / s.getColumns();

	}

	/**
	 * Get the starting column of the field. Offset is 0 so column 6 returned is
	 * column 7 mapped to screen
	 * 
	 * @return int starting column of the field offset 0
	 */
	public int startCol() {

		return startPos % s.getColumns();

	}

	public int endPos() {

		return endPos;

	}

	/**
	 * Sets the field's text plane to the specified string. If the string is
	 * shorter than the length of the field, the rest of the field is cleared.
	 * If the string is longer than the field, the text is truncated. A
	 * subsequent call to getText on this field will not show the changed text.
	 * To see the changed text, do a refresh on the iOhioFields collection and
	 * retrieve the refreshed field object.
	 * 
	 * @param text
	 *            - The text to be placed in the field's text plane.
	 */
	public void setString(String text) {

		cursorPos = startPos;

		if (isRightToLeft()) {
			text = new StringBuilder(text).reverse().toString();
		}

		final ScreenPlanes planes = s.getPlanes();
		for (int x = 0,len = text.length(); x < length; x++) {
			char tc = (x < len) ? text.charAt(x) : ' ';
			planes.setChar(cursorPos, tc);
			changePos(1);
		}
		setMDT();
		s.getScreenFields().setMasterMDT();
	}

	@Override
	public String toString() {
		int fcw = (fcw1 & 0xff) << 8 | fcw2 & 0xff;
		return "startRow = " + startRow() + " startCol = " + startCol()
				+ " length = " + length + " ffw1 = (0x"
				+ Integer.toHexString(ffw1) + ") ffw2 = (0x"
				+ Integer.toHexString(ffw2) + ") fcw1 = (0x"
				+ Integer.toHexString(fcw1) + ") fcw2 = (0x"
				+ Integer.toHexString(fcw2) + ") fcw = ("
				+ Integer.toBinaryString(fcw) + ") fcw hex = (0x"
				+ Integer.toHexString(fcw) + ") is bypass field = "
				+ isBypassField() + ") is autoenter = " + isAutoEnter()
				+ ") is manditoryenter = " + isMandatoryEnter()
				+ ") is field exit required = " + isFER() + ") is Numeric = "
				+ isNumeric() + ") is Signed Numeric = " + isSignedNumeric()
				+ ") is cursor progression = " + (fcw1 == 0x88)
				+ ") next progression field = " + fcw2 + ") field id "
				+ fieldId + " continued edit field = " + isContinued()
				+ " first continued edit field = " + isContinuedFirst()
				+ " middle continued edit field = " + isContinuedMiddle()
				+ " last continued edit field = " + isContinuedLast()
				+ " mdt = " + mdt;
	}

	int startPos = 0;
	int endPos = 0;
	boolean mdt = false;
	protected boolean checkCanSend;
	protected boolean rightAdjd;
	protected boolean manditoried;
	boolean canSend = true;
	int attr = 0;
	int length = 0;
	int ffw1 = 0;
	int ffw2 = 0;
	int fcw1 = 0;
	int fcw2 = 0;
	int cursorPos = 0;
	Screen5250 s;
	int cursorProg = 0;
	int fieldId = 0;
	ScreenField next = null;
	ScreenField prev = null;
	boolean isSelectionField;
	int selectionFieldType;
	int selectionIndex;
	int selectionPos;
}
# Class to aid scripts in identifying texta areas of the session screen
# Author : jorjun@mac.com
# Date : 1st April 2003

import java

class Trigger:
	def __init__(self):
		self.screen = _session.getScreen()
		self.strBufScreen = java.lang.StringBuffer()
	
	def getScreenRow(self, x, y, width):
		self.strBufScreen.setLength(0)
		screenChars = self.screen.getScreenAsChars()
		
		y -= 1 ; x -= 1
		sPos = y * self.screen.getCols() + x
		for i in range(width) :
			self.strBufScreen.append(screenChars[sPos + i])
		
		return self.strBufScreen.toString()
		
	def isMatched(self, x, y, strTest):
		matched =0
		if self.getScreenRow(x, y, len(strTest)) == strTest:
			matched = 1
		return matched

if __name__ =='main' or __name__ == '__main__':	
	from javax.swing import *
	logon = Trigger()

	options =  ("OK", "CANCEL");

	if logon.isMatched(41, 24, "(C) COPYRIGHT IBM CORP. 1980, 2000."):
		JOptionPane.showOptionDialog(None, "Standard Signon Detected", \
		 "Screen Match", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,\
		  None, options, options[0])
		  
	elif logon.isMatched(2, 24, "(C) COPYRIGHT IBM CORP. 1980, 2000."):
		JOptionPane.showOptionDialog(None, "Session started", \
		"Screen Match", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, \
		None, options, options[0])
	
	else:
		JOptionPane.showOptionDialog(None, "Screen not recognised", \
		"Screen Match", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, \
		None, options, options[0])
	

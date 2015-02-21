# Fragments.py acts between "paste" (one temporary fragment) and "macros" (many saved fragments).
# When you need to paste several text parts or fragments (e.g. ordernumbers, items, names) which are only
# used in your current session, this script is for you ...
# Enter the fragments with ADD and use USE to get them on the actual line in the session,
# REMOVE removes the selected fragment.
#
# TRIM    - remove spaces in the fragment (e.g. copied and pasted from screen)
# [ENTER] - add the command string [ENTER] to the line
# REFRESH - put new sessions (started after the script) in the scope (sessionlist) of the script

# TIP: You can use the command keys like 'wrksplf[enter]' or [pf3]'
# To do:
# - filter on extension *.txt
# - use restored file name as default for save file
# - die on invalid file
# - remove sub 'serializedlist'
# - make helpdialog with trim and [enter]
# - make input field in edit dialog longer
# - update to new version
#
# March 20, 2003 version 0.2

import java.io as io
import java.lang.String
import org.python.util as util
from java  import awt
from javax import swing

def Trim(old):
	"Remove unnecessary spaces and set the correct newline"
	return java.lang.String(java.lang.String(old).replaceAll('\s{2,}', ' ')).replaceFirst('\s$', '');

def Enter(old):
	return old + '[enter]'
	
class SerializedList(io.Serializable):
	"Only used to serialize the list"

	def __init__(self, value):
		self.value = value

class ActiveSession:
	"Get the active session"
	
  	def __init__(self, _session):
		self.sessionManager = _session.getSessionManager()
		self._getSessions()
		
	def _getSessions(self):
		"Get the current existing sessions"
		self.sessions = self.sessionManager.getSessions()
		self.sessList = self.sessions.getSessionsList()

	def refreshSessions(self):
		"Rebuild the session list"
		self._getSessions()

	def getActiveSession(self):
		"Get the active session"
		for x in self.sessList:
			if (x.isVisible() == 1):
				theScreen = x.getScreen()
		return theScreen

class EditFragment(swing.JDialog):
    "Return the value in the edit field"

    def __init__(self, dialogparent):
        swing.JDialog.__init__(self, dialogparent, 'Edit the fragment', modal = 1)
	myPanel     = swing.JPanel()
	self.myText = swing.JTextField(20)
	myPanel.add(self.myText)
	myPanel.add(swing.JButton('OK',     actionPerformed = self.__ok))
	myPanel.add(swing.JButton('Cancel', actionPerformed = self.__cancel))
	self.contentPane.add(myPanel)
	self.pack()

    def EditValue(self, editvalue):
        self.editvalue = editvalue
       	self.myText.setText(self.editvalue)
       	self.setVisible(1)
       	return self.editvalue

    def __ok(self, event):
        "Accept the new value"
        self.setVisible(0)
        self.editvalue = self.myText.getText()

    def __cancel(self, event):
        "Ignore the new value"
        self.setVisible(0)

class Fragments(swing.JFrame):
	"The fragments class"

	def __init__(self):
		self.myFrame = swing.JFrame.__init__(self, 'Fragments')

		self.mydlg     = EditFragment(self)
		self.mySession = ActiveSession(_session)

		self.myListModel = swing.DefaultListModel()
		self.myList      = swing.JList(self.myListModel)
		myMidPanel       = swing.JScrollPane(self.myList)

		myTopPanel   = swing.JPanel()
		self.myEntry = swing.JTextField(40)
		myTopPanel.add(self.myEntry)
		myTopPanel.add(swing.JButton('Add',     actionPerformed=self.AddValue))
		myTopPanel.add(swing.JButton('Trim',    actionPerformed=self.TrimValue))
		myTopPanel.add(swing.JButton('[Enter]', actionPerformed=self.EnterValue))

		myPanel = swing.JPanel()
		myPanel.add(swing.JButton('Edit',    actionPerformed=self.EditValue))
		myPanel.add(swing.JButton('Remove',  actionPerformed=self.RemoveValue))
		myPanel.add(swing.JButton('Use',     actionPerformed=self.UseValue))
		myPanel.add(swing.JButton('Refresh', actionPerformed=self.RefreshSessions))
		myPanel.add(swing.JButton('Save',    actionPerformed=self.SaveValue))
		myPanel.add(swing.JButton('Restore', actionPerformed=self.RstValue))
		
		self.contentPane.add(myTopPanel, awt.BorderLayout.NORTH)
		self.contentPane.add(myMidPanel, awt.BorderLayout.CENTER)
		self.contentPane.add(myPanel,    awt.BorderLayout.SOUTH)

		self.pack()

	def AddValue(self, event):
		self.myListModel.addElement(self.myEntry.getText())
		self.myEntry.setText('')

	def TrimValue(self, event):
		self.myEntry.setText(Trim(self.myEntry.getText()))

	def EnterValue(self, event):
		self.myEntry.setText(Enter(self.myEntry.getText()))

	def EditValue(self, event):
		result = self.mydlg.EditValue(self.myListModel.elementAt(self.myList.getSelectedIndex()))
		self.myListModel.setElementAt(result, self.myList.getSelectedIndex())

	def RemoveValue(self, event):
		try:
			elem = self.myListModel.elementAt(self.myList.getSelectedIndex())
		except:
			pass
		else:
			self.myListModel.removeElement(elem)

	def UseValue(self, event):
		try:
			elem = self.myListModel.elementAt(self.myList.getSelectedIndex())
		except:
			pass
		else:
			activeScreen = self.mySession.getActiveSession()
			activeScreen.sendKeys(elem)

	def RefreshSessions(self, event):
		"Rebuild the currrent sessionslist"
		self.mySession.refreshSessions()

	def SaveValue(self, event):
		"Serialize the default list model"
		chooser   = swing.JFileChooser()
		if not (chooser.showSaveDialog(self.myFrame)):
			file     = chooser.getSelectedFile()
			filename = file.getAbsolutePath()
			elements  = self.myListModel.elements()
			fp = open(filename, 'w')
			for element in elements:
				fp.write(element)
				fp.write('\n')
			fp.close()

	def RstValue(self, event):
		"Get the serialized object and put it into the list"
		chooser = swing.JFileChooser()
		if not (chooser.showOpenDialog(self.myFrame)):
			file     = chooser.getSelectedFile()
			filename = file.getAbsolutePath()
			fp = open(filename, 'r')
			self.myListModel.clear()
			for line in fp.readlines():
				self.myListModel.addElement(line)
			fp.close()
			
	def main(self):
		"The main method"
		self.setVisible(1)

myApp = Fragments()
myApp.main()

# Fragments.py acts between "paste" (one temporary fragment) and "macros" (many saved fragments).
# When you need to paste several text parts or fragments (e.g. ordernumbers, items, names) which are only
# used in your current session, this script is for you ...
# Enter the fragments with ADD and use USE to get them on the actual line in the session,
# REMOVE removes the selected fragment.
# TIP: You can use the command keys like 'wrksplf[enter]' or [pf3]'
# 
# August 7, 2002 version 0.1

from java  import awt
from javax import swing
import java

def AddValue(event):
	myListModel.addElement(myEntry.getText())
	myEntry.setText('')

def RemoveValue(event):
	try:
		elem = myListModel.elementAt(myList.getSelectedIndex())
	except:
		pass
	else:
		myListModel.removeElement(elem)

def UseValue(event):
	try:
		elem = myListModel.elementAt(myList.getSelectedIndex())
	except:
		pass
	else:
		screen.sendKeys(elem)

screen = _session.getScreen()

myListModel = swing.DefaultListModel()
myList      = swing.JList(myListModel)
myListPane  = swing.JScrollPane(myList)

myEntry   = swing.JTextField(10)
myButIn   = swing.JButton('Add',    actionPerformed=AddValue)
myButOut  = swing.JButton('Remove', actionPerformed=RemoveValue)
myButExit = swing.JButton('Use',    actionPerformed=UseValue)
myButPane = swing.JPanel()
myButPane.add(myEntry)
myButPane.add(myButIn)
myButPane.add(myButOut)
myButPane.add(myButExit)

myFrame  = swing.JFrame('Fragments', visible=1)
myFrame.contentPane.add(myListPane, awt.BorderLayout.CENTER)
myFrame.contentPane.add(myButPane,  awt.BorderLayout.SOUTH)
myFrame.pack()


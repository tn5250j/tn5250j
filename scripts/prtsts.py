######################################################################
#                            PRTSTS.PY                               #
# ================================================================== #
# Programmer    : Patrick Gerrit Jackie Bielen                       #
# Company       : Stafa Holland BV - Feeling for fasteners           #
# Functionality : Script for easy printer-management on your AS/400  #
#                 Specially created for system-operators.            #
# How it works  : When there is a message waiting for a specific     #
#                 printer, a window pops up telling you wich printer #
#                 has a message waiting to be answered.              #
# Feedback      : tn5250j-mailinglist or directly to bielen@st...    #
#                 ICQ #24140635 or private email pbielen@pl...       #
######################################################################

import java
import time
from javax import swing
import java.awt as awt

def DebugWindow():
    msg = swing.JDialog()
    msg.show()

def fillWithValues(row,col,width,sb):
    sPos = ((row - 1) * screen.getCols()) + (col - 1)
    while width > 0:
        sb.append(screenChars[sPos])
        sPos += 1
        width -= 1

def StatusWindow():
    win = swing.JFrame("Printer Status")
    win.size = (200,75 + (count * 25))
    win.contentPane.background = awt.Color.white
    win.contentPane.setLayout(awt.BorderLayout())
    win.contentPane.add(list,awt.BorderLayout.CENTER)
    win.show()

def waitWhileLocked(timeSeconds):
    while screen.getOIA().isKeyBoardLocked() :
        time.sleep(timeSeconds)    
   
#DebugWindow()
	
debug = 0

print debug.toString()
    
screen = _session.getScreen()
prtdev = java.lang.StringBuffer()
status = java.lang.StringBuffer()
modus = java.lang.StringBuffer()

done = 0    # Window displayed
basic = 0   # Basic or Advanced Mode - Reset at exit
amount = 0  # Amount of printers
count = 0   # Amount of messages waiting

# Checking Modus from here

screen.sendKeys("[pf9]WRKWTR[enter][pf21]")
waitWhileLocked(1)
screenChars = screen.getScreenAsChars()
modus = java.lang.StringBuffer()
fillWithValues(10, 37, 1, modus)
if modus.toString() == "1":
    basic = 1
    screen.sendKeys("2[enter]")
else:
    screen.sendKeys("[pf12]")

# Count the amount of printers

waitWhileLocked(1)
screenfields = screen.getScreenFields()
fields = screenfields.getFields()
FirstRow = 256
LastRow = 0

for x in fields:
    if x.startRow() < FirstRow:
        FirstRow = x.startRow()
    # Remember that the column is offset 0,0 so you have to make sure that
    #    it is less than 6 instead of 7 where the screen offset would start
    if x.startRow() > LastRow and x.startCol() < 6:
        LastRow = x.startRow()
	amount += 1

# The emulator keeps everything on base position 0,0 not 1,1
#    so we must add one to them at the end.
FirstRow += 1
LastRow += 1

# Debug mode (display amount and first and last row)
if debug:
    print "FirstRow : " + FirstRow.toString()
    print "LastRow  : " + LastRow.toString()
    print "Amount   : " + amount.toString()

while not _session.isStopMacroRequested() and not done:
    screen.sendKeys("[pf5]")
    waitWhileLocked(1)
    screenChars = screen.getScreenAsChars()
    times = 0
    lm = swing.DefaultListModel()
    list = swing.JList(lm)
    while times < amount:
        prtdev = java.lang.StringBuffer()
        fillWithValues(9 + times,7,10,prtdev)
        status = java.lang.StringBuffer()
        fillWithValues(9 + times,19,4,status)
        if status.toString() == "MSGW":
            lm.addElement("   " + prtdev.toString() + "   " + \
                          "Check Printer")
            count += 1
        times += 1
    if not lm.isEmpty():
        done = 1
        StatusWindow()
    else:
        if screen.getOIA().isKeyBoardLocked() :
            waitWhileLocked(10)
        else:
            time.sleep(10)

if basic == 1:
    screen.sendKeys("[pf21]1[enter][pf12][pf12]")


# testtillbottom.py will page down the wrkactjob screen area until Bottom
# it will also print out the text for Subsystem/job to the screen on each page down
#
# August 22, 2002 version 0.1
# Author Kenneth J. Pouncey
import java
import time

# Fill a StringBuffer with an area from the screen
def fillWithSubArea(row,col,width,sb):
    sPos = ((row - 1) * screen.getCols()) + (col - 1)
    while width > 0:
        c = screenChars[sPos]
        sb.append(screenChars[sPos])
        sPos += 1
        width -= 1


# Get the screen object from _session object
screen = _session.getScreen()
screenChars = screen.getScreenAsChars()

# Check for advanced users
advanced = java.lang.StringBuffer()
advanced.setLength(0)
fillWithSubArea(22,2,2,advanced)

pfKeys = advanced.toString()

if pfKeys == "F3":
    lines = 9
    bottomLine = 19
else:
    lines = 11
    bottomLine = 21

# define bottom variable text
bottomText = "Bottom"

# Create some work variables to hold screen characters
bottom = java.lang.StringBuffer()
sj = java.lang.StringBuffer()

# Loop until we are asked to stop or literal is found on the screen
while not _session.isStopMacroRequested():
    screenChars = screen.getScreenAsChars()
    times = 0
    #loop 9 times to read all the rows from 10 to 18
    while times < lines:
        sj.setLength(0)
        # get the Subsystem/job information for each row 
        fillWithSubArea(10 + times,7,14,sj)
        # print it out
        print sj.toString()
        times += 1

    # check for bottom literal
    bottom.setLength(0)
    fillWithSubArea(bottomLine,74,len(bottomText),bottom)

    whereAreWe = bottom.toString()
    if whereAreWe == bottomText:
        print 'we are at the bottom'
        break
    else:
        # if bottom is not found yet then page down for the next page
        screen.sendKeys("[pgdown]")
        time.sleep(1)

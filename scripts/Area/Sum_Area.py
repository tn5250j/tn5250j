# Sum_Area.py will sum the values selected by a bounded area of the screen
# August 14, 2002 version 0.1
# Author Kenneth J. Pouncey

from java  import awt
from javax import swing
import java

def fillWithSubArea(row,col,width,sb):
    sPos = ((row - 1) * screen.getCols()) + (col - 1)
    while width > 0:
        c = screenChars[sPos]
        if (c >= '0' and c <= '9') or c== '.' or c == ',' or c == '-':
            sb.append(screenChars[sPos])
        sPos += 1
        width -= 1

screen = _session.getScreen()

sumDisplay = swing.JLabel()
sumRect = awt.Rectangle()
screen.getBoundingArea(sumRect)

screenChars = screen.getScreenAsChars()
sb = java.lang.StringBuffer()
sPos = ((sumRect.x - 1) * screen.getCols()) + (sumRect.y - 1)
width = sumRect.width
offset = 0
times = sumRect.height
sumVector = java.util.Vector()

while times > 0:
    sb.setLength(0)
    fillWithSubArea(sumRect.x + offset,sumRect.y,width,sb)
    if sb.length() > 0:
        sumVector.add(sb.toString())
    offset += 1
    times -= 1

df = java.text.NumberFormat.getInstance() ;

dfs = df.getDecimalFormatSymbols();

sum = 0.0
for x in sumVector:
    try:
        n = df.parse(x);
    except:
        pass
    else:
        print x,str(n);
        sum += n;

sumDisplay.setText(str(sum))

mySumPane = swing.JPanel()
mySumPane.add(sumDisplay)

sumFrame = swing.JFrame('Sum Area', visible=1)
sumFrame.contentPane.add(mySumPane, awt.BorderLayout.CENTER)
sumFrame.pack()


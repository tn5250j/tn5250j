#*******************************************************************
#  Show Session Information in a split screen
#  -----------------------------
#  Author : jorjun@mac.com
#
#*******************************************************************
from java.lang import *
import java.io as io
import java.awt as awt
import java.util as util
from javax.swing import *
import org.tn5250j as tnj
#===================================================================
# Helpers
#===================================================================
def makeJListFromProperty(this):
	lstPrpNam = this.propertyNames()
	lstPrpVal =[]
	for key in lstPrpNam:
		lstPrpVal.append("%s = %s"%(key,this.getProperty(key) ) )
	lstPrpVal.sort()
	return JList(lstPrpVal)
#===================================================================
# Get Sessions properties
#===================================================================
##inStream = io.FileInputStream("test/tn5250jMsgs.properties")
config = tnj.GlobalConfigure.instance()
prpSes = config.getProperties(config.SESSIONS)
jLst1 = makeJListFromProperty(prpSes)
#===================================================================
# Get System Properties
#===================================================================
prpSys = System.getProperties()
jLst2 = makeJListFromProperty(prpSys)
#===================================================================
# GUI Stuff
#===================================================================
minSize = awt.Dimension(100, 50)
scrPan1 = JScrollPane(jLst1); scrPan1.setMinimumSize(minSize)
scrPan2 = JScrollPane(jLst2); scrPan2.setMinimumSize(minSize)
splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, scrPan1, scrPan2)
splitPane.setPreferredSize(awt.Dimension(600, 200) )

frame = JFrame("System Properties")
frame.getContentPane().add(splitPane)
frame.pack()
frame.show()
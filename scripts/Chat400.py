######################################################################
#                            chat400                                 #
#====================================================================#
# Code      : Peter Moore (jorjun@mac.com)                           #
#             Patrick Bielen (bielen@stafa.nl)                       #
# ================================================================== #
# Functionality : Script to send/receive messages to AS/400 users    #
# Discussions   : tn5250j-scripting@lists.sourceforge.net            #
######################################################################

import sys
import time
from java.lang import *
import com.ibm.as400.access as acc
import com.ibm.as400.resource as rsc
from javax import swing
import java.awt as awt
import java

global server, curUsr, name, passw
#**************************************************************************
server = ''
curUsr = ''
name = 'CHAT400'
passw = 'AS400'
CHATQ = "/QSYS.LIB/QGPL.LIB/chat400.DTAQ"
KEYLEN = 10
#==========================================================================
class Poller(Runnable):
#==========================================================================
    def __init__(self, parent):
        self.parent = parent

    def run(self):
        done = 0
        dq = acc.KeyedDataQueue(self.parent.as400, CHATQ)
        #curUsr = self.parent.as400.getUserId() # Current User ID.
        if not dq.exists():
            dq.create(KEYLEN, 512)

        while not done:
            try:
                entry = dq.read(curUsr,-1,"EQ")
                mail = entry.getString()
                sndUsr = mail.split(':')[0]
                if not self.parent.jobDct.has_key(sndUsr):
                    self.parent.rtvIntJobs()
                # Split only once do avoid problems text containing more :'s
                msg = mail.split(':', 1)[1]
                if msg == 'Logged and ready':
                    continue
                elif msg == 'Logged Out':
                    # wait for refreshing and give the sessions some time to close.
                    time.sleep(5)
                    self.parent.rtvIntJobs()
                    continue
                self.parent.rpyTxt.append("--== Received from " + sndUsr \
                + " ==--\n" + msg + "\n")
                self.parent.rpyTxt.setCaretPosition(len(self.parent.rpyTxt.getText()))
            except:
                done=1
                exc=sys.exc_info()
                print "Thread interrupted at line ", exc[2].tb_lineno
                print exc[0]
                print "type = %s"%type(exc[0])
                self.statusTxt.text='There was an error, contact the System-Administrator'
            if self.parent.chkActive.isSelected():
                item = sndUsr + ': ' + self.parent.usrDct[sndUsr]
            else:
                if self.parent.jobDct.has_key(sndUsr):
                    item = sndUsr + ': ' + self.parent.usrDct[sndUsr] + ' *'
                else:
                    item = sndUsr + ': ' + self.parent.usrDct[sndUsr]
            try:
                self.parent.users.setSelectedItem(item)
            except:
                None
            if self.parent.getState() == swing.JFrame.ICONIFIED:
                self.parent.state=(swing.JFrame.NORMAL)
            self.parent.chatTxt.requestFocus()
            self.parent.show()

#==========================================================================
class Chat400(swing.JFrame, awt.event.WindowListener):
#==========================================================================
    def __init__(self):
        swing.JFrame.__init__(self, title=curUsr + " - CHAT400", resizable=0)
        try:
            self.setDefaultCloseOperation(swing.WindowConstants.EXIT_ON_CLOSE) # JDK1.4?
            self.setLocation(275, 150)
        except:
            None 
    def run(self, server, name, *passw):
        self.as400 = acc.AS400(server, name, *passw)

        # Get user profile descriptions==> usrDct
        rUsrLst = rsc.RUserList(self.as400)  
        rUsrLst.open()
        rUsrLst.waitForComplete()
        self.usrDct = {}
        for idx in range(rUsrLst.getListLength()):
                tmp_rUsr = rUsrLst.resourceAt(idx)
                key_usr = tmp_rUsr.getAttributeValue(rsc.RUser.USER_PROFILE_NAME)
                if key_usr.startswith('Q') or key_usr == 'FAXSTAR':
                    continue
                tmp_usrText = tmp_rUsr.getAttributeValue(rsc.RUser.TEXT_DESCRIPTION)
                self.usrDct[key_usr] = tmp_usrText
        rUsrLst.close()
        
        # Interactive job list      
        self.jobLst = rsc.RJobList(self.as400)
        self.jobLst.setSelectionValue(rsc.RJobList.PRIMARY_JOB_STATUSES, \
                                      rsc.RJob.JOB_STATUS_ACTIVE)
        self.jobLst.setSelectionValue(rsc.RJobList.JOB_TYPE, \
                                      rsc.RJob.JOB_TYPE_INTERACTIVE)
        self.jobLst.setSortValue([rsc.RJob.USER_NAME, rsc.RJob.JOB_NAME])

        # Thread of execution to receive instant messages
        self.polchat = Thread(Poller(self))

        # Form GUI
        self.contentPane.setLayout(awt.GridBagLayout())
        self.addWindowListener(self)
        self.chkActive = swing.JCheckBox("Show only Active Users", 1)

        self.chatTxt = swing.JTextArea(5, 30, lineWrap=1, wrapStyleWord=1)
        self.rpyTxt = swing.JTextArea(10, 30, lineWrap=1, wrapStyleWord=1)
        self.users = swing.JComboBox(preferredSize=(250, 25), minimumSize=(250, 25))

        self.showGui()

    def windowClosed(self, event):
        self.polchat.interrupt()
        self.dispose()
    def windowClosing(self, event):
        System.setProperty("chat400", "None")
        keys = self.jobDct.keys()
        keys.sort()
        dq = acc.KeyedDataQueue(self.as400, CHATQ)
        for key_usr in keys:
            user = key_usr
            if user ==  curUsr:
                continue
            if not dq.exists():
                dq.create(KEYLEN, 512)
            try:
                dq.write(user, "%s:%s"%(curUsr, 'Logged Out'))
            except:
                None        
        self.sessionManager = _session.getSessionManager()
        self.sessions = self.sessionManager.getSessions()
        self.sessList = self.sessions.getSessionsList()
        for x in self.sessList:
            self.sessionManager.closeSession(x)
        # _session.closeSession() 
    def windowActivated(self, event):
        None
    def windowDeactivated(self, event):
        None
    def windowOpened(self, event):
        keys = self.jobDct.keys()
        keys.sort()
        dq = acc.KeyedDataQueue(self.as400, CHATQ)
        for key_usr in keys:
            user = key_usr
            if not dq.exists():
                dq.create(KEYLEN, 512)
            try:
                dq.write(user, "%s:%s"%(curUsr, 'Logged and ready'))
            except:
                None
    def windowIconified(self, event):
        None
    def windowDeiconified(self, event):
        None

    #**************************
    #   Retrieve send-user list
    #**************************
    def rtvIntJobs(self):
        try:
            self.users.removeAllItems()
        except:
            None
        # Get interactive job list
        self.jobLst.open()
        self.jobLst.waitForComplete()
        self.jobDct = {}
        for idx in range(self.jobLst.getListLength()):
            tmp_job = self.jobLst.resourceAt(idx)
            key_usr = tmp_job.getAttributeValue(rsc.RJob.USER_NAME)
            if not self.jobDct.has_key(key_usr):
                self.jobDct[key_usr] = tmp_job
        self.jobLst.close()
        
        keys = self.usrDct.keys()
        keys.sort()
        self.users.addItem("ALL TRASH: Vuilnisbak")
        for key_usr in keys:
            menuItem = key_usr
            sts = ''
            if self.jobDct.has_key(key_usr):
                if not self.chkActive.isSelected():
                    sts = ' *'
            try:
                fullName = self.usrDct[key_usr]
            except:
                fullName  = "Not Defined"
            menuItem += ': %s' %fullName
            if self.chkActive.isSelected() and not self.jobDct.has_key(key_usr):   # Active jobs only
                continue

            menuItem += sts    # N.B. * means profile is running an interactive job
            self.users.addItem(menuItem)

    #**************************
    #   Send message
    #**************************
    def btnActSnd(self, event):
        cmd = acc.CommandCall(self.as400)
        #curUsr = self.as400.getUserId()
        selected = self.users.getSelectedItem()
        sndUsr =selected.split(':')[0]
        chatTxt = self.chatTxt.getText()
        dq = acc.KeyedDataQueue(self.as400, CHATQ)
        if not dq.exists():
            dq.create(KEYLEN, 512)
        try:
            if not sndUsr == "ALL TRASH":
            	dq.write(sndUsr, "%s:%s"%(curUsr, chatTxt) )
            if not curUsr == sndUsr:
                self.rpyTxt.append("%s -->> %s\n%s\n"%(curUsr, sndUsr, chatTxt))
                self.rpyTxt.setCaretPosition( len(self.rpyTxt.getText()) )
            if not sndUsr == "ALL TRASH":
				self.statusTxt.text='Message Sended.'
            else:
                self.rpyTxt.append("MESSAGE IS SENDED TO TRASHCAN !!!")
                self.rpyTxt.append("\nALWAYS PAY ATTENTION WHO YOU'RE SENDING TO !!!!\n")
                self.rpyTxt.setCaretPosition( len(self.rpyTxt.getText()) )
                self.statusTxt.text='Message sended to trashcan - Pay Attention Please !'
            self.chatTxt.selectAll()
            self.chatTxt.cut()
        except:
            self.statusTxt.text='Message send Failed - Contact your system-operator.'
        self.chatTxt.requestFocus()

    #**************************
    def btnActRef(self, event):
        self.rtvIntJobs()
    #**************************
    def showGui(self):
        self.rtvIntJobs()

        self.btnRef = swing.JButton("Refresh List", \
                                    actionPerformed = self.btnActRef, \
                                    minimumSize=(100,25), \
                                    preferredSize=(100, 25))
        self.btnRef.setMnemonic('R')
        self.btnSnd = swing.JButton("Send Message", \
                                    actionPerformed = self.btnActSnd)
        self.btnSnd.setMnemonic('S')
        self.label1 = swing.JLabel("Send To:", minimumSize=(50, 25), \
                                   preferredSize=(50, 25))
        self.rootPane.setDefaultButton(self.btnSnd)
        self.rpyTxt.setEditable(0)      # <Scrollable message reply text area>
        self.statusTxt = swing.JTextField(text='Welcome to CHAT400 - An AS/400 Instant Messenger',\
                                          editable=0, horizontalAlignment=swing.JTextField.CENTER)

        gbc = awt.GridBagConstraints()
        # Build the screen
        # Label 'send to'
        gbc.insets = awt.Insets(10, 10, 5, 5)
        self.contentPane.add(self.label1, gbc)
        # Combobox list of users
        gbc.insets = awt.Insets(10, 5, 5, 5)
        self.contentPane.add(self.users, gbc)
        # Refresh User-List Button
        gbc.insets = awt.Insets(10, 5, 5, 10)
        self.contentPane.add(self.btnRef, gbc)
        # Active Users checkbox
        gbc.gridx = 0
        gbc.gridwidth = 3
        gbc.insets = awt.Insets(0, 0, 5, 0)
        self.contentPane.add(self.chkActive, gbc)
        # Send Message Button
        gbc.gridx = 0
        gbc.gridwidth = 3
        gbc.insets = awt.Insets(5, 0, 5, 0)
        self.contentPane.add(self.btnSnd, gbc)
        # Build the SplitPane (2 scrollpanes)
        scrollPane1 = swing.JScrollPane(self.chatTxt, swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, \
        swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        scrollPane1.setViewportView(self.chatTxt)
        scrollPane2 = swing.JScrollPane(self.rpyTxt, swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, \
        swing.JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        scrollPane2.setViewportView(self.rpyTxt)
        splitPane = swing.JSplitPane(swing.JSplitPane.VERTICAL_SPLIT, scrollPane1, scrollPane2)
        # Add the SplitPane
        gbc.gridx = 0
        gbc.gridwidth = 3
        gbc.fill = awt.GridBagConstraints.HORIZONTAL
        gbc.insets = awt.Insets(5, 10, 10, 10)
        self.contentPane.add(splitPane, gbc)
        # Add a status-textfield on the bottom, to display status or errors
        gbc.gridx = 0
        gbc.gridwidth = 3
        gbc.fill = awt.GridBagConstraints.HORIZONTAL
        gbc.insets = awt.Insets(0, 10, 10, 10)
        self.contentPane.add(self.statusTxt, gbc)

        self.pack()
        self.polchat.start()

        self.show()
        
def fillWithValues(row,col,width,sb):
    sPos = ((row - 1) * screen.getCols()) + (col - 1)
    while width > 0:
        sb.append(screenChars[sPos])
        sPos += 1
        width -= 1

try:
    server = _session.getHostName()
    curUsr = _session.getConfigurationResource().upper()
except:
    None
ready = 0
while not ready:
    screen = _session.getScreen()
    logon = java.lang.StringBuffer()
    screenChars = screen.getScreenAsChars()
    fillWithValues(24, 41, 35, logon)
    if logon.toString() == "(C) COPYRIGHT IBM CORP. 1980, 2000.":
        time.sleep(2)
    else:
        ready = 1
isRunning = System.getProperty("chat400")
if not isRunning == curUsr:
    System.setProperty("chat400", curUsr)
    chatter=Chat400()
    chatter.run(server, name, passw)
    try:
        _session.requestFocus()
    except:
        None
######################################################################
#   jyinfo.py : Only runs when jython 2-1 is installed completely.   #
#               Does not run from within tn5250j at the moment.      #
#               If possible it will be enhanced to run from tn5250j. #
######################################################################

"""GUI for browsing events, properties, and methods of Java classes as 
    seen from Jython.

-----------------------------------------------------------------------------
(c) Copyright by Paul M. Magwene, 2003  (mailto:pmagwene@sas.upenn.edu)

    Permission to use, copy, modify, and distribute this software and its
    documentation for any purpose and without fee or royalty is hereby granted,
    provided that the above copyright notice appear in all copies and that
    both that copyright notice and this permission notice appear in
    supporting documentation or portions thereof, including modifications,
    that you make.

    THE AUTHOR PAUL M. MAGWENE DISCLAIMS ALL WARRANTIES WITH REGARD TO
    THIS SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
    FITNESS, IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL,
    INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING
    FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT,
    NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION
    WITH THE USE OR PERFORMANCE OF THIS SOFTWARE !
-----------------------------------------------------------------------------
"""

from java.util import Enumeration
from javax.swing.tree import TreeNode
from javax.swing.tree import TreeModel
from javax.swing.event import TreeModelEvent
from java.lang import Exception
from java.lang import Object
#------------------------------------------------------------------------------#
# GridBag taken from pawt package, included here to obviate need for jython lib
#------------------------------------------------------------------------------#
# GridBag class
class GridBag:
	def __init__(self, frame, **defaults):
		self.frame = frame
		self.gridbag = awt.GridBagLayout()
		self.defaults = defaults
		frame.setLayout(self.gridbag)

	def addRow(self, widget, **kw):
		kw['gridwidth'] = 'REMAINDER'
		apply(self.add, (widget, ), kw)

	def add(self, widget, **kw):
		constraints = awt.GridBagConstraints()

		for key, value in self.defaults.items()+kw.items():
			if isinstance(value, type('')):
				value = getattr(awt.GridBagConstraints, value)
			setattr(constraints, key, value)
		self.gridbag.setConstraints(widget, constraints)
		self.frame.add(widget)
#------------------------------------------------------------------------------#
# The tree code and property/event code is adapted from
# Python Programming with the Java Class Libraries: A Tutorial for Building Web 
#    and Enterprise Applications with Jython
# By Richard Hightower, Addison-Wesley
#
#------------------------------------------------------------------------------#
# Enumeration class

class ListEnumeration(Enumeration):
    def __init__(self, the_list):
        self.list = the_list[:]
        self.count = len(self.list)
        self.index = 0
        
    def hasMoreElements(self):
        return self.index < self.count
        
    def nextElement(self):
        object = self.list[self.index]
        self.index = self.index + 1
        return object

#------------------------------------------------------------------------------#
# Node class

class SampleNode (Object, TreeNode):
    def __init__(self, name, items=[], parent=None, leaf=0):
        self.__nodes = []

        self.__name = name
        self.__parent = parent
        self.__leaf=leaf
        
        for name in items:
            node = SampleNode(name, parent=self, leaf=1)
            self.__nodes.append(node)

    def getChildAt(self, index):
        "Get the child at the given index"
        return self.__nodes[index]
            
    def children(self):
        'get children nodes ----------------'
        return ListEnumeration(self.__nodes)
            
        
    def getAllowsChildren(self):
        'Does this node allows children node?'
        return not self.leaf
        
    def getChildCount(self):
        'column count node'
        return len (self.__nodes)
    
    def getIndex(self, node):
        'get index of node in nodes'
        try:
            return self.__nodes.index(node)
        except ValueError, e:
            return None
    
    def getParent(self):
        'get parent node'
        return self.__parent
    
    def isLeaf(self):
        'is leaf node'
        return self.__leaf
    
    def __str__(self):
        'str node'
        return self.__name
        
    def toString(self):
        return self.__str__()
        
    def __repr__(self):
        nodes = []
        
        for node in self.__nodes:
            nodes.append(str(node))
            
        if (self.__parent):
            tpl=(self.__name, nodes, self.__parent, self.__leaf)
            return 'SampleNode(name=%s,list=%s,parent=%s,leaf=%s)' % tpl
        else:
            tpl=(self.__name, nodes, self.__leaf)
            return 'SampleNode(name=%s,list=%s,leaf=%s)' % tpl
    
    #-------- End Node interface, the below is for the SampleModel
    def add(self, node):
        self.__nodes.append(node)
        node.setParent(self)

    def setParent(self, parent):
        self.__parent = parent
        
    def setName(self, name):
        self.__name=name
    
    def getName(self, name):
        return self.__name
        


#------------------------------------------------------------------------------#
# Tree model

class SampleModel(TreeModel):
    debug = 0
    def __init__(self, root_name):
        if self.debug: print 'init'
        root = SampleNode(root_name, [])
        self._root = root
        self.listeners = []
        
    #------------ The following methods implement the TreeModel interface.        
    def addTreeModelListener(self, listener):
        if self.debug: print 'add listener'
        self.listeners.append(listener)
    
    def removeTreeModelListener(self, listener):
        if self.debug: print 'remove listener'
        self.listeners.remove(listener)
    
    def getChild(self, parent, index):
        if self.debug: print 'get child'
        return parent.getChildAt(index)

    def getChildCount(self, parent):
        if self.debug: print 'get child count'
        return parent.getChildCount()

    def getIndexOfChild(self, parent, child):
        if self.debug: print 'get index of child'
        return parent.getIndex(child)
    
    def getRoot(self):
        if self.debug: print 'get root'
        return self._root

    def isLeaf(self, node):
        if self.debug: print 'isLeaf'
        return node.isLeaf()

    def valueForPathChanged(self, path, newValue):
        if self.debug: print 'value changed for path'
        node = path.getLastPathComponent()
        node.setName(newValue)
        if self.debug: print 'Got a new Name ' + node.name

    #---------------- Helper methods
    def getNodePathToRoot(self, node):
        parent = node   # Holds the current node.
        path=[]        # To hold the path to root.
        
            # Get the path to the root
        while not parent is None:
                # Add the parent to the path and then get the
                # parent's parent
            path.append(parent)
            parent = parent.getParent()
            
            #Switch the order
        path.reverse()
        return path
        
        
    def fireStructureChanged(self, node):
            # Get the path to the root node.
            # Create a TreeModelEvent class instance.
        path = self.getNodePathToRoot(node)
        event = TreeModelEvent(self, path)
        
            # Notify every tree model listener that 
            # this tree model changed at the tree path.
        for listener in self.listeners:
            listener.treeStructureChanged(event)
            
    
    def addNode(self, name, children=[], parent=None):
            # Set the value of the leaf.
            # No children means the node is a leaf.
        leaf = len(children)==0
        
            # Create a SampleNode,
            # and add the node to the given parent.
        node = SampleNode(name, children, leaf=leaf)
        self.__add(node, parent)
        return node            
        
    def __add(self, node, parent=None):
            # If the parent is none, 
            # then set the parent to the root.
        if not parent:
            parent = self.getRoot()
            
            # Add the node to the parent,
            # and notify the world that the node changed.
        parent.add(node)    
        self.fireStructureChanged(parent)
        
#------------------------------------------------------------------------------#
# For querying Java classes on their event properties

from org.python.core import PyBeanProperty, PyBeanEventProperty, PyReflectedFunction
from java.lang.reflect import Modifier
from java.lang import Object

def getBeanInfo(bean):
    eventdict = {}
    propdict = {}
    methdict = {}
    tbean = bean
    while tbean != Object:            
        events = []
        properties = []
        methods = []
        for name, item in tbean.__dict__.items():
            if(type(item)==PyBeanEventProperty):
                events.append(item)
            if(type(item)==PyBeanProperty):
                properties.append(item)
            if(type(item)==PyReflectedFunction):
                methods.append(item)
        eventdict[tbean.__name__] = events
        propdict[tbean.__name__] = properties
        methdict[tbean.__name__] = methods
        tbean = tbean.superclass            
    return eventdict, propdict, methdict
    
def parseEventDict(edict):
    pedict = {}
    for key in edict.keys():
        pedict[key] = [e.__name__ for e in edict[key]]
    return pedict
    
def parsePropDict(pdict):
    ppdict = {}
    for key in pdict.keys():        
        ppdict[key] = [p.toString().split()[1] for p in pdict[key]]
    return ppdict

def parseMethDict(mdict):
    pmdict = {}
    for key in mdict.keys():
        pmdict[key] = [m.__name__ for m in mdict[key]]
    return pmdict

#------------------------------------------------------------------------------#
# GUI logic

from javax.swing import JFrame, JTextField, JButton, JPanel, JTree, JScrollPane, JLabel
import java.awt as awt
import java.lang as lang
import imp


def default_tree():
    tree_model = SampleModel("None")
    return tree_model

class InfoFrame(JFrame):
    def __init__(self, title=""):
        JFrame.__init__(self, title)
        self.size = 400,500
        self.windowClosing = self.closing
        
        label = JLabel(text="Class Name:") 
        label.horizontalAlignment = JLabel.RIGHT
        tpanel = JPanel(layout = awt.FlowLayout())
        self.text = JTextField(20, actionPerformed = self.entered)
        btn = JButton("Enter", actionPerformed = self.entered)
        tpanel.add(label)
        tpanel.add(self.text)
        tpanel.add(btn)
    
        bpanel = JPanel()
        self.tree = JTree(default_tree())
        scrollpane = JScrollPane(self.tree)
        scrollpane.setMinimumSize(awt.Dimension(200,200))
        scrollpane.setPreferredSize(awt.Dimension(350,400))
        bpanel.add(scrollpane)
        
        bag = GridBag(self.contentPane)
        bag.addRow(tpanel, fill='HORIZONTAL', weightx=1.0, weighty=0.5)
        bag.addRow(bpanel, fill='BOTH', weightx=0.5, weighty=1.0) 

    def closing(self, event):
        self.hide()
        self.dispose()
        
    def entered(self, event):
        name = self.text.getText()
        try:
            mod = __import__(name)
            components = name.split('.')
            for comp in components[1:]:
                mod = getattr(mod, comp)            
        except ImportError:
            mod = None
            self.setupTree("Invalid Class", {})
            return None
        edict, pdict, mdict = getBeanInfo(mod)
        pedict = parseEventDict(edict)
        ppdict = parsePropDict(pdict)
        pmdict = parseMethDict(mdict)
        
        self.setupTree(mod.__name__, pedict, ppdict, pmdict)     
          

    def setupTree(self, top, pedict, ppdict, pmdict):
        tree_model = SampleModel(top)
        events = tree_model.addNode("Events",["<<Events of the class and its ancestors>>"])
        props = tree_model.addNode("Properties",["<<Properties of the class and its ancestors>>"])
        meths = tree_model.addNode("Methods",["<<Methods of the class and its ancestors>>"])        
        
        for key in pedict.keys():
            tree_model.addNode(key, pedict[key], parent=events)
        for key in ppdict.keys():
            tree_model.addNode(key, ppdict[key], parent=props)
        for key in pmdict.keys():
            tree_model.addNode(key, pmdict[key], parent=meths)
        
        self.tree.setModel(tree_model)
        
        
                

#------------------------------------------------------------------------------#
def main():
	frame = InfoFrame("Java-to-Jython Event/Property/Method Browser")
	frame.show()

if __name__=='main' or __name__ =='__main__':
	main()        

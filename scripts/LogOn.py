import javax.swing as swing
import java.awt as awt

class LogOn(swing.JDialog):
    def __init__(self, parent=None):
	swing.JDialog.__init__(self, title="AS/400 - LogIn", modal=1)
	self.setDefaultCloseOperation(swing.WindowConstants.HIDE_ON_CLOSE)
	self.setResizable(0)
	self.setDefaultLookAndFeelDecorated(1)
	self.setLocationRelativeTo(self)
	self.contentPane.setLayout(awt.GridBagLayout())
	gbc = awt.GridBagConstraints()
		
	lbl1 = swing.JLabel("Server", preferredSize=(60, 22))
	lbl2 = swing.JLabel("Naam", preferredSize=(60, 22))
	lbl3 = swing.JLabel("Paswoord", preferredSize=(60, 22))
	self.txt1 = swing.JTextField(preferredSize=(125, 22))
	self.txt2 = swing.JTextField(preferredSize=(125, 22))
	self.txt3 = swing.JTextField(preferredSize=(125, 22))
	btn = swing.JButton("OK", preferredSize=(75, 25), actionPerformed=self.sendData)
	btn.setMnemonic('O')
		
	gbc.insets = awt.Insets(15, 10, 15, 5)
	self.contentPane.add(lbl1, gbc)
	gbc.insets = awt.Insets(15, 5, 15, 10)
	self.contentPane.add(self.txt1, gbc)
	gbc.gridx = 0
	gbc.insets = awt.Insets(0, 10, 0, 5)
	self.contentPane.add(lbl2, gbc)
	gbc.gridx = 1
	gbc.insets = awt.Insets(0, 5, 0, 10)
	self.contentPane.add(self.txt2, gbc)
	gbc.gridx = 0
	gbc.insets = awt.Insets(15, 10, 15, 5)
	self.contentPane.add(lbl3, gbc)
	gbc.gridx = 1
	gbc.insets = awt.Insets(15, 5, 15, 10)
	self.contentPane.add(self.txt3, gbc)
	gbc.gridx = 0
	gbc.gridwidth = 2
	gbc.insets = awt.Insets(10, 10, 20, 10)
	self.contentPane.add(btn, gbc)
	self.pack()
		
    def sendData(self, event):
	self.hide()

if __name__ == "__main__":
    dlg = LogOn()
    dlg.show()

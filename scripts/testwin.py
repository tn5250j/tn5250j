from javax import swing
import java.awt as awt

print "--------------- tn5250j test script start ------------"
win = swing.JFrame("tn5250j test window")
win.size = (300,400)
win.contentPane.background = awt.Color.white
label = swing.JLabel("Hello all you tn5250j'ers script writers")
label.foreground = awt.Color.red
win.contentPane.add(label)
win.show()

print "---------------- tn5250j test script end -------------"


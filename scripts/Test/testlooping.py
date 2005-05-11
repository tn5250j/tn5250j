import time

print "--------------- tn5250j test looping script start ------------"

screen = _session.getScreen()

while not _session.isStopMacroRequested():
    screen.sendKeys("[pf5]")
    time.sleep(5)

print "---------------- tn5250j test looping script end -------------"
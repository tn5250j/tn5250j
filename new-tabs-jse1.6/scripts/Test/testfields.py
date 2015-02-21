print "--------------- tn5250j test fields script start ------------"

screen = _session.getScreen()

screenfields = screen.getScreenFields()

fields = screenfields.getFields()

for x in fields:
    print x.toString()
    print x.getString()

print "number of fields %s " % screenfields.getSize()

print "---------------- tn5250j test fields script end -------------"

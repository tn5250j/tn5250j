######################################################################
#                           calc.py                                  #
#====================================================================#
#    Script to start the Windows Calculator from within TN5250j      #
#      Keep in mind this script only runs when using Windows.        #
#====================================================================#
#      Feel free to enchance the script to start a calculator        #
#      on linux-boxes also and then send it to bielen@stafa.nl       #
#       so that i can add it to CVS and all users can use it.        #
#====================================================================#
#       Special thanks to Pete Moore and Christian Geisert,          #
#       for helping me to use this way instead of os.system.         #
######################################################################

from java.lang import *
hostOs = System.getProperty('os.name')
cmd = 'unknown'
if (hostOs.startswith('Mac OS X') ):
	cmd = 'open /Applications/Calculator.app'
else:
	cmd = 'calc.exe'
print cmd
Runtime.getRuntime().exec(cmd)

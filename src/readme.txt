tn5250j README
------------------------------------------------------------------------------
This is an implementation of the 5250 telnet protocol in java.  I started it
because I could not find a 5250 emulator of Linux with the enhanced functions
like graphical windows, cursor progression, continued edit fields, masked edit
fields etc.....

new 0.5.7 release candidate 1

Added CodePage 1047 French Euro.

Added bypass signon System parameters.  Will add these to the session
attributes at a later time.

Added file transfer persistance of parameters per Session.  Later will add
capability to save off transfer information.

Added support for Transient Data.  What might not have worked before should
work so give it another try.

Fixed multiple script menu's showing up when the script menus point to the
same directory.

Fixed support for Mac OSX jdk 1.4.1 jvm.  The letters typed should appear now.

Fixed support for keyboard in Mac OSX.  The keyboard keys can not be distinguished
by left and right.

Added warning message about NULL capable field files.  They are not able to be
exported.

Bugs:
Lots of those but this fixes a lot I hope

Comments, questions, bug reports and patches are much appreciated - please
subscribe to the list and post them there if at all possible at sourceforge.net.

To post to the list, send mail to tn5250j-general@lists.sourceforge.net

If that's too much trouble, email me at the following:

kjp1@user.sourceforge.net

Make sure you reference tn5250j in the subject or there is a chance I might
delete it without opening the mail.

Sourceforge at http://sourceforge.net/projects/tn5250j/


Enjoy!



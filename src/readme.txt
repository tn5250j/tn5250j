tn5250j README
------------------------------------------------------------------------------
This is an implementation of the 5250 telnet protocol in java.  I started it
because I could not find a 5250 emulator of Linux with the enhanced functions
like graphical windows, cursor progression, continued edit fields, masked edit
fields etc.....

Changes since 0.5.7 pre release 2
------------------------------------------------------------------------------

Fixed the bounding area.
Added kunstoff look and feel - Patrick Bielen
Added access to functions/operations with password access
Added delete macros functionality - Right click on the macro
Fixed up some of the panels - Patrick Bielen
Fixed a lot of problems in E-mail - Patrick Bielen
Added new options to e-mail functions
   - New output to image .png
   - New access to just send quick message with file attachment -
         Patrick Bielen
   - Send screen image via e-mail - Fixes by Patrick Bielen
Added new options to define the margins for the Print Screen
     function.
Added auto login support
New splash screen donated by Carlo Daffara from Conecta http://www.conecta.it
Added blinking cursor.  Beta code and may leave traces
Added a new keep alive function to fix some network disconnect problems.

Lot's and lot's of bug fixes and improvements.

Well just run it to see all the new stuff.

==============================================================================
Changes since 0.5.7 pre release 1
------------------------------------------------------------------------------

Added antialias option for font use

Added Hide tab when only one tab option to connections dialog

Added tabs for options on the connections dialog

Added check to make system name obligatory when adding systems

Fixed connection dialog to allow double click on session to connect instead
of having to select it first.

Enhanced the spool file dialog to allow entering information and the option is
automatically selected.

Fixed an error when a window is defined with only a header or footer where
the window border does not show up.

Made file transfer dialog non modal within it's own frame so that it can
be minimized or moved out of the way.

Added code page 297 support for finnish

Other small bug fixes

0.5.7 pre release 1

Changed File transfer to add the column headers then text then field depending
on whether they exist or not for include full text.

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



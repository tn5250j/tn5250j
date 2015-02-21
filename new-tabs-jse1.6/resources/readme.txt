tn5250j README
------------------------------------------------------------------------------
This is an implementation of the 5250 telnet protocol in java.  I started it
because I could not find a 5250 emulator of Linux with the enhanced functions
like graphical windows, cursor progression, continued edit fields, masked edit
fields etc.....

Changes to last release since forever and a day
------------------------------------------------------------------------------
****  JDK Version WARNING *****
I am only going to release versions compiled with JDK 1.4 if you need a 1.3
version please download the sources and compile it or from CVS.  This will be
the last release that fully supports JDK 1.3.  If you have problems with this
please express them on the tn5250j-general mailing list or e-mail privately.
We need to get ready for the new JDK's and will only support the latest two
available jdk releases.
*******************************

I truthfully can not remember all that has happened since the last release
so will only list here some of what I remember and the most important.

First and foremost lots of bug fixes and enhancments.
Second the whole code base has been refactored to run in headless mode:
   1) You do not need to have the gui to have access to the data stream
      Can you say screen scraping?  Well hopefully it all works.   
   2) A ProtocolBean has been added for this.
   3) A SessionBean has been added for this.
   4) The code now uses events for all this information so you can write your
      own listeners to these events. There are some people that have used this for 
      such work so ask on the mailing list and am sure some samples can be
      given or at least help with it.  The events are in org.tn5250j.event

Logging modules have been added for log4j or built in console support.  You
can reference this on the Connection panel under the Logging Panel

New session properties have been added.  Take a look at the options for more
on this.

Anything else please ask on the mailing lists.  Hope you enjoy.

==============================================================================   

Changes since 0.5.7 pre release 3
------------------------------------------------------------------------------
Fixed problems with Field Exit on certain fields.

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
Fixed the tab hiding function
Added the an option to include the session name on the main panel.
Fixed Hex Map panel that had an error parsing.
Added an "open same" session mapped to Alt-U

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



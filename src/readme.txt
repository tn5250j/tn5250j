tn5250j README
------------------------------------------------------------------------------
This is an implementation of the 5250 telnet protocol in java.  I started it
because I could not find a 5250 emulator of Linux with the enhanced functions
like graphical windows, cursor progression, continued edit fields, masked edit
fields etc.....

new 0.5.6 release

*** NOTE *** *** NOTE *** *** NOTE *** *** NOTE ***

With this release you will have to remap your keyboard if
using JDK 1.4 or greater.  This is for existing installs only.

If using the installer for linux or Mac you will have to chmod +x the
tn5250j script in the bin directory of the install path used.

The interface may look like it is locked while using the web installer.
This happens while downloading the pack file from the website.  It will
install do not worry.

*** NOTE *** *** NOTE *** *** NOTE *** *** NOTE ***

Added a new web installer module.

Translations of German by Christian

Translations of Dutch by Patrick

Added a the scripting directory from the settings directory to
also be displayed in scripts menu items

Fixed a threading problem with exporting of spooled files.

Fixed another problem with defaults reported by Christian

Added support for 1.4.0 keymapping for position of keys.

Added support for multiple key bindings.

Added support for verification of mapping to existing keys.

The ant build file now checks for the existence of the the
correct jdk version and compiles the correct support
automatically.

Added a signoff region verification.

Added Icelandic codepage 871 by Karl Helgason, kalli@midverk.is

Added new default startup fonts per system.  This caused
problems because some fonts did not exist.

Fixed resize of frame not causing a re centering of the screen.  Report from James

New installations start with full screen.

Fixed a null pointer on key presses

Fixed problem with font changing on session attribute apply.

Fixed default configuration parameters.  Reported and tested
with James.

Fixed a few more criters that creeped in.

Added a translate binary option to the JDBC connection for
downloading of file.  Request and fix by Luca

Center the screen

Fixed some bounding area problems

Fix cancel of export files to really cancel the export thread

Add the ability to copy while the terminal is busy

Added a global configuration for where setting files are saved
when first running the emulator.  It is now user/home/.tn5250j
unless there is already an old configuration there.  This is with
help from Luc.

Added a new default session configuration.  There is still a 
problem with Linux default font.

Changed the Session Attributes to be more module allowing
for changing easily.

Added new on error attribute to define whether or not the 
terminal needs to be sent an error reset on error

Added code to suppor the ruler follows cursor attribute setting

Fixed the Confirm Signon screen attribute to actually check
that the session is on the signon screen before actually
closing the session.  There will be a confirmation.

Added new Codepage support by LDC, Wim and Luc.  There 
is still work to be done here but the infrastructure is there.

CodePage support for 1140 and 1142 - Christian

Speed enhancements - LDC, WVL and Luc

New Keyboard Handlers - no changes in functionality just
getting ready for some other enhancements like hopefully left
and right control key use.

Lots of code refactoring.

Unicode/EBCDIC conversion enhancements - LDC, WVL and Luc

New session attributes refactoring  - Done
- Session attributes are updated for all sessions using the same profile
- New signoff attributes page is now implemented.
     What this does is if the flag is checked then the first screen for 
     signon is saved.  When you close the session window it will check
     the screen against this save buffer and if it is not the same it
     will notify you that the screens are not the same and prompt for
     a continue or not.
- Start refactoring session attributes code for easier maintenance

E-mail addresses are now persistant based on session.  Just the start
   as there are more fields that can use this.

Export spool file updates
- New wizard config page for text export.  Thanks James for the help here
- Text export can now have an external editor attached that will open the
   file after export.  The editor is session persistant
- IFS file export now works.  Let's hope anyway.  Test it please
- Added new filter option accessed from the popup menu

Infrastruction being put in place for plug-in architecture.  Let's see
   where this leads.  No visable changes to the emulator just some
   code refactoring.  Our thoughts are that the file transfers and 
   spoolfile export will then move to this new plugin architecture.
   In the future though ;-)

Code Page 875 added for Greek
- Big thanks goes to  Kleandros Stogiannopoulos he has told me that
  if there are problems please contact him here kstogian@hotmail.com
  and MAKE SURE YOU REFERENCE tn5250j or he might not read it.

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



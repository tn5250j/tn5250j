tn5250j README
------------------------------------------------------------------------------
This is an implementation of the 5250 telnet protocol in java.  I started it
because I could not find a 5250 emulator of Linux with the enhanced functions
like graphical windows, cursor progression, continued edit fields, masked edit
fields etc.....

This, the 0.4.x release, is beta but is stable and being used in a development
environment so should not cause to many headaches but may contain bugs :-))

For languages supported look at option -L for command line options.

The following sections are available:
   1. Installing
   2. Quick Start
      2a.  Command Line Options (supported languages -L option)
   3. Keybindings
   4. Mousebindings
   5. Menu Options Descriptions
   6. Tested Systems/Operating systems
   7. Compiling the source
   8. Sending screen by e-mail
   9. Emulator used as an applet
  10. Acknowledgements


1. Installing
=======================

1)  Place the my5250.jar file in a directory that you choose.
2)  For e-mailing screen shots please read the e-mail.txt file included in the
       source file that was downloaded.

2. Quick Start
=======================
2)  java -jar my5250.jar

    Select the configure button to define sessions when the session selection
    window is displayed.
           or
    java -jar my5250.jar host -p port
           or
    java -jar my5250.jar host


    2a. Command line options:  (If you do not want to use the configure button)
    -------------------------
    -p port ----> port to be used - default is port 23 if not specified

    -f filename ----> configuration file

    -t ----> use system name instead of system id Host IP Address/DNS Host name.

    -cp ---> Code Page
                Supported code pages
                --------------------
                37 - US default
                37PT - Portuguese
                273 - German
                280 - Italian
                284 - Spanish
                297 - French
                500-ch - Switzerland
                870-pl - Poland

                Let me know of others that are needed.  The code pages can be
                updated quickly.

    -e ---->  Enhanced 5250 option.  This gives graphical windows, edit masked
               fields, continued edit fields, etc...
               ** Note ** not all enhanced options are implemented as yet.
               I have only implemented what our applications use here and should
               be considered as improving (buggy :-)).  At our site we have not
               had any problems with it yet (note the word yet please).
               Watch for further announcements and improvements.

    -L ---->   Specify the locale/language to be used for literals.  The default
               is the locale of the system if it is supported.

                  Available Languages
                     Default -> Supported locale of the system or English
                     Spanish -> -L es
                     German -> -L de
                     French  -> -L fr
                     Portuguese -> -L pt
                     Catalan -> -L ca_ES
                     Dutch -> -L nl
                     Italian -> -L it
                     Polish -> -L pl

    -132 ----> Change to 27*132 column.

    -s ------> Start up emulator using an existing configured session
                  you can specify multiple of these as in -s xxxx -s yyyy
                  and at startup the emulator will start up session xxxx and
                  yyyy

               ==========================================================
               **** NOTE **** Session names are case sensitive!!!!!!!
               ==========================================================


    -width --> Start up emulator using the width specified

    -height -> Start up emulator using the height specified

    -d ------> if no other instances are running and the -nc options is not
               specified then start up the bootstrap monitoring thread

    -nc -----> no check for other tn5250j instances that are running
                    A new frame will be created within another instance
                    of JVM

    -usp ----> Use Socks Proxy
    -sph ----> Socks Proxy Host
    -spp ----> Socks Proxy Port

    -dn -----> This option takes a device name parameter to be used
               by the Host.  The device name is 10 characters in
               length and following rfc2877 it the device name
               is already allocated then the device name will be
               sent again with a consecutive number appended.  If
               the device name plus appended sequencial number is
               longer than 10 characters then the device name is
               truncated until the device name and the appended
               sequential number fits within 10 characters.

             For example:
             -dn DEVICEUSER

             This will send the device name on allocation to the host
             as DEVICEUSER.  If there is already a session allocated
             with device name DEVICEUSER then the emulator will send
             the device name with a 1 (number one) appended as
             DEVICEUSE1 if this is allocated then DEVICEUSE2 and so
             on until the host says no more attempts are allowed or
             the same device name is sent twice in succession as per
             rfc2877.

             This option will also change the session name to that
             of the allocated device name.

    Example of command line options
    -------------------------------

    java -jar my5250.jar -L es

        This will change the default language of the system to use the languange
        translations of what is specified.

    java -jar my5250.jar hostAS400 -f hostAS400.prop

       This will connect to hostAS400 and use the file hostAS400.prop as the
       property file.  Any properties that are changed will be saved to this
       file under the current directory.  It does not have to exist as it will
       be created.  If there are any properties saved from a previous session
       then those will be read and used.  It uses the code page 37 which is
       default.

    java -jar my5250.jar parisAS400 -f parisAS400.prop -cp 297

        This will connect to parisAS400 use the property file as described above
        and use the code page 297 for french.

    java -jar my5250.jar spainAS400 -f spainAS400.prop -cp 284 -e

        This will connect to spainAS400 use the property file as described above
        and use the code page 284 for spanish and will notify the as400 that it
        can send enhanced commands.

      **** Note the -e can be used on all sessions.

       format of the session entries:
       ------------------------------

       systemName=route-to-host [options as described above]


       Sample sessions file
       --------------------

luxembourg=lux -f luxgui.prop -e -gui
lux-nogui=lux -f lux.prop -e
houilles=houilles -f houilles.prop -cp 297 -e
paris=paris -f paris.prop -cp 297 -e
spain=spain -f spain.prop -cp 284 -e


==========================================================
**** NOTE **** Session names are case sensitive!!!!!!!
==========================================================


3. Keybindings
==============

These are the default key bindings.  You can remap these by clicking on the
map keys from the popup menu.

F1		         PF1
F2		         PF2
F3    		   PF3
F4		         PF4
F5    		   PF5
F6		         PF6
F7	   	      PF7
F8		         PF8
F9		         PF9
F10		      PF10
F11		      PF11
F12		      PF12
Shift + F1	   PF13
Shift + F2	   PF14
Shift + F3	   PF15
Shift + F4	   PF16
Shift + F5	   PF17
Shift + F6	   PF18
Shift + F7	   PF19
Shift + F8	   PF20
Shift + F9	   PF21
Shift + F10	   PF22
Shift + F11	   PF23
Shift + F12	   PF24
Enter		      Field Exit
Ctrl		      Enter (note this includes both left and right control keys)
Shift + Esc	   System Request
Alt + ->	      Next Word
Alt + <-	      Prev Word
Alt + x		   Connect/Diconnect toggle
Alt + l		   Toggle ruler line vertical line, horizontal line, cross hair
Alt + r		   Reset
Alt + F1 	   Help
Alt + p		   Print Screen
Alt + h		   Host Print
Alt + d		   Display Attributes
Alt + c		   Copy
Alt + v		   Paste
Alt + m		   Display messages
Alt + n		   New Session
Alt + q		   Close session and remove from session tab panel
Alt + s		   Toggle Hotspots on and off
Alt + g		   Toggle GUI Interface on and off
Alt + t		   Transfer File
Insert		   Toggle insert mode (cursor changes shape to half height cursor)
		            Right shift of field characters in insert mode
Delete		   Delete character (with left shift of field characters)
BackSpace	   Delete Previous Character (with left shift of field characters)
Keypad plus	   Field Positive Key (works like field exit in non numeric fields)
Keypad minus	Field Minus Key
Tab		      Next Field
Shift + Tab	   Prev Field
Home		      Home position
End		      End of field
Alt + PageUp	Next Session
Alt + PageDn	Previous Session

Alt + O        Debug output mode (see not below)

*** NOTE *** Alt + O (not zero) by itself will turn on debugging mode that I use
for testing and will probably slow down the session painting because it outputs
all input data streams to log.txt and to the screen.  I will take this out as
soon as I can.  If you have any other suggestions or key combinations for this
let me know!!!!!!  Oh by the way this toggles the mode so if it is on then
hitting it one more time will turn it off.


4. Mousebindings - *** English Version ***
================

Right click on session screen pulls up a menu as follows:

:=====================:
: Copy                :
: Paste               :
: Paste Special       :
:---------------------:
: Selected Columns XX :
: Selected Rows XX    :
: Sum Area          > :
:---------------------:
: Print Screen        :
:---------------------:
: System Request      :
: Help                :
: Host Print          :
: Display Messages    :
:---------------------:
: Hex Map             :
: Map Keys...         :
: Settings...         :
:---------------------:
: Macros            > :
:---------------------:
: Transfer File...    :
: Send Screen         :
:---------------------:
: Connect/Disconnect  :
: Close this session  :
:=====================:


5. Menu Options Descriptions
============================

Copy - Copies bounded area or if no bounded area selected copies the screen to
         the clipboard to be pasted

Paste - Pastes clipboard data to the screen

Paste Special - This will paste the data from the clipboard stripping all non
                  letter and non numeric characters.

	Example:
	--------
                                 Display Report
                                              Report width . . . . . :     556
 Position to line  . . . . .              Shift to column  . . . . . .
 Line   ....+....1....+....2....+....3....+....4....+....5....+....6....+....7..
        Opening Branch......  Company Id..........  Application Code....       A
 000001         1,091                00069                   D
 000002         2,010                00069                   D
 000003         2,010                00069                   D
 000004         2,010                00069                   D
 000005         2,010                00069                   D


	The Opening Branch...... field has a comma within the field, if you do just a
   regular copy and paste into a field you would get 1,091 which sometimes is
   not formatted correctly so you would use the paste special and what would be
   pasted is 1091

Selected columns and Selected rows - shows the status of the selection.  We use
            this to count the characters of a selection.  Note these two entries
            will only show up if there is a bounded area specified.

Sum Area1   This option will only show up if there was a bounded area selected
            for example copy and paste.

            You can select two different formats to parse numbers.  The Grouping
            separator as ',' and a '.' as Decimal Separator, #,###.## or
            the grouping seaparator as '.' and a ',' as Decimal Separator,
            #.###,##  .

Print Screen - Prints the screen

System Request - Issues a System Request to the AS400 system

Help - NOTE this is not application help but the help key that is sent to the
         AS400

Host Print - This will send a host print to the AS400 which will print the
               screen to the users spool

Display Messages - Will only display if there are messages waiting (ie. the
                     MW light is on).  When the option is selected the messages
                     will be displayed.  Alt + M will also do this but you can
                     press this at any time.

Hex Map - Displays a hex map of ASCII characters that can be inserted into the
            current field

Map Keys - Displays a window to allow remapping of the keys for the emulator

Settings - Displays a window to allow the changing of the font, colors,
            cursor size and column separator.

Macros - If there are macros defined then this item will be displayed.  It will
            have a submenu of all macros defined across the sessions.  More
            information can be found in the macros.txt file.

Transfer file - The user is presented with a screen to fill out the file
                  information to be downloaded.  More information can be
                  found in the filetransfers.txt file.

Send Screen - Send screen via e-mail.  More information can be
                  found in the e-mail.txt file.

Connect/Disconnect - This is a toggle switch.  When connected this entry will
            read Disconnect and when disconnected this item will read Connect.

Close this session - Will popup a options panel that will allow you to close
            the current session or all sessions and remove the sessions from
            the session tab panel.

Left mouse button held down allows you to specify a bounding area.


6. Tested Systems/Operating systems
===================================

The JDK's tested are from Sun.  If there are any others please let me know.

MicroSoft
---------
Windows NT   - JDK 1.2.2, 1.3, 1.3.1, 1.4 beta3
Window 98    - JDK 1.2.2, 1.3
Window 2000  - JDK 1.2.2, 1.3, 1.3.1

The 1.4 beta 3 on windows and linux is closing the gap between consistency !!!!!

Linux
-----
Suse 6.4, 7.0, 7.1, 7.2, 7.3 using KDE and GNOME
Redhat 7.2

   Sun
   ---
 - JDK 1.3.x, JDK 1.4 beta 3 (still some problems here) Sun JDK

 The 1.4 beta 3 on windows and linux is closing the gap between consistency !!!!!

   IBM
   ---
 - JDK 1.3
   You will have to remap the keys to get the keys to work correctly.

!!! Please no operating system or desktop flames please !!!!

MAC OS
------
It works and I have screen shots now.  Thank you for sending me screen shots.
I still do not have any specifics of the JDK/JVM used.

7. Compiling the sourc
======================
   Please read the antbuild.tx file for compiling via Ant

8. Sending screen by e-mail
===========================
   Please read the e-mail.txt file for setting up sending screen via e-mail.

9. Emulator used as an applet
=============================
   Please read the applet.txt file for using the emulator as an applet

10. Acknowledgements
====================

Special thanks go out to the pre-beta testers that test the constant changes before
anyone else gets to see it.  When they say there are no problems then I release
the code to the others.

Irene Serrano
Jesus Irausquin
Didac Lopez
Ricardo Fermin
Patrick Bielen

Without their constant testing and feedback there would be a lot more bugs.

For changes to the current version/release please see the CHANGELOG.txt file.

Comments, questions, bug reports and patches are much appreciated - please
subscribe to the list and post them there if at all possible at sourceforge.net.

To post to the list, send mail to tn5250j-general@lists.sourceforge.net

If that's too much trouble, email me at the following:

kjpou@hotmail.com

Sourceforge at http://sourceforge.net/projects/tn5250j/


Enjoy!



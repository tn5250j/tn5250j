<html>
<head>
<title>Untitled Document</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>

<body>
	<APPLET  CODE = org.tn5250j.My5250Applet.class CODEBASE = <?php echo $HTTP_POST_VARS["CODEBASE"]; ?> ARCHIVE = my5250.jar 
		WIDTH = <?php echo $HTTP_POST_VARS["WIDTH"] ; ?> 
		HEIGHT = <?php ECHO $HTTP_POST_VARS["HEIGHT"]; ?> name = "tn5250 - Java tn5250 Client " ALIGN = top>
        <PARAM NAME = CODE VALUE = org.tn5250j.My5250Applet.class >
        <PARAM NAME = CODEBASE VALUE = "<?php echo $HTTP_POST_VARS["CODEBASE"]; ?>" >
    	<PARAM NAME = ARCHIVE VALUE = my5250.jar >
    	<PARAM NAME = name VALUE = "tn5250 - Java tn5250 Client " >
    	<PARAM NAME="type" VALUE="application/x-java-applet;version=1.4">
    	<PARAM NAME="scriptable" VALUE="false">
    	<PARAM NAME = "host" VALUE ="<?php echo $HTTP_POST_VARS["HOST"]; ?>">
    	<PARAM NAME = "-e" VALUE =" ">
    	<PARAM NAME = "-p" VALUE ="<?php echo $HTTP_POST_VARS["PORT"]; ?>">
    	<PARAM NAME = "height" VALUE ="<?php echo $HTTP_POST_VARS["HEIGHT"]; ?>">
    	<PARAM NAME = "width" VALUE ="<?php echo $HTTP_POST_VARS["WIDTH"]; ?>">
    	<PARAM NAME = "align" VALUE ="top">
    	<PARAM NAME = "-sslType" VALUE ="<?php echo $HTTP_POST_VARS["SSLTYPE"]; ?>">
	<PARAM NAME = "-dn" VALUE="<?php echo $HTTP_POST_VARS["DEVICE"]; ?>">
	</APPLET>
</body>
</html>

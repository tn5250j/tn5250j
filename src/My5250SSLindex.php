<?php
  $codebase="http://appletserverurl.com/path";
  $defaultHost="defaulthost.com";
  $defaultPort="992";
  $defaultDeviceName="";
  $defaultAppletWidth="640";
  $defaultAppletHeight="480";
  
?>
<html>
<head>
<title>Applet Parameter Configuration</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
</head>

<body>
<br>

<a href="<?php echo "$codebase/SSLinstructions.html"; ?>">Client Setup Instructions</a>

<form method="post" action="My5250SSLApplet.php">
    <input name="CODEBASE" type="hidden" id="CODEBASE" 
	value="<?php echo $codebase; ?>">
<p><strong>Connection Settings:</strong><br>
    Host: 
    <input name="HOST" type="text" id="HOST" value="<?php echo $defaultHost; ?>" size="40"><br>
    Port: 
    <input name="PORT" type="text" id="PORT" value="<?php echo $defaultPort; ?>" size="7">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    SSL Type: 
    <select name="SSLTYPE" id="SSLTYPE">
      <option value="NONE">NONE</option>
      <option value="SSLv2">SSLv2</option>
      <option value="SSLv3">SSLv3</option>
      <option value="TLS" selected>TLS</option>
    </select>
    <br>
    Device Name: 
    <input name="DEVICE" type="text" id="DEVICE" value="<?php echo $defaultDeviceName; ?>">
  </p>
  <p><strong>Screen Settings</strong> <br>
    Width: 
    <input name="WIDTH" type="text" id="WIDTH" value="<?php echo $defaultAppletWidth; ?>" size="6" maxlength="4">
    &nbsp;Pixels&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 
    <input type="submit" name="Submit" value="Connect">
    <br>
    Height: 
    <input name="HEIGHT" type="text" id="HEIGHT" value="<?php echo $defaultAppletHeight; ?>" size="6" maxlength="4">
    &nbsp;Pixels<br>
  </p>
  </form>
  <br>
  <br>
  <br>
</body>
</html>


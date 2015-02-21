@echo off
@rem ======================================
@rem DOS Batch file to invoke the emulator
@rem ======================================

setlocal

SET EMUL_PATH=$INSTALL_PATH
start javaw -jar "%EMUL_PATH%\lib\tn5250j.jar"

endlocal
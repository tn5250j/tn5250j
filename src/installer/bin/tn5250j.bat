@echo off
@rem ======================================
@rem DOS Batch file to invoke the emulator
@rem ======================================

SET EMUL_PATH=$INSTALL_PATH
java -jar "%EMUL_PATH%\lib\tn5250j.jar"

@echo on

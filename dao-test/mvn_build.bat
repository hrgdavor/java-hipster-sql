REM DEPS
echo OFF

cd ..\processor
call "mvnd" -DskipTests=true -T 8 install
if  %ERRORLEVEL% NEQ 0 goto error

cd ..\dao-test
call "mvnd" -T 8 clean compile
if  %ERRORLEVEL% NEQ 0 goto error

goto ok
:error
	cd ..\fgks-back
	echo .
	echo XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	echo .
    echo FAILED
	echo .
	echo XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
	echo .
    exit /b %errorlevel%

:ok
echo .
echo ........................................
echo .
echo SUCCESS
echo .
echo ........................................

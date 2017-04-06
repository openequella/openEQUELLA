@echo off
setlocal

call equellaserver-config.bat

set MAIN_CLASS=com.tle.core.equella.runner.EQUELLAServer
set JAVA_VM=%JAVA_HOME%/jre/bin/server/jvm.dll
IF NOT X%SERVICE_USER%==X SET SERVICE_FLAGS=--ServiceUser %SERVICE_USER% --ServicePassword %SERVICE_PASSWORD%

if "%OS%"=="Windows_NT" goto nt
echo This script only works with NT-based versions of Windows.
goto :eof

:nt
rem
rem Find the application home.
rem
rem %~dp0 is name of current script under NT
set _REALPATH=%~dp0
set _WRAPPER_EXE=%_REALPATH%prunsrv.exe
set WORKING_DIR=%_REALPATH%
set LOG_PATH=%_REALPATH%..^\logs

rem Remove spaces from service name
set SERVICE_NAME=%SERVICE_NAME: =%

set EXITPARAM=%2
if "%EXITPARAM%" == "exit" (
set EXIT=1
)

rem Find the requested command.
for /F %%v in ('echo %1^|findstr "^console$ ^start$ ^stop$ ^restart$ ^status$ ^quickstatus$ ^install$ ^update$ ^remove"') do call :exec set COMMAND=%%v

if "%COMMAND%" == "" (
    echo Usage: %0 { console : start : stop : restart : status : quickstatus : install : update : remove }
    pause
    goto :eof
) else (
    shift
)

rem set options
if not "%HEAP_CONFIG%" == "" (
	SET JAVA_ARGS=%JAVA_ARGS%%HEAP_CONFIG%
)

if not "%JMX_CONFIG%" == "" (
	SET JAVA_ARGS=%JAVA_ARGS%%JMX_CONFIG%
)

rem
rem Run the application.
rem At runtime, the current directory will be that of Wrapper.exe
rem
call :installed
call :%COMMAND%
goto :eof


:console
if %INSTALLED%==false goto :End
"%_WRAPPER_EXE%" "//TS//%SERVICE_NAME%"
goto :eof

:start
if %INSTALLED%==false goto :End
call :update>NUL
echo Starting EQUELLA
NET START %SERVICE_NAME%
goto :eof

:stop
if %INSTALLED%==false goto :End
echo Stopping EQUELLA
NET STOP %SERVICE_NAME%
goto :eof

:install
echo Installing the EQUELLA service
"%_WRAPPER_EXE%" //IS//%SERVICE_NAME% --DisplayName="%DISPLAY_NAME%" --Description="%DISPLAY_NAME%" --Jvm="%JAVA_VM%" --StartMode=jvm --StopMode=jvm --StartClass=%MAIN_CLASS% --StartMethod=start --StopClass=%MAIN_CLASS% --StopMethod=stop --StopParams=stop --Classpath="%CLASS_PATH%" --JvmOptions="%JAVA_ARGS%" --LogPath="%LOG_PATH%" --StdOutput=auto --StdError=auto --StartPath="%WORKING_DIR%." --Startup=%START_TYPE% --LogPrefix="equellaserver" --PidFile="equellaserver.pid" %SERVICE_FLAGS%
COPY "prunmgr.exe" "%SERVICE_NAME%w.exe">NUL
goto :eof

:update
echo Updating the EQUELLA service
"%_WRAPPER_EXE%" //US//%SERVICE_NAME% --DisplayName="%DISPLAY_NAME%" --Description="%DISPLAY_NAME%" --Jvm="%JAVA_VM%" --StartMode=jvm --StopMode=jvm --StartClass=%MAIN_CLASS% --StartMethod=start --StopClass=%MAIN_CLASS% --StopMethod=stop --StopParams=stop --Classpath="%CLASS_PATH%" --JvmOptions="%JAVA_ARGS%" --LogPath="%LOG_PATH%" --StdOutput=auto --StdError=auto --StartPath="%WORKING_DIR%." --Startup=%START_TYPE% --LogPrefix="equellaserver" --PidFile="equellaserver.pid" %SERVICE_FLAGS%
COPY "prunmgr.exe" "%SERVICE_NAME%w.exe">NUL
goto :eof

:remove
echo Removing the EQUELLA service
"%_WRAPPER_EXE%" "//DS//%SERVICE_NAME%"
goto :eof

:status
FOR /F "tokens=4 delims= " %%A IN ('SC QUERY %SERVICE_NAME% ^| FIND "STATE"') DO SET status=%%A
IF X%status%==X echo Not installed
IF NOT X%status%==X echo Status is %status%
goto :eof

:quickstatus
FOR /F "tokens=4 delims= " %%A IN ('SC QUERY %SERVICE_NAME% ^| FIND "STATE"') DO SET status=%%A
IF X%status%==XRUNNING exit 2
exit 0
goto :eof

:restart
if %INSTALLED%==false goto :End
echo Restarting EQUELLA
call :stop
call :start
goto :eof

:installed
FOR /F "tokens=4 delims= " %%A IN ('SC QUERY %SERVICE_NAME% ^| FIND "STATE"') DO SET status=%%A
set INSTALLED=false
IF NOT X%status%==X set INSTALLED=true
goto :eof

:exec
%*
goto :eof

:End
echo Please install the EQUELLA service using the "install" command

:exit
if "%EXIT%" == "1" ( 
 exit /B %ERRORLEVEL% 
)
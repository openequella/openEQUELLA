@echo off
setlocal

call manager-config.bat

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
set _WRAPPER_CONF=%_REALPATH%manager.conf
set WORKING_DIR=%_REALPATH%
set LOG_PATH=%_REALPATH%..^\logs

set MAIN_CLASS=com.tle.upgrademanager.Main
set JAVA_VM=%JAVA_HOME%/bin/server/jvm.dll

rem Remove spaces from service name
set SERVICE_NAME=%SERVICE_NAME: =%
IF NOT X%SERVICE_USER%==X SET SERVICE_FLAGS=--ServiceUser %SERVICE_USER% --ServicePassword %SERVICE_PASSWORD%

rem Find the requested command.
for /F %%v in ('echo %1^|findstr "^console$ ^start$ ^stop$ ^restart$ ^status$ ^quickstatus$ ^install$ ^update$ ^remove"') do call :exec set COMMAND=%%v

if "%COMMAND%" == "" (
    echo Usage: %0 { console : start : stop : restart : status : quickstatus : install : update : remove }
    pause
    goto :eof
) else (
    shift
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
echo Starting openEQUELLA Manager
call :update>NUL
NET START %SERVICE_NAME%
goto :eof

:stop
if %INSTALLED%==false goto :End
echo Stopping openEQUELLA Manager
NET STOP %SERVICE_NAME%
goto :eof

:install
echo Installing openEQUELLA Manager as a service
"%_WRAPPER_EXE%" //IS//%SERVICE_NAME% --DisplayName="%DISPLAY_NAME%" --Description="%DISPLAY_NAME%" --ServiceUser="LocalSystem" --Jvm="%JAVA_VM%" --StopTimeout=5 --StartMode=jvm --StopMode=jvm --StartClass=%MAIN_CLASS% --StartParams=start --StopClass=%MAIN_CLASS% --StopParams=stop --Classpath="%CLASS_PATH%" --JvmOptions="%JAVA_ARGS%" --LogPath="%LOG_PATH%" --StdOutput=auto --StdError=auto --StartPath="%WORKING_DIR%." --Startup=%START_TYPE% --LogPrefix="manager" --PidFile="manager.pid" %SERVICE_FLAGS%
COPY "prunmgr.exe" "%SERVICE_NAME%w.exe">NUL
goto :eof

:update
echo Updating the openEQUELLA Manager service
"%_WRAPPER_EXE%" //US//%SERVICE_NAME% --DisplayName="%DISPLAY_NAME%" --Description="%DISPLAY_NAME%" --ServiceUser="LocalSystem" --Jvm="%JAVA_VM%" --StopTimeout=5 --StartMode=jvm --StopMode=jvm --StartClass=%MAIN_CLASS% --StartParams=start --StopClass=%MAIN_CLASS% --StopParams=stop --Classpath="%CLASS_PATH%" --JvmOptions="%JAVA_ARGS%" --LogPath="%LOG_PATH%" --StdOutput=auto --StdError=auto --StartPath="%WORKING_DIR%." --Startup=%START_TYPE% --LogPrefix="manager" --PidFile="manager.pid" %SERVICE_FLAGS%
COPY "prunmgr.exe" "%SERVICE_NAME%w.exe">NUL
goto :eof

:remove
echo Removing the openEQUELLA Manager service
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
echo Restarting openEQUELLA Manager
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
echo Please install the openEQUELLA Manager service using the "install" command

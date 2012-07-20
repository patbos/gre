@REM ----------------------------------------------------------------------------
@REM GRE Start Up script
@REM
@REM Required ENV vars:
@REM JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM GRE_HOME - location of maven2's installed home dir
@REM GRE_BATCH_ECHO - set to 'on' to enable the echoing of the batch commands
@REM GRE_BATCH_PAUSE - set to 'on' to wait for a key stroke before ending
@REM GRE_OPTS - parameters passed to the Java VM when running GRE
@REM     e.g. to debug GRE itself, use
@REM set GRE_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
@REM ----------------------------------------------------------------------------

@REM Begin all REM lines with '@' in case GRE_BATCH_ECHO is 'on'
@echo off
@REM enable echoing my setting GRE_BATCH_ECHO to 'on'
@if "%GRE_BATCH_ECHO%" == "on"  echo %GRE_BATCH_ECHO%

@REM set %HOME% to equivalent of $HOME
if "%HOME%" == "" (set "HOME=%HOMEDRIVE%%HOMEPATH%")

set ERROR_CODE=0

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

@REM ==== START VALIDATION ====
if not "%JAVA_HOME%" == "" goto OkJHome

echo.
echo ERROR: JAVA_HOME not found in your environment.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto chkMHome

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation
echo.
goto error

:chkMHome
if not "%GRE_HOME%"=="" goto valMHome

if "%OS%"=="Windows_NT" SET "GRE_HOME=%~dp0.."
if "%OS%"=="WINNT" SET "GRE_HOME=%~dp0.."
if not "%GRE_HOME%"=="" goto valMHome

echo.
echo ERROR: GRE_HOME not found in your environment.
echo Please set the GRE_HOME variable in your environment to match the
echo location of the GRE installation
echo.
goto error

:valMHome

:stripMHome
if not "_%GRE_HOME:~-1%"=="_\" goto checkMBat
set "GRE_HOME=%GRE_HOME:~0,-1%"
goto stripMHome

:checkMBat
if exist "%GRE_HOME%\bin\mvn.bat" goto init

echo.
echo ERROR: GRE_HOME is set to an invalid directory.
echo GRE_HOME = "%GRE_HOME%"
echo Please set the GRE_HOME variable in your environment to match the
echo location of the GRE installation
echo.
goto error
@REM ==== END VALIDATION ====

:init
@REM Decide how to startup depending on the version of windows

@REM -- Windows NT with Novell Login
if "%OS%"=="WINNT" goto WinNTNovell

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

:WinNTNovell

@REM -- 4NT shell
if "%@eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set GRE_CMD_LINE_ARGS=%*
goto endInit

@REM The 4NT Shell from jp software
:4NTArgs
set GRE_CMD_LINE_ARGS=%$
goto endInit

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of agruments (up to the command line limit, anyway).
set GRE_CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto endInit
set GRE_CMD_LINE_ARGS=%GRE_CMD_LINE_ARGS% %1
shift
goto Win9xApp

@REM Reaching here means variables are defined and arguments have been captured
:endInit
SET GRE_JAVA_EXE="%JAVA_HOME%\bin\java.exe"

@REM Start GRE
:runm2
%GRE_JAVA_EXE% %GRE_OPTS% -jar %GRE_HOME%\gre.jar  "-Dgre.home=%GRE_HOME%" %GRE_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
if "%OS%"=="WINNT" @endlocal
set ERROR_CODE=1

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT
if "%OS%"=="WINNT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set GRE_JAVA_EXE=
set GRE_CMD_LINE_ARGS=
goto postExec

:endNT
@endlocal & set ERROR_CODE=%ERROR_CODE%

:postExec
if exist "%HOME%\GRErc_post.bat" call "%HOME%\GRErc_post.bat"
@REM pause the batch file if GRE_BATCH_PAUSE is set to 'on'
if "%GRE_BATCH_PAUSE%" == "on" pause

if "%GRE_TERMINATE_CMD%" == "on" exit %ERROR_CODE%

cmd /C exit /B %ERROR_CODE%


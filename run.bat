@echo off
setlocal

set "ROOT=%~dp0"

javac -encoding UTF-8 -cp "%ROOT%lib/*" @"%ROOT%compile.list" -d "%ROOT%class"
if errorlevel 1 exit /b %errorlevel%

pushd "%ROOT%class"
java -cp ".;%ROOT%lib/*" Main
set "EXIT_CODE=%errorlevel%"
popd

exit /b %EXIT_CODE%
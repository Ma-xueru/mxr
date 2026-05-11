@echo off
call "%~dp0env.bat"
cd /d "%~dp0apoetryreccsquizintelassist\src\main\resources\manage"
npm.cmd run serve

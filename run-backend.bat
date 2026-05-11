@echo off
call "%~dp0env.bat"
cd /d "%~dp0apoetryreccsquizintelassist"
mvn.cmd spring-boot:run

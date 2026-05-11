@echo off
call "%~dp0env.bat"
cd /d "%~dp0apoetryreccsquizintelassist"
mysql.exe -uroot -proot -e "CREATE DATABASE IF NOT EXISTS r8479 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
mysql.exe -uroot -proot r8479 < boot_apoetryreccsquizintelassist.sql

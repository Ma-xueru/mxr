@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-17"
set "MAVEN_HOME=D:\apache-maven"
set "MYSQL_HOME=C:\Program Files\MySQL\MySQL Server 8.0"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%MYSQL_HOME%\bin;%PATH%"
cd /d "%~dp0apoetryreccsquizintelassist"
mvn.cmd spring-boot:run

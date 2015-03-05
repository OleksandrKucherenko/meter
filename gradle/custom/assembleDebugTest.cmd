@echo off

cls
cd ..\..

gradlew.bat :samples:sample-01:assembleDebug :samples:sample-01:assembleDebugTest --info >build.log 2>build-errors.log
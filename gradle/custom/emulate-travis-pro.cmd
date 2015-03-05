@echo off

cls
cd ..\..

set TRAVIS=true
set TRAVIS_JOB_ID=38691809
set SERVICE_NAME=travis-pro
set COVERALLS_REPO_TOKEN=eMlGHyXPJPqm0sn91H2HwOls3mSIrz15C

::gradlew coveralls --info %1 %2 %3 %4 %5 %6 %7
gradlew coveralls --stacktrace --debug >debug.log 2>debug-errors.log

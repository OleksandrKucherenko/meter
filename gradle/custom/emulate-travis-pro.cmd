@echo off

cls

:: save current directory
pushd .
cd ..\..

set TRAVIS=true
set TRAVIS_JOB_ID=54078341
set SERVICE_NAME=travis-ci
set COVERALLS_REPO_TOKEN=eMlGHyXPJPqm0sn91H2HwOls3mSIrz15C

set CI_BUILD_NUMBER=104
set CI_BUILD_URL=https://travis-ci.org/OleksandrKucherenko/meter/builds/54078340
::set CI_NAME=

::gradlew coveralls --info %1 %2 %3 %4 %5 %6 %7
gradlew :meter:coveralls --stacktrace --debug 2>&1 | tee debug.log

:: recover directory
popd

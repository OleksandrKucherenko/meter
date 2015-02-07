@echo off
:: ArtfulBits Inc. (c) 2005-2015
::
:: Facebook Hash Calculations
:: https://developers.facebook.com/docs/android/getting-started/

SET JDK=%JAVA_HOME%\bin
SET OPENSSL=C:\Utils\OpenSSL-Win64\bin
SET COMPANY=artfulbits
SET outName=%COMPANY%.facebook.hash

:: validate script dependences
if not exist "%JDK%\keytool.exe" (
  goto :error "%JDK%\keytool.exe"
  goto :EOF
)

if not exist "%OPENSSL%\openssl.exe" (
  goto :error "%OPENSSL%\openssl.exe"
  goto :EOF
)

SET KEY=android
SET PASS=android
SET ALIAS=androiddebugkey
SET KEYSTORE=%COMPANY%.debug.keystore

echo https://developers.facebook.com/docs/android/getting-started/ >%outName%
echo https://developers.google.com/appengine/docs/java/endpoints/auth#Creating_OAuth_20_client_IDs >>%outName%
echo. >>%outName%
echo. >>%outName%
echo Debug Key: %KEYSTORE% >>%outName%
"%JDK%\keytool.exe" -exportcert -alias %ALIAS% -storepass %PASS% -keypass %KEY% -keystore %KEYSTORE% | "%OPENSSL%\openssl.exe" sha1 -binary | "%OPENSSL%\openssl.exe" base64 >>%outName%

echo. >>%outName%
echo Debug Key (SHA1): %KEYSTORE% >>%outName%
"%JDK%\keytool.exe" -exportcert -alias %ALIAS% -storepass %PASS% -keypass %KEY% -keystore %KEYSTORE% | "%OPENSSL%\openssl.exe" sha1 >>%outName%

SET KEY=P7EqivolM6qL
SET PASS=TwgV7dmmFM8T
SET ALIAS=artfulbits
SET KEYSTORE=%COMPANY%.keystore

echo. >>%outName%
echo Release Key: %KEYSTORE% >>%outName%
"%JDK%\keytool.exe" -exportcert -alias %ALIAS% -storepass %PASS% -keypass %KEY% -keystore %KEYSTORE% | "%OPENSSL%\openssl.exe" sha1 -binary | "%OPENSSL%\openssl.exe" base64 >>%outName%

echo. >>%outName%
echo Release Key (SHA1): %KEYSTORE% >>%outName%
"%JDK%\keytool.exe" -exportcert -alias %ALIAS% -storepass %PASS% -keypass %KEY% -keystore %KEYSTORE% | "%OPENSSL%\openssl.exe" sha1 >>%outName%

goto :EOF

:: -------------------------------------------------------------------------------------
:error
echo ERROR:
echo Script for running require proper path to JDK.
echo %1 can not be found. Please fix script variables declaration.
echo Please set correctly JDK script variable.
echo Search path is: %1
goto :EOF
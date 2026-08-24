@echo off
CLS

CALL :init_colors

SET ENV_FILE=params_deployment.txt
SET versio=none
SET entorn=none
SET remote_params=
SET jar=none
SET app=none

REM Llegim les variables del fitxer ENV_FILE
For /F "tokens=1* delims==" %%A IN (%ENV_FILE%) DO (
    IF "%%A"=="jar" SET jar=%%B
    IF "%%A"=="app" SET app=%%B
)

IF %app% EQU none GOTO error_app
IF %jar% EQU none GOTO error_jar

SET imatge_jre=ames/jre:%app%
echo.
echo %ESC%[101;93m*** ACTIVACIO DE L'APLICACIO %app% *** %ESC%[0m
echo.
SET /p versio="[97mVersio de l'aplicacio: [0m"
IF %versio% EQU none GOTO error_versio
SET image=ames/app/%app%:%versio%
SET /p entorn="[97mSelecciona el servidor docker [local,test,produccio]: [0m"
IF %entorn% EQU local goto check_image
IF %entorn% EQU test goto test
IF %entorn% EQU produccio goto produccio
GOTO error_entorn

:test
SET remote_params=-H dockertest.ames:2375
GOTO check_image

:produccio
SET remote_params=-H docker.ames:2375
GOTO check_image

REM Comprovem que la imatge de l'aplicació existeixi
:check_image
echo.
echo [93mComprovant imatge... [0m 
DEL activate.txt 2> nul
SET command=docker -H dockertest.ames:2375 image ls -f "reference=%image%" --format "{{.ID}}"
%command% > activate.txt

REM Si la búsqueda no dóna resultats, activate.txt estarà buit
SET size=0
FOR /f %%i in ("activate.txt") DO SET size=%%~zi
IF %size% == 0 GOTO error_not_found

echo IMATGE_APP=%image% > .env
echo IMATGE_JRE=%imatge_jre% >> .env
echo NOM_CONTENIDOR=%app% >> .env
echo SPRING_PROFILE=SPRING_PROFILES_ACTIVE=%entorn% >> .env

REM Creem el contenidor a partir de la imatge existent. Si hi ha algun contenidor creat, l'atura i el substitueix.
REM Com que no li posem el paràmetre --force-recreate ni --build, la imatge no es torna a crear
echo.
echo [93mActivant la versio %versio% de l'aplicacio %app%... [0m 
echo.
SET command=docker-compose %remote_params% -p %app% -f compose_app.yml up -d --remove-orphans
%command%
IF %ERRORLEVEL% NEQ 0 GOTO fi

echo.
echo [92mVersio %versio% de l'aplicacio %app% activada [0m 
echo.
GOTO fi

:error_not_found
echo.
echo [91mError: no s'ha trobat cap imatge amb la referencia %image% [0m 
echo.
GOTO fi

:error_entorn
echo.
echo [91mError: servidor docker incorrecte [local,test,produccio] [0m 
echo.

:error_versio
echo.
echo [91mError: no s'ha introduit cap versio de l'aplicacio [0m 
echo.
GOTO fi

:error_app
echo.
echo [91mError: la variable 'app' no esta definida en el fitxer %ENV_FILE% [0m 
echo.
GOTO fi

:error_jar
echo.
echo [91mError: la variable 'jar' no esta definida en el fitxer %ENV_FILE% [0m 
echo.
GOTO fi

:fi

echo.
SET /p exitkey= "[97mPrem RETURN per acabar... [0m"
echo.

:init_colors
for /F "tokens=1,2 delims=#" %%a in ('"prompt #$H#$E# & echo on & for %%b in (1) do rem"') do (
  set ESC=%%b
  exit /B 0
)
exit /B 0


rem echo IMATGE_APP=ames/app/isocat:2.0 > .env
rem echo IMATGE_JRE=ames/jre:isocat >> .env
rem echo NOM_CONTENIDOR=isocat >> .env

rem SET command=docker-compose -H dockertest.ames:2375 -p isocat -f compose_app.yml up -d --remove-orphans
rem %command%

SET image=ames/app/isocat
SET versio=2.00
echo %image%:%versio%
SET command=docker -H dockertest.ames:2375 image ls -f "reference=%image%":%versio% --format "{{.Repository}}"

set size=0
for /f %%i in ("tmp.txt") do set size=%%~zi
if %size% == 0 echo Empty

rem del tmp.txt
rem %command% > tmp.txt
rem echo bla > tmp.txt
SET /p result=<tmp.txt
if not defined %result% echo Not defined
echo result=%result%


rem echo.
rem SET /p exitkey= "[97mPrem RETURN per acabar... [0m"
rem echo.




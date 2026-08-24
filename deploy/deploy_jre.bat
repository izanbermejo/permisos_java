@echo off
CLS

CALL :init_colors

SET ENV_FILE=params_deployment.txt
SET entorn=none
SET remote_params=
SET jar=none
SET app=none

REM Llegim les variables del fitxer ENV_FILE
For /F "tokens=1* delims==" %%A IN (%ENV_FILE%) DO (
    IF "%%A"=="jar" SET jar=%%B
    IF "%%A"=="app" SET app=%%B
)

SET imatge_jre=ames/jre:%app%
IF %app% EQU none GOTO error_app
IF %jar% EQU none GOTO error_jar

echo.
echo %ESC%[101;93m*** DEPLOY DE LA IMATGE %imatge_jre% *** %ESC%[0m
echo.
SET /p entorn="[97mSelecciona el servidor docker [local,test,produccio]: [0m"
IF %entorn% EQU local GOTO clean_layers
IF %entorn% EQU test GOTO test
IF %entorn% EQU produccio GOTO produccio
GOTO error_entorn

:test
SET remote_params=-H dockertest.ames:2375
GOTO clean_layers

:produccio
SET remote_params=-H webapps.ames:2375
goto clean_layers

REM Eliminació dels directoris de les capes de l'aplicació d'spring boot
:clean_layers
echo.
echo [93mInicialitzant entorn... [0m 
rd /S /Q layers 2> nul

REM Extracció les dependències del fat jar en la carpeta deploy
echo.
echo [93mExtraient les dependencies del fat jar... [0m 
mkdir layers
cd layers
SET command=java -Djarmode=layertools -jar ../../target/%jar%-fat.jar extract 
%command%
cd..
IF %ERRORLEVEL% NEQ 0 GOTO fi

REM Creació del fitxer jdeps_list.txt amb els mòduls de la JVM utilitzats pel codi de l'aplicació i les dependències
echo.
echo [93mObtenint els moduls de la JVM necessaris... [0m 
SET "command=jdeps -R -q --print-module-deps --ignore-missing-deps --multi-release base -cp layers/dependencies/BOOT-INF/lib/*.jar ..\target\%jar%.jar > jdeps_list.txt"
%command%
IF %ERRORLEVEL% NEQ 0 GOTO fi

REM Si jdeps_list.txt està buit donem error
SET size=0
FOR /f %%i in ("jdeps_list.txt") DO SET size=%%~zi
IF %size% == 0 GOTO error_modules

REM Creació de la imatge de la màquina virtual i les dependències
echo.
echo [93mCreant la imatge de la maquina virtual i les dependencies... [0m 
echo.
SET /p modules= < jdeps_list.txt
SET command=docker %remote_params% build -f dockerfile_jre -t %imatge_jre% --build-arg modules=%modules% .
%command%
IF %ERRORLEVEL% NEQ 0 GOTO fi

echo.
echo [93mEliminant imatges sense tag... [0m 
echo.
SET command=docker %remote_params% image prune --force
%command%
IF %ERRORLEVEL% NEQ 0 GOTO fi

echo.
echo [92mImatge %imatge_jre% creada [0m 
echo.
GOTO fi

:error_modules
echo.
echo [91mError: no s'han trobat moduls Java per aquesta aplicacio [0m 
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

:error_entorn
echo.
echo [91mError: servidor docker incorrecte [local,test,produccio] [0m 
echo.

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

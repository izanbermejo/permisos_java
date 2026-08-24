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
echo %ESC%[101;93m*** DEPLOY DE L'APLICACIO %app% *** %ESC%[0m
echo.
echo [93mRecorda executar 'deploy_jre.bat' abans d'aquest script si has canviat les dependencies del projecte [0m
echo.
SET /p versio="[97mVersio de l'aplicacio: [0m"
IF %versio% EQU none GOTO error_versio
SET image=ames/app/%app%:%versio%
SET /p entorn="[97mSelecciona el servidor docker [local,test,produccio]: [0m"
IF %entorn% EQU local goto clean_layers
IF %entorn% EQU test goto test
IF %entorn% EQU produccio goto produccio
GOTO error_entorn

:test
SET remote_params=-H dockertest.ames:2375
GOTO clean_layers

:produccio
SET remote_params=-H webapps.ames:2375
GOTO clean_layers

REM Eliminació dels directoris de les capes de l'aplicació d'spring boot
:clean_layers
echo.
echo [93mInicialitzant entorn... [0m 
rd /S /Q layers 2> nul

REM Extracció les dependències del fat jar en la carpeta layers
echo.
echo [93mExtraient les dependencies del fat jar... [0m 
mkdir layers
cd layers
SET command=java -Djarmode=layertools -jar ../../target/%jar%-fat.jar extract 
%command%
cd..
IF %ERRORLEVEL% NEQ 0 GOTO fi

REM Creació de la imatge de l'aplicació
echo.
echo [93mCreant la imatge %image%... [0m 
echo.
REM Per passar paràmetres al ficher de docker compose, es pot fer amb variables d'entron o a través del fitxer .env
echo IMATGE_APP=%image% > .env
echo IMATGE_JRE=%imatge_jre% >> .env
echo NOM_CONTENIDOR=%app% >> .env
echo SPRING_PROFILE=SPRING_PROFILES_ACTIVE=%entorn% >> .env
REM El paràmetre -p és necessari ja que per defecte docker utilitza el directori actual com a nom de projecte
REM Si es fes un desplegament des d'un altre dir, docker donaria error dient que el contenidor ja està en ús
REM Utilitzem --force-recreate per tornar a crear la imatge encara que existeixi
SET command=docker-compose %remote_params% -p %app% -f compose_app.yml up -d --build --force-recreate --remove-orphans
%command%

echo.
echo [93mEliminant imatges sense tag... [0m 
echo.
SET command=docker %remote_params% image prune --force
%command%
echo.
echo [92mAplicacio %image% arrancada [0m 
echo [93mLa imatge de la versio anterior de l'aplicacio s'ha d'eliminar manualment  [0m
GOTO fi 

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
echo [91mError: la variable 'jar' no esta definida en el fitxer %ENV_FILE%  [0m 
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



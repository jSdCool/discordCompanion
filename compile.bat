@echo off
goto check_Permissions

:check_Permissions
    echo Administrative permissions required. Detecting permissions...

    net session >nul 2>&1
    if %errorLevel% == 0 (
        goto :compile
    ) else (
        goto :noAdmin
    )

:compile 
echo Success: Administrative permissions confirmed.
cd "C:\Users\nisky\Desktop\random things\discordCompanion"
gradlew build
pause
goto :end
:noAdmin
echo Failure: please run file as admin.
:end
pause 
pause
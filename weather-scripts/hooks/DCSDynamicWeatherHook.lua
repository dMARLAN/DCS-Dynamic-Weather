local DCS_DYNAMIC_WEATHER_HOOK_VERSION = "1.1.0"
local DCSDynamicWeather = {}
local DCSDynamicWeatherCallbacks = {}
DCSDynamicWeather.Logger = {}

local THIS_FILE = "DCSDynamicWeatherHook"
local DCS_ROOT = lfs.currentdir()
local DCS_SG = lfs.writedir()
local RESTART_TIME = 3600

local missionLoaded = false
local simulationStartTime = DCS.getRealTime()
local initialPauseState
local waiting = false
local checkRestartTime = 0
local initialPauseStateSet = false
local injectedRestart = false

function DCSDynamicWeatherCallbacks.onMissionLoadEnd()
    local THIS_METHOD = "DCSDynamicWeatherCallbacks.onMissionLoadEnd"
    waiting = false
    initialPauseStateSet = false
    injectedRestart = false

    initialPauseState = DCS.getPause()
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Initial pause state: " .. tostring(initialPauseState))
    if initialPauseState then
        DCS.setPause(false)
        DCSDynamicWeather.Logger.info(THIS_METHOD, "Pause State set to: " .. tostring(false))
    end

    local missionDesc = DCS.getMissionDescription()
    if not string.find(missionDesc, "DCSDW") then
        DCSDynamicWeather.Logger.info(THIS_METHOD, "\"DCSDW\" not found in mission description (situation), skipping.")
        return
    end

    missionLoaded = true
    simulationStartTime = DCS.getRealTime()
    DCSDynamicWeather.injectMissionNameToScriptEnv()
end

function DCSDynamicWeatherCallbacks.onTriggerMessage(message, _, _)
    local THIS_METHOD = "DCSDynamicWeatherCallbacks.onTriggerMessage"

    if not (DCS.isServer() and DCS.isMultiplayer()) then
        DCSDynamicWeather.Logger.warning(THIS_METHOD, "Only a multiplayer server can load a mission.")
        return
    end

    if (string.match(message, "%[DCSDynamicWeather%.Mission%]:%sLoad%sMission:%s")) then
        local mission = string.match(message, "%[DCSDynamicWeather%.Mission%]:%sLoad Mission:%s(.*)")
        DCSDynamicWeather.Logger.info(THIS_METHOD, "Loading Mission: " .. DCS_SG .. "Missions\\" .. mission)
        net.load_mission(DCS_SG .. "Missions\\" .. mission)
    end

    if (string.match(message, "%[DCSDynamicWeather%.Mission%]:%sEncode")) then
        local code = [[a_do_script("DCSDynamicWeather.MISSION_TABLE = \"]] .. DCS.getCurrentMission() .. [[\"")]]
        DCSDynamicWeather.injectCodeStringToScriptEnv(code)
    end
end

function DCSDynamicWeatherCallbacks.onSimulationFrame()
    local THIS_METHOD = "DCSDynamicWeatherCallbacks.onSimulationFrame"
    if DCS.getRealTime() > simulationStartTime + 5 and not initialPauseStateSet then
        DCS.setPause(initialPauseState)
        DCSDynamicWeather.Logger.info(THIS_METHOD, "Pause State set to: " .. tostring(initialPauseState))
        initialPauseStateSet = true
    end

    if math.floor(DCS.getRealTime()) % 60 == 0 then
        DCSDynamicWeather.checkCondForRestart()
    end
end

function DCSDynamicWeather.checkCondForRestart()
    local THIS_METHOD = "DCSDynamicWeather.waitForRestart"
    if not missionLoaded then
        return
    end

    if not waiting then
        waiting = true
        checkRestartTime = DCS.getRealTime() + 15
    else
        if (DCS.getRealTime() > checkRestartTime) then
            if (DCS.getRealTime() > simulationStartTime + DCSDynamicWeather.getRestartTimeInSeconds()) and not injectedRestart then
                DCSDynamicWeather.Logger.info(THIS_METHOD, "Restarting mission.")
                DCS.setPause(false)
                DCSDynamicWeather.Logger.info(THIS_METHOD, "Pause State set to: " .. tostring(false))
                DCSDynamicWeather.restart()
                injectedRestart = true
            else
                local timeUntilRestart = DCSDynamicWeather.getRestartTimeInSeconds() + simulationStartTime - DCS.getRealTime()
                DCSDynamicWeather.Logger.info(THIS_METHOD, "Waiting for restart. (" .. timeUntilRestart .. " seconds)")
            end
            waiting = false
        end
    end
end

function DCSDynamicWeather.injectMissionNameToScriptEnv()
    local missionName = DCS.getMissionName()

    local code = [[a_do_script("DCSDynamicWeather.MISSION_NAME = \"]] .. missionName .. [[\"")]]
    DCSDynamicWeather.injectCodeStringToScriptEnv(code)
end

function DCSDynamicWeather.restart()
    local code = [[a_do_script("DCSDynamicWeather.Restart.now()")]]
    DCSDynamicWeather.injectCodeStringToScriptEnv(code)
end

function DCSDynamicWeather.injectCodeStringToScriptEnv(code)
    local THIS_METHOD = "DCSDynamicWeatherHook.injectCode"

    local successful, err = pcall(net.dostring_in, "mission", code)
    if not successful then
        DCSDynamicWeather.Logger.error(THIS_METHOD, "Failed to inject: \"" .. code .. "\" with error: " .. err)
    else
        DCSDynamicWeather.Logger.info(THIS_METHOD, "Injected: \"" .. code .. "\"")
    end
end

function DCSDynamicWeather.getRestartTimeInSeconds()
    return RESTART_TIME -- TODO: Make this configurable
end

function DCSDynamicWeather.fileExists(file)
    local f = io.open(file, "rb")
    if f then
        io.close(f)
    end
    return f ~= nil
end

function DCSDynamicWeather.desanitizeMissionScripting()
    local THIS_METHOD = "DCSDynamicWeatherHook.desanitizeMissionScripting"
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Desanitizing Mission Scripting...")
    local missionScriptingFileName = "MissionScripting.lua"
    local missionScriptingFilePath = DCS_ROOT .. "Scripts\\" .. missionScriptingFileName
    local uncommentedLineFound = false
    local newMissionScriptingContent = ""

    if not (DCSDynamicWeather.fileExists(missionScriptingFilePath)) then
        DCSDynamicWeather.Logger.error(THIS_METHOD, "File: " .. missionScriptingFilePath .. " does not exist.")
        return
    end

    local readMissionScriptingFile = io.open(missionScriptingFilePath, "rb")
    for line in io.lines(readMissionScriptingFile) do
        if (string.match(line, "^(%-%-)")) or (string.match(line, "dofile")) or (string.match(line, "^[%s%c]*$")) then
            newMissionScriptingContent = newMissionScriptingContent .. line .. "\n"
        else
            uncommentedLineFound = true
            newMissionScriptingContent = newMissionScriptingContent .. "--" .. line .. " -- Commented by " .. THIS_FILE .. "\n"
        end
    end
    io.close(readMissionScriptingFile)

    if (uncommentedLineFound) then
        local writeMissionScriptingFile = io.open(missionScriptingFilePath, "wb")
        io.write(writeMissionScriptingFile, newMissionScriptingContent)
        io.flush(writeMissionScriptingFile)
        io.close(writeMissionScriptingFile)
        DCSDynamicWeather.Logger.info(THIS_METHOD, "Desanitized Mission Scripting.")
    else
        DCSDynamicWeather.Logger.info(THIS_METHOD, "Mission Scripting is already desanitized.")
    end
end

function DCSDynamicWeather.Logger.info(logSource, message)
    DCSDynamicWeather.Logger.printLog(logSource, message, "INFO    ")
end

function DCSDynamicWeather.Logger.warning(logSource, message)
    DCSDynamicWeather.Logger.printLog(logSource, message, "WARNING ")
end

function DCSDynamicWeather.Logger.error(logSource, message)
    DCSDynamicWeather.Logger.printLog(logSource, message, "ERROR   ")
end

function DCSDynamicWeather.Logger.printLog(logSource, message, level)
    local time = os.date("%Y-%m-%d %H:%M:%S ")
    local logFile = io.open(DCS_SG .. "Logs\\" .. THIS_FILE .. ".log", "a")
    io.write(logFile, time .. level .. "[" .. logSource .. "]: " .. message .. "\n")
    io.flush(logFile)
    io.close(logFile)
end

local function main()
    DCSDynamicWeather.Logger.info(THIS_FILE, "Loading DCS Dynamic Weather Version: " .. DCS_DYNAMIC_WEATHER_HOOK_VERSION .. "...")
    DCSDynamicWeather.Logger.info(THIS_FILE, "DCS_ROOT: " .. DCS_ROOT)
    DCSDynamicWeather.Logger.info(THIS_FILE, "DCS_SG: " .. DCS_SG)

    DCSDynamicWeather.desanitizeMissionScripting()
    DCS.setUserCallbacks(DCSDynamicWeatherCallbacks)

    DCSDynamicWeather.Logger.info(THIS_FILE, "Loaded.")
end
main()

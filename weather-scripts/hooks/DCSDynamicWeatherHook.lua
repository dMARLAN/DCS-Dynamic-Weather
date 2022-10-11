local DCS_DYNAMIC_WEATHER_HOOK_VERSION = "1.0.0"
local DCSDynamicWeatherHook = {}
local DCSDynamicWeatherCallbacks = {}
DCSDynamicWeatherHook.Logger = {}

local THIS_FILE = "DCSDynamicWeatherHook"
local DCS_ROOT = lfs.currentdir()
local DCS_SG = lfs.writedir()

function DCSDynamicWeatherCallbacks.onMissionLoadEnd()
    local THIS_METHOD = "DCSDynamicWeatherCallbacks.onMissionLoadEnd"
    local missionName = DCS.getMissionName()
    local missionDesc = DCS.getMissionDescription()

    if not string.find(missionDesc, "DCSDW") then
        DCSDynamicWeatherHook.Logger.info(THIS_METHOD, "\"DCSDW\" not found in mission description (situation), skipping mission name injection.")
        return
    end

    local code = [[a_do_script("DCSDynamicWeather.MISSION_NAME = \"]] .. missionName .. [[\"")]]

    local successful, err = pcall(net.dostring_in, "mission", code)
    if not successful then
        DCSDynamicWeatherHook.Logger.error(THIS_METHOD, "Failed to inject: \"" .. code .. "\" with error: " .. err)
    else
        DCSDynamicWeatherHook.Logger.info(THIS_METHOD, "Injected: \"" .. code .. "\"")
    end
end

function DCSDynamicWeatherCallbacks.onTriggerMessage(message, _, _)
    local THIS_METHOD = "DCSDynamicWeatherCallbacks.onTriggerMessage"

    if not (DCS.isServer() and DCS.isMultiplayer()) then
        DCSDynamicWeatherHook.Logger.warning(THIS_METHOD, "Only a multiplayer server can load a mission.")
        return
    end

    if (string.match(message, "%[DCSDynamicWeather%.Mission%]:%sLoad%sMission:%s")) then
        local mission = string.match(message, "%[DCSDynamicWeather%.Mission%]:%sLoad Mission:%s(.*)")
        DCSDynamicWeatherHook.Logger.info(THIS_METHOD, "Loading Mission: " .. DCS_SG .. "Missions\\" .. mission)
        net.load_mission(DCS_SG .. "Missions\\" .. mission)
    end
end

function DCSDynamicWeatherHook.fileExists(file)
    local f = io.open(file, "rb")
    if f then
        io.close(f)
    end
    return f ~= nil
end

function DCSDynamicWeatherHook.desanitizeMissionScripting()
    local THIS_METHOD = "DCSDynamicWeatherHook.desanitizeMissionScripting"
    DCSDynamicWeatherHook.Logger.info(THIS_METHOD, "Desanitizing Mission Scripting...")
    local missionScriptingFileName = "MissionScripting.lua"
    local missionScriptingFilePath = DCS_ROOT .. "Scripts\\" .. missionScriptingFileName
    local uncommentedLineFound = false
    local newMissionScriptingContent = ""

    if not (DCSDynamicWeatherHook.fileExists(missionScriptingFilePath)) then
        DCSDynamicWeatherHook.Logger.error(THIS_METHOD, "File: " .. missionScriptingFilePath .. " does not exist.")
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
        DCSDynamicWeatherHook.Logger.info(THIS_METHOD, "Desanitized Mission Scripting.")
    else
        DCSDynamicWeatherHook.Logger.info(THIS_METHOD, "Mission Scripting is already desanitized.")
    end
end

function DCSDynamicWeatherHook.Logger.info(logSource, message)
    DCSDynamicWeatherHook.Logger.printLog(logSource, message, "INFO    ")
end

function DCSDynamicWeatherHook.Logger.warning(logSource, message)
    DCSDynamicWeatherHook.Logger.printLog(logSource, message, "WARNING ")
end

function DCSDynamicWeatherHook.Logger.error(logSource, message)
    DCSDynamicWeatherHook.Logger.printLog(logSource, message, "ERROR   ")
end

function DCSDynamicWeatherHook.Logger.printLog(logSource, message, level)
    local time = os.date("%Y-%m-%d %H:%M:%S ")
    local logFile = io.open(DCS_SG .. "Logs\\" .. THIS_FILE .. ".log", "a")
    io.write(logFile, time .. level .. "[" .. logSource .. "]: " .. message .. "\n")
    io.flush(logFile)
    io.close(logFile)
end

local function main()
    DCSDynamicWeatherHook.Logger.info(THIS_FILE, "Loading DCS Dynamic Weather Version: " .. DCS_DYNAMIC_WEATHER_HOOK_VERSION .. "...")
    DCSDynamicWeatherHook.Logger.info(THIS_FILE, "DCS_ROOT: " .. DCS_ROOT)
    DCSDynamicWeatherHook.Logger.info(THIS_FILE, "DCS_SG: " .. DCS_SG)

    DCSDynamicWeatherHook.desanitizeMissionScripting()
    DCS.setUserCallbacks(DCSDynamicWeatherCallbacks)

    DCSDynamicWeatherHook.Logger.info(THIS_FILE, "Loaded.")
end
main()

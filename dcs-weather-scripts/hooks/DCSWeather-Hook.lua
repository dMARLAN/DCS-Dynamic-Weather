local DCSWeatherHook = {}
local DCSWeatherCallbacks = {}
DCSWeatherHook.Logger = {}

local THIS_FILE = "DCSWeather-Hook"
local DCS_ROOT = lfs.currentdir()
local DCS_SG = lfs.writedir()

function DCSWeatherCallbacks.onMissionLoadEnd()
    local THIS_METHOD = "DCSWeatherCallbacks.onMissionLoadEnd"
    local missionName = DCS.getMissionName()
    local code = [[a_do_script("DCSWeather.MISSION_NAME = \"]] .. missionName .. [[\"")]]
    local input = "a_do_script(\"" .. code .. "\")"
    net.dostring_in("mission", input)
    DCSWeatherHook.Logger.Info(THIS_METHOD, "Injected DCSWeather.MISSION_NAME = " .. missionName)
end

function DCSWeatherCallbacks.onTriggerMessage(message, _, _)
    local THIS_METHOD = "DCSWeatherCallbacks.onTriggerMessage"
    if (string.match(message, "%[DCSWeather%.Mission%]:%sLoad%sMission:%s")) then
        local mission = string.match(message, "%[DCSWeather%.Mission%]:%sLoad Mission:%s(.*)")
        DCSWeatherHook.Logger.Info(THIS_METHOD, "Loading Mission: " .. DCS_SG .. "Missions\\" .. mission)
        net.load_mission(DCS_SG .. "Missions\\" .. mission)
    end
end

function DCSWeatherHook.fileExists(file)
    local f = io.open(file, "rb")
    if f then
        io.close(f)
    end
    return f ~= nil
end

function DCSWeatherHook.desanitizeMissionScripting()
    local THIS_METHOD = "DCSWeatherHook.desanitizeMissionScripting"
    DCSWeatherHook.Logger.Info(THIS_METHOD, "Desanitizing Mission Scripting...")
    local missionScriptingFileName = "MissionScripting.lua"
    local missionScriptingFilePath = DCS_ROOT .. "Scripts\\" .. missionScriptingFileName
    local uncommentedLineFound = false
    local newMissionScriptingContent = ""

    if not (DCSWeatherHook.fileExists(missionScriptingFilePath)) then
        DCSWeatherHook.Logger.Error(THIS_METHOD, "File: " .. missionScriptingFilePath .. " does not exist.")
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
        DCSWeatherHook.Logger.Info(THIS_METHOD, "Desanitized Mission Scripting.")
    else
        DCSWeatherHook.Logger.Info(THIS_METHOD, "Mission Scripting is already desanitized.")
    end
end

function DCSWeatherHook.Logger.Info(logSource, message)
    DCSWeatherHook.Logger.printLog(logSource, message, "INFO    ")
end

function DCSWeatherHook.Logger.Warning(logSource, message)
    DCSWeatherHook.Logger.printLog(logSource, message, "WARNING ")
end

function DCSWeatherHook.Logger.Error(logSource, message)
    DCSWeatherHook.Logger.printLog(logSource, message, "ERROR   ")
end

function DCSWeatherHook.Logger.printLog(logSource, message, level)
    local time = os.date("%Y-%m-%d %H:%M:%S ")
    local logFile = io.open(DCS_SG .. "Logs\\" .. THIS_FILE .. ".log", "a")
    io.write(logFile, time .. level .. "[" .. logSource .. "]: " .. message .. "\n")
    io.flush(logFile)
    io.close(logFile)
end

local function main()
    DCSWeatherHook.Logger.Info(THIS_FILE, "Loading...")
    DCSWeatherHook.Logger.Info(THIS_FILE, "DCS_ROOT: " .. DCS_ROOT)
    DCSWeatherHook.Logger.Info(THIS_FILE, "DCS_SG: " .. DCS_SG)

    DCSWeatherHook.desanitizeMissionScripting()
    DCS.setUserCallbacks(DCSWeatherCallbacks)

    DCSWeatherHook.Logger.Info(THIS_FILE, "Loaded.")
end
main()

local DCSDynamicWeatherHook = {}
local DCSDynamicWeatherCallbacks = {}
DCSDynamicWeatherHook.Logger = {}

local THIS_FILE = "DCS-Dynamic-Weather-Hook"
local DCS_ROOT = lfs.currentdir()
local DCS_SG = lfs.writedir()

function DCSDynamicWeatherCallbacks.onMissionLoadEnd()
    local THIS_METHOD = "DCSDynamicWeatherCallbacks.onMissionLoadEnd"
    local missionName = DCS.getMissionName()

    local code = [[a_do_script("DCSDynamicWeather.MISSION_NAME = \"]] .. missionName .. [[\"")]]
    local succesful, err = pcall(net.dostring_in("mission", code))
    if not succesful then
        DCSDynamicWeatherHook.Logger.error(THIS_METHOD, "DCSDynamicWeather.MISSION_NAME failed to inject.")
    else
        DCSDynamicWeatherHook.Logger.info(THIS_METHOD, "Injected DCSDynamicWeather.MISSION_NAME = " .. missionName)
    end
end

function DCSDynamicWeatherCallbacks.onTriggerMessage(message, _, _)
    local THIS_METHOD = "DCSDynamicWeatherCallbacks.onTriggerMessage"

    if not DCS.isServer() then
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
    local THIS_METHOD = "DCSWeatherHook.desanitizeMissionScripting"
    DCSWeatherHook.Logger.info(THIS_METHOD, "Desanitizing Mission Scripting...")
    local missionScriptingFileName = "MissionScripting.lua"
    local missionScriptingFilePath = DCS_ROOT .. "Scripts\\" .. missionScriptingFileName
    local uncommentedLineFound = false
    local newMissionScriptingContent = ""

    if not (DCSWeatherHook.fileExists(missionScriptingFilePath)) then
        DCSWeatherHook.Logger.error(THIS_METHOD, "File: " .. missionScriptingFilePath .. " does not exist.")
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
        DCSWeatherHook.Logger.info(THIS_METHOD, "Desanitized Mission Scripting.")
    else
        DCSWeatherHook.Logger.info(THIS_METHOD, "Mission Scripting is already desanitized.")
    end
end

function DCSWeatherHook.Logger.info(logSource, message)
    DCSWeatherHook.Logger.printLog(logSource, message, "INFO    ")
end

function DCSWeatherHook.Logger.warning(logSource, message)
    DCSWeatherHook.Logger.printLog(logSource, message, "WARNING ")
end

function DCSWeatherHook.Logger.error(logSource, message)
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
    DCSWeatherHook.Logger.info(THIS_FILE, "Loading...")
    DCSWeatherHook.Logger.info(THIS_FILE, "DCS_ROOT: " .. DCS_ROOT)
    DCSWeatherHook.Logger.info(THIS_FILE, "DCS_SG: " .. DCS_SG)

    DCSWeatherHook.desanitizeMissionScripting()
    DCS.setUserCallbacks(DCSWeatherCallbacks)

    DCSWeatherHook.Logger.info(THIS_FILE, "Loaded.")
end
main()

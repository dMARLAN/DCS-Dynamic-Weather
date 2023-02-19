DCSDynamicWeather.Mission = {}
package.path = package.path .. ";.\\Scripts\\?.lua;"

local THIS_FILE = DCSDynamicWeather.MODULE_NAME .. ".Mission"
local invertMissionIdentifier, getNextMissionName, loadMission,
fileExists, copyFileWithNewIdentifier, invertIdentifier,
decodeJSONAndSerializeMissionFile, encodeJSONMissionFile, executeWeatherUpdate

local missionEncoded = false

function decodeJSONAndSerializeMissionFile()
    local THIS_METHOD = "DCSDynamicWeather.updateMissionToLua"

    local Factory = require("Factory")
    local JSON = loadfile(lfs.currentdir() .. "Scripts\\" .. "JSON.lua")()
    local Serializer = require("Serializer")

    local readMissionFile = io.open(DCSDynamicWeather.SCRIPTS_PATH .. "\\mission", "r")
    local missionFileContents = io.read(readMissionFile, "*a")
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Read mission file.")

    local missionFileJson = JSON:decode(missionFileContents)
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Decoded mission file.")

    local withoutMissionFileJson = missionFileJson.mission
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Removed mission from table.")
    io.close(readMissionFile)

    local fout = io.open(DCSDynamicWeather.SCRIPTS_PATH .. "\\mission", "w")
    local serializer = Serializer.new()
    serializer:construct(fout)
    serializer:serialize_simple2("mission", withoutMissionFileJson)
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Serialized and wrote mission file.")
end

function encodeJSONMissionFile()
    trigger.action.outText("[DCSDynamicWeather.Mission]: Encode", 10)
    local startWaitTime = timer.getTime()
    while DCSDynamicWeather.MISSION_TABLE == nil do
        if timer.getTime() % 1 == 0 then
            trigger.action.outText("[DCSDynamicWeather.Mission]: Waiting for mission table to be populated...", 10)
        end
        if timer.getTime() > startWaitTime + 10 then
            DCSDynamicWeather.Logger.error(THIS_FILE, "Timed out waiting for mission table to be populated.")
            return
        end
    end
    local missionFileJson = JSON:encode(DCSDynamicWeather.MISSION_TABLE)
    local fout = io.open(DCSDynamicWeather.SCRIPTS_PATH .. "\\mission", "w")
    fout:write(missionFileJson)
    fout:flush()
    fout:close()
    DCSDynamicWeather.Logger.info(THIS_FILE, "Encoded and wrote mission file.")
end

function executeWeatherUpdate()
    if not missionEncoded then
        DCSDynamicWeather.JAR.execute("weather-update")
    end
end

function DCSDynamicWeather.Mission.loadNextMission(weatherType)
    local cvOpsEnabled = DCSDynamicWeather.JSON.getValue("cyclic_ops", DCSDynamicWeather.CONFIG_PATH) == "true"
    if not weatherType and cvOpsEnabled then
        weatherType = "cvops"
    elseif not weatherType then
        weatherType = "real"
    end
    local nextMissionName = getNextMissionName()
    trigger.action.outText("[DCSDynamicWeather]: Preparing to load next mission...", 10)
    DCSDynamicWeather.JSON.setValue("current_game_time", timer.getAbsTime(), DCSDynamicWeather.DTO_PATH)
    DCSDynamicWeather.JSON.setValue("weather_type", weatherType, DCSDynamicWeather.DTO_PATH)
    DCSDynamicWeather.JSON.setValue("mission", nextMissionName .. ".miz", DCSDynamicWeather.DTO_PATH)

    trigger.action.outText("[DCSDynamicWeather.Mission]: Encode", 10)
    DCSDynamicWeather.JSON.setValue("update_phase", "edit", DCSDynamicWeather.DTO_PATH)
    timer.scheduleFunction(executeWeatherUpdate, nil, timer.getTime() + 2)

    decodeJSONAndSerializeMissionFile()
    DCSDynamicWeather.JSON.setValue("update_phase", "update", DCSDynamicWeather.DTO_PATH)
    DCSDynamicWeather.JAR.execute("weather-update")
end

function loadMission(mission)
    local THIS_METHOD = THIS_FILE .. ".loadMission()"
    -- DCSDynamicWeatherHook.lua catches this text
    trigger.action.outText("[DCSDynamicWeather.Mission]: Load Mission: " .. DCSDynamicWeather.MISSION_FOLDER .. "\\" .. mission .. ".miz", 10, false)
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Loading Mission: " .. DCSDynamicWeather.MISSION_FOLDER .. "\\" .. mission .. ".miz")
end

function getNextMissionName()
    local THIS_METHOD = THIS_FILE .. ".getNextMissionName"
    local missionName = DCSDynamicWeather.MISSION_NAME

    if not missionName then
        DCSDynamicWeather.Logger.error(THIS_METHOD, "missionName is nil")
        return
    else
        DCSDynamicWeather.Logger.info(THIS_METHOD, "missionName is " .. missionName)
    end

    local missionNameLast2Chars = string.sub(missionName, #missionName - 1)

    if (string.match(missionNameLast2Chars, "_A") or string.match(missionNameLast2Chars, "_B")) then
        local missionNameWithInvertedIdentifier = invertMissionIdentifier(missionName)
        if not fileExists(DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. missionNameWithInvertedIdentifier .. ".miz") then
            copyFileWithNewIdentifier(invertIdentifier(string.sub(missionName, #missionName)))
        end
        DCSDynamicWeather.Logger.info(THIS_METHOD, "Next Mission: " .. missionNameWithInvertedIdentifier)
        return missionNameWithInvertedIdentifier
    else
        DCSDynamicWeather.Logger.warning(THIS_METHOD, "Can't match identifier on current mission.")
        if not fileExists(DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. missionName .. "_A.miz") then
            DCSDynamicWeather.Logger.warning(THIS_METHOD, "_A identifier not found, generating new file.")
            copyFileWithNewIdentifier("A")
        end
        if not fileExists(DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. missionName .. "_B.miz") then
            DCSDynamicWeather.Logger.warning(THIS_METHOD, "_B identifier not found, generating new file.")
            copyFileWithNewIdentifier("B")
        end
        return missionName .. "_A"
    end
end

function copyFileWithNewIdentifier(newIdentifier)
    local originalMissionName = DCSDynamicWeather.MISSION_NAME
    local newMissionName
    local missionNameLast2Chars = string.sub(originalMissionName, #originalMissionName - 1)

    if (string.match(missionNameLast2Chars, "_A") or string.match(missionNameLast2Chars, "_B")) then
        newMissionName = string.sub(originalMissionName,0, #originalMissionName - 2)
    else
        newMissionName = originalMissionName
    end

    local originalFilePath = DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. originalMissionName .. ".miz"
    local newFilePath = DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. newMissionName .. "_" .. newIdentifier .. ".miz"
    os.execute("copy \"" .. originalFilePath .. "\" \"" .. newFilePath .. "\"")
end

function invertMissionIdentifier(missionName)
    local missionNameWithoutLast2Chars = string.sub(missionName, 0, #missionName - 1)
    local identifier = string.sub(missionName, #missionName)
    return missionNameWithoutLast2Chars .. invertIdentifier(identifier)
end

function invertIdentifier(identifier)
    if (identifier == "A") then
        return "B"
    else
        return "A"
    end
end

function fileExists(file)
    return DCSDynamicWeather.File.exists(file)
end

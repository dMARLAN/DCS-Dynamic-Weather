DCSDynamicWeather.Mission = {}

local THIS_FILE = DCSDynamicWeather.MODULE_NAME .. ".Mission"
local invertMissionIdentifier, getNextMissionName, loadMission, fileExists, copyFileWithIdentifier, invertIdentifier

function DCSDynamicWeather.Mission.loadNextMission(weatherType)
    local THIS_METHOD = THIS_FILE .. ".loadNextMission()"

    weatherType = weatherType or "real"
    DCSDynamicWeather.JSON.setValue("weather_type", weatherType, DCSDynamicWeather.DTO)
    DCSDynamicWeather.JSON.setValue("mission", getNextMissionName() .. ".miz", DCSDynamicWeather.DTO)
    DCSDynamicWeather.JAR.execute("weather-update")

    loadMission(getNextMissionName())
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
    local missionNameLast2Chars = string.sub(missionName, #missionName - 1)

    if (string.match(missionNameLast2Chars, "_A") or string.match(missionNameLast2Chars, "_B")) then
        local missionNameWithInvertedIdentifier = invertMissionIdentifier(missionName)
        if not fileExists(DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. missionNameWithInvertedIdentifier .. ".miz") then
            copyFileWithIdentifier(invertIdentifier(string.sub(missionName, #missionName)))
        end
        DCSDynamicWeather.Logger.info(THIS_METHOD, "Next Mission: " .. missionNameWithInvertedIdentifier)
        return missionNameWithInvertedIdentifier
    else
        DCSDynamicWeather.Logger.warning(THIS_METHOD, "Can't match identifier on current mission.")
        if not fileExists(DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. missionName .. "_A.miz") then
            DCSDynamicWeather.Logger.warning(THIS_METHOD, "_A identifier not found, generating new file.")
            copyFileWithIdentifier("A")
        end
        if not fileExists(DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. missionName .. "_B.miz") then
            DCSDynamicWeather.Logger.warning(THIS_METHOD, "_B identifier not found, generating new file.")
            copyFileWithIdentifier("B")
        end
        return missionName .. "_A"
    end
end

function copyFileWithIdentifier(identifier)
    local missionNamePath = DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. DCSDynamicWeather.MISSION_NAME
    local originalFilePath = missionNamePath .. ".miz"
    local newFilePath = missionNamePath .. "_" .. identifier .. ".miz"
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

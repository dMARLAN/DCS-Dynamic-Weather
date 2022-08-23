DCSWeather.Mission = {}

local THIS_FILE = DCSWeather.MODULE_NAME .. ".Mission"
local invertMissionIdentifier
local MISSION_NAME = DCSWeather.MISSION_NAME

function DCSWeather.Mission.loadNextMission(mission)
    local THIS_METHOD = THIS_FILE .. ".loadNextMission()"
    DCSWeather.Logger.Info(THIS_METHOD, "Loading Mission: " .. DCSWeather.MISSION_FOLDER .. "\\" .. mission .. ".miz")
    trigger.action.outText("[DCSWeather.Mission]: Load Mission: " .. DCSWeather.MISSION_FOLDER .. "\\" .. mission .. ".miz", 10, false)
end

function DCSWeather.Mission.getNextMissionName()
    local THIS_METHOD = THIS_FILE .. ".getNextMissionName"
    if (string.match(string.sub(MISSION_NAME, #MISSION_NAME - 1), "_%a")) then
        local invertedMissionIdentifier = invertMissionIdentifier(MISSION_NAME)
        if (invertedMissionIdentifier ~= 0) then
            local nextMissionName = string.sub(MISSION_NAME, 0, #MISSION_NAME - 1) .. invertedMissionIdentifier
            DCSWeather.Logger.Info(THIS_METHOD, "Next Mission: " .. nextMissionName)
            return nextMissionName
        end
    end
    DCSWeather.Logger.Info(THIS_METHOD, "Could not match mission identifier.")
    return 0
end

function invertMissionIdentifier(missionName)
    if (string.sub(missionName, #missionName) == "A") then
        return "B"
    end
    if (string.sub(missionName, #missionName) == "B") then
        return "A"
    end
    return 0
end
local getNumPlayerUnits, restartMission
local reminderCount = 0

local THIS_FILE = DCSWeather.MODULE_NAME .. ".Restart"

function getNumPlayerUnits()
    local numPlayerUnits = 0
    for _, side in pairs(coalition.side) do
        for _, _ in pairs(coalition.getPlayers(side)) do
            numPlayerUnits = numPlayerUnits + 1
        end
    end
    return numPlayerUnits
end

function restartMission(maxOverTime)
    local reminderIntervalInMins = 60
    local repeatInterval = 300
    local numPlayerUnits = getNumPlayerUnits()
    if (numPlayerUnits == 0 or timer.getTime() >= maxOverTime) then
        env.info("[DCS-Weather-Restart.lua]: Restarting mission.")
        local nextMissionToLoad = DCSWeather.Mission.getNextMissionName()
        if (nextMissionToLoad ~= 0) then
            DCSWeather.JSON.setValue("mission", nextMissionToLoad .. ".miz", DCSWeather.DAO)
            DCSWeather.JAR.execute("weather-update")
            DCSWeather.Mission.loadNextMission(nextMissionToLoad)
        end
    else
        DCSWeather.Logger.Info(THIS_FILE, "Waiting for " .. numPlayerUnits .. " player units to leave the mission.")
        local timeRemaining = maxOverTime - timer.getTime()
        local timeRemainingInMinutes = timeRemaining / 60
        local flooredTimeRemainingInMinutes = math.floor(timeRemainingInMinutes)
        local message = "[DCSWeather.Restart]: Server will restart in " .. flooredTimeRemainingInMinutes .. " minutes."
        if (reminderCount == reminderIntervalInMins) then
            trigger.action.outText(message, 10)
            reminderCount = 0
        elseif (flooredTimeRemainingInMinutes <= 5) then
            trigger.action.outText(message, 10)
        else
            reminderCount = reminderCount + 1
        end
        return timer.getTime() + repeatInterval -- Schedules next attempt to restart in repeatInterval seconds
    end
end

local function main()
    local restartTimeInHours = 1
    local maximumOverTimeInHours = 8
    local restartTimeInSeconds = restartTimeInHours * 3600
    local maximumOverTimeInSeconds = maximumOverTimeInHours * 3600
    local relativeRestartTimeInSeconds = timer.getTime() + restartTimeInSeconds
    local relativeMaximumOverTimeInSeconds = timer.getTime() + maximumOverTimeInSeconds
    timer.scheduleFunction(restartMission, relativeMaximumOverTimeInSeconds, relativeRestartTimeInSeconds)
end
main()

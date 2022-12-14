DCSDynamicWeather.Restart = {}

local getNumPlayerUnits, restartMission, getMaximumOvertimeInSeconds
local reminderCount = 0

local THIS_FILE = DCSDynamicWeather.MODULE_NAME .. ".Restart"

function getNumPlayerUnits()
    local numPlayerUnits = 0
    for _, side in pairs(coalition.side) do
        for _, _ in pairs(coalition.getPlayers(side)) do
            numPlayerUnits = numPlayerUnits + 1
        end
    end
    return numPlayerUnits
end

function restartMission()
    local maxOverTime = getMaximumOvertimeInSeconds()
    local reminderIntervalInMins = 60
    local repeatInterval = 300
    local numPlayerUnits = getNumPlayerUnits()
    if (numPlayerUnits == 0 or timer.getTime() >= maxOverTime) then
        DCSDynamicWeather.Logger.info(THIS_FILE, "Restarting mission.")
        DCSDynamicWeather.Mission.loadNextMission()
    else
        DCSDynamicWeather.Logger.info(THIS_FILE, "Waiting for " .. numPlayerUnits .. " player units to leave the mission.")
        local timeRemaining = maxOverTime - timer.getTime()
        local timeRemainingInMinutes = timeRemaining / 60
        local flooredTimeRemainingInMinutes = math.floor(timeRemainingInMinutes)
        local message = "[DCSDynamicWeather.Restart]: Server will restart in " .. flooredTimeRemainingInMinutes .. " minutes."
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

function DCSDynamicWeather.Restart.now()
    local THIS_METHOD = THIS_FILE .. ".attemptRestartNow()"
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Attempting to restart mission now.")
    timer.scheduleFunction(restartMission, nil, timer.getTime() + 1)
end

function getMaximumOvertimeInSeconds()
    local maximumOverTimeInHours = DCSDynamicWeather.JSON.getValue("maximumHoursForEachRestart", DCSDynamicWeather.CONFIG_PATH)
    return maximumOverTimeInHours * 3600
end

local function main()
    local restartTimeInHours = DCSDynamicWeather.JSON.getValue("hoursForEachRestart", DCSDynamicWeather.CONFIG_PATH)
    local restartTimeInSeconds = restartTimeInHours * 3600
    timer.scheduleFunction(restartMission, nil, timer.getTime() + restartTimeInSeconds)
end
main()

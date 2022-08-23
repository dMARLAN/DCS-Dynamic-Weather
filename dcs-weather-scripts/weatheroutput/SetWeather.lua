local setWeather, createMenus
local THIS_FILE = DCSWeather.MODULE_NAME .. ".SetWeather"

function setWeather(weatherType)
    DCSWeather.JSON.setValue("weather_type", weatherType, DCSWeather.DTO)

    local nextMissionToLoad = DCSWeather.Mission.getNextMissionName()
    if (nextMissionToLoad ~= 0) then
        trigger.action.outText("[DCSWeather.SetWeather]: Loading: " .. weatherType .. "\\" .. nextMissionToLoad .. "...", 10)
        DCSWeather.JSON.setValue("mission", nextMissionToLoad .. ".miz", DCSWeather.DTO)
        DCSWeather.JAR.execute("weather-update")
        DCSWeather.JSON.setValue("weather_type", "real", DCSWeather.DTO)
        DCSWeather.Mission.loadNextMission(nextMissionToLoad)
    end
end

function createMenus()
    local setWeatherMenu = missionCommands.addSubMenu("Set Weather")

    local clearDayConfirm = missionCommands.addSubMenu("Clear Day", setWeatherMenu)
    missionCommands.addCommand("Confirm", clearDayConfirm, setWeather, "clearDay")

    local clearNightConfirm = missionCommands.addSubMenu("Clear Night", setWeatherMenu)
    missionCommands.addCommand("Confirm", clearNightConfirm, setWeather, "clearNight")
    DCSWeather.Logger.Info(THIS_FILE, "Created Set Weather Menus.")
end

local function main()
    createMenus()
end
main()

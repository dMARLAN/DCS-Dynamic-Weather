local menuHandler = {}
local setWeather, createGroupSpecificMenus, createAllGroupsMenus
local THIS_FILE = DCSWeather.MODULE_NAME .. ".SetWeather"
local setWeatherMenu

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

function menuHandler:onEvent(event)
    local ADMIN_GROUP_NAME = "ADMIN/DEBUG"
    if event.id == world.event.S_EVENT_BIRTH and event.initiator:getPlayerName() ~= nil then
        if Group.getName(event.initiator:getGroup()) == ADMIN_GROUP_NAME then
            local adminGroup = event.initiator:getGroup()
            local adminGroupID = Group.getID(adminGroup)
            createGroupSpecificMenus(adminGroupID, ADMIN_GROUP_NAME)
        end
    end
end

function createGroupSpecificMenus(adminGroupID, ADMIN_GROUP_NAME)
    if setWeatherMenu ~= nil then
        return
    end
    setWeatherMenu = missionCommands.addSubMenuForGroup(adminGroupID, "Set Weather")

    local clearDayConfirm = missionCommands.addSubMenuForGroup(adminGroupID, "Clear Day", setWeatherMenu)
    missionCommands.addCommandForGroup(adminGroupID, "Confirm", clearDayConfirm, setWeather, "clearDay")

    local clearNightConfirm = missionCommands.addSubMenuForGroup(adminGroupID, "Clear Night", setWeatherMenu)
    missionCommands.addCommandForGroup(adminGroupID, "Confirm", clearNightConfirm, setWeather, "clearNight")
    DCSWeather.Logger.Info(THIS_FILE, "Created Set Weather Menus for " .. ADMIN_GROUP_NAME)
end

function createAllGroupsMenus()
    setWeatherMenu = missionCommands.addSubMenu("Set Weather")

    local clearDayConfirm = missionCommands.addSubMenu("Clear Day", setWeatherMenu)
    missionCommands.addCommand("Confirm", clearDayConfirm, setWeather, "clearDay")

    local clearNightConfirm = missionCommands.addSubMenu("Clear Night", setWeatherMenu)
    missionCommands.addCommand("Confirm", clearNightConfirm, setWeather, "clearNight")
    DCSWeather.Logger.Info(THIS_FILE, "Created Set Weather Menus for all groups.")
end

local function main()
    if true then -- TODO: check config.json when developed
        world.addEventHandler(menuHandler)
    else
        createAllGroupsMenus()
    end
end
main()

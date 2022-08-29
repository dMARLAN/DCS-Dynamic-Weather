local menuHandler = {}
local setWeather, createGroupSpecificMenus, createAllGroupsMenus
local THIS_FILE = DCSDynamicWeather.MODULE_NAME .. ".SetWeather"
local setWeatherMenu

function setWeather(weatherType)
    DCSDynamicWeather.Mission.loadNextMission(weatherType)
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

    local clearDayConfirm = missionCommands.addSubMenuForGroup(adminGroupID, "Clear: 1200L", setWeatherMenu)
    missionCommands.addCommandForGroup(adminGroupID, "Confirm", clearDayConfirm, setWeather, "clearDay")

    local clearNightConfirm = missionCommands.addSubMenuForGroup(adminGroupID, "Clear: 0000L)", setWeatherMenu)
    missionCommands.addCommandForGroup(adminGroupID, "Confirm", clearNightConfirm, setWeather, "clearNight")

    local real0400Confirm = missionCommands.addSubMenuForGroup(adminGroupID, "Real: 0400L", setWeatherMenu)
    missionCommands.addCommandForGroup(adminGroupID, "Confirm", real0400Confirm, setWeather, "real0400")

    local real0600Confirm = missionCommands.addSubMenuForGroup(adminGroupID, "Real: 0600L", setWeatherMenu)
    missionCommands.addCommandForGroup(adminGroupID, "Confirm", real0600Confirm, setWeather, "real0600")

    local real1200Confirm = missionCommands.addSubMenuForGroup(adminGroupID, "Real: 1200L", setWeatherMenu)
    missionCommands.addCommandForGroup(adminGroupID, "Confirm", real1200Confirm, setWeather, "real1200")

    local real1800Confirm = missionCommands.addSubMenuForGroup(adminGroupID, "Real: 1800L", setWeatherMenu)
    missionCommands.addCommandForGroup(adminGroupID, "Confirm", real1800Confirm, setWeather, "real1800")

    local real2200Confirm = missionCommands.addSubMenuForGroup(adminGroupID, "Real: 2200L", setWeatherMenu)
    missionCommands.addCommandForGroup(adminGroupID, "Confirm", real2200Confirm, setWeather, "real2200")

    local real0000Confirm = missionCommands.addSubMenuForGroup(adminGroupID, "Real: 0000L", setWeatherMenu)
    missionCommands.addCommandForGroup(adminGroupID, "Confirm", real0000Confirm, setWeather, "real0000")

    DCSDynamicWeather.Logger.info(THIS_FILE, "Created Set Weather Menus for " .. ADMIN_GROUP_NAME)
end

function createAllGroupsMenus()
    setWeatherMenu = missionCommands.addSubMenu("Set Weather")

    local clearDayConfirm = missionCommands.addSubMenu("Clear Day", setWeatherMenu)
    missionCommands.addCommand("Confirm", clearDayConfirm, setWeather, "clearDay")

    local clearNightConfirm = missionCommands.addSubMenu("Clear Night", setWeatherMenu)
    missionCommands.addCommand("Confirm", clearNightConfirm, setWeather, "clearNight")
    DCSDynamicWeather.Logger.info(THIS_FILE, "Created Set Weather Menus for all groups.")
end

local function main()
    if true then -- TODO: check config.json when developed
        world.addEventHandler(menuHandler)
    else
        createAllGroupsMenus()
    end
end
main()

DCSDynamicWeather.Mission = {}
local IS_MISSION_EDITED = false

local THIS_FILE = DCSDynamicWeather.MODULE_NAME .. ".Mission"
local invertMissionIdentifier, getNextMissionName, loadMission, fileExists, copyFileWithNewIdentifier, invertIdentifier, executeWeatherUpdate, executeMissionEdit, executeMissionUpdate

function executeMissionEdit()
    trigger.action.outText("[DCSDynamicWeather.Mission]: Edit", 10, false)
    DCSDynamicWeather.JSON.setValue("update_phase", "edit", DCSDynamicWeather.DTO_PATH)
    timer.scheduleFunction(executeWeatherUpdate, nil, timer.getTime() + 3)
end

function executeMissionUpdate()
    eatMyAss()
    DCSDynamicWeather.JSON.setValue("update_phase", "update", DCSDynamicWeather.DTO_PATH)
    timer.scheduleFunction(executeWeatherUpdate, nil, timer.getTime() + 3)
end

function DCSDynamicWeather.removeMissionIdentifier(mission)
    local missionNameLast2Chars = string.sub(mission, #mission - 1)
    if (string.match(missionNameLast2Chars, "_A") or string.match(missionNameLast2Chars, "_B")) then
        return string.sub(mission, 1, #mission - 2)
    end
    return mission
end

function DCSDynamicWeather.Mission.writeData(fcn, fcnVars, fdir)
    if lfs and io then
        local f = io.open(fdir, 'w')
        f:write(fcn(unpack(fcnVars, 1, table.maxn(fcnVars))))
        f:close()
    end
end

function DCSDynamicWeather.Mission.basicSerialize(var)
    if var == nil then
        return "\"\""
    else
        if ((type(var) == 'number') or
                (type(var) == 'boolean') or
                (type(var) == 'function') or
                (type(var) == 'table') or
                (type(var) == 'userdata') ) then
            return tostring(var)
        elseif type(var) == 'string' then
            var = string.format('%q', var)
            return var
        end
    end
end

function DCSDynamicWeather.Mission.serialize(name, value, level)
    --Based on ED's serialize_simple2
    local function basicSerialize(o)
        if type(o) == "number" then
            return tostring(o)
        elseif type(o) == "boolean" then
            return tostring(o)
        else -- assume it is a string
            return basicSerialize(o)
        end
    end

    local function serializeToTbl(name, value, level)
        local var_str_tbl = {}
        if level == nil then
            level = ""
        end
        if level ~= "" then
            level = level..""
        end
        table.insert(var_str_tbl, level .. name .. " = ")

        if type(value) == "number" or type(value) == "string" or type(value) == "boolean" then
            table.insert(var_str_tbl, basicSerialize(value) ..	",\n")
        elseif type(value) == "table" then
            table.insert(var_str_tbl, "\n"..level.."{\n")

            for k,v in pairs(value) do -- serialize its fields
                local key
                if type(k) == "number" then
                    key = string.format("[%s]", k)
                else
                    key = string.format("[%q]", k)
                end
                table.insert(var_str_tbl, DCSDynamicWeather.Mission.serialize(key, v, level.."	"))
            end
            if level == "" then
                table.insert(var_str_tbl, level.."} -- end of "..name.."\n")
            else
                table.insert(var_str_tbl, level.."}, -- end of "..name.."\n")
            end
        end
        return var_str_tbl
    end
    local t_str = serializeToTbl(name, value, level)
    return table.concat(t_str)
end

function eatMyAss()
    local THIS_METHOD = "DCSDynamicWeather.updateMissionToLua"
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Updating mission file to Lua...")
    local readMissionFile = io.open(DCSDynamicWeather.SCRIPTS_PATH .. "\\mission", "r")
    local missionFileContents = io.read(readMissionFile, "*a")
    local missionFileJson = dkjson.decode(missionFileContents)
    io.close(readMissionFile)
    DCSDynamicWeather.Mission.writeData(DCSDynamicWeather.Mission.serialize,
            {'mission', missionFileJson},
            DCSDynamicWeather.SCRIPTS_PATH .. "\\mission"
    )
end

function executeWeatherUpdate()
    DCSDynamicWeather.JAR.execute("weather-update")
    if not IS_MISSION_EDITED then
        IS_MISSION_EDITED = true
        executeMissionUpdate()
    else
        loadMission(getNextMissionName())
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

    executeMissionEdit()
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

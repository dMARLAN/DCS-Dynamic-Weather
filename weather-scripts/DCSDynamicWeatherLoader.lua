require "lfs"

DCSDynamicWeather.MODULE_NAME = "DCSDynamicWeather-Scripts"
DCSDynamicWeather.SCRIPTS_PATH = lfs.writedir() .. "Missions\\" .. DCSDynamicWeather.MISSION_FOLDER
DCSDynamicWeather.DTO_PATH = "data\\dto.json"
DCSDynamicWeather.CONFIG_PATH = "config.json"

local UTILITIES = "utilities"
local WEATHER_OUTPUT = "weatheroutput"
local THIS_FILE = "DCSDynamicWeatherLoader"

local function loadLua(folder, fileName)
    dofile(DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. folder .. "\\" .. fileName .. ".lua")
    DCSDynamicWeather.Logger.info(THIS_FILE, "Loaded: " .. folder .. "\\" .. fileName .. ".lua")
end

loadLua(UTILITIES, "Logger") -- Must be first
loadLua(UTILITIES, "File")
loadLua(UTILITIES, "JAR")
loadLua(UTILITIES, "JSON")
loadLua(UTILITIES, "Mission")
loadLua(UTILITIES, "Restart")

loadLua(WEATHER_OUTPUT, "BuildMetar")
loadLua(WEATHER_OUTPUT, "SetWeather")

DCSDynamicWeather.Logger.info(THIS_FILE, "DCSDynamicWeather.SCRIPTS_PATH: " .. DCSDynamicWeather.SCRIPTS_PATH)
DCSDynamicWeather.Logger.info(THIS_FILE, "DCSDynamicWeather.DATA_FILE: " .. DCSDynamicWeather.DTO_PATH)
DCSDynamicWeather.Logger.info(THIS_FILE, "DCSDynamicWeather.MODULE_NAME: " .. DCSDynamicWeather.MODULE_NAME)
DCSDynamicWeather.Logger.info(THIS_FILE, "Loaded.")

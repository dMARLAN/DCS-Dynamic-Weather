require "lfs"

DCSDynamicWeather.MODULE_NAME = "DCSDynamicWeather"
DCSDynamicWeather.SCRIPTS_PATH = lfs.writedir() .. "Missions\\" .. DCSDynamicWeather.MISSION_FOLDER
DCSDynamicWeather.DTO = "dto.json" -- TODO Get from config.json

local LIBRARIES = "libraries"
local UTILITIES = "utilities"
local MODEL = "model"
local WEATHER_OUTPUT = "weatheroutput"
local THIS_FILE = "DCSDynamicWeatherLoader"

local function loadAllLua(folder)
    local dir = DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. folder
    for file in lfs.dir(dir) do
        if string.find(file, ".lua$") then
            dofile(dir .. "\\" .. file)
            DCSDynamicWeather.Logger.info(THIS_FILE, "Loaded: " .. folder .. "\\" .. file)
        end
    end
end

local function loadLua(folder, fileName)
    dofile(DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. folder .. "\\" .. fileName .. ".lua")
    DCSDynamicWeather.Logger.info(THIS_FILE, "Loaded: " .. folder .. "\\" .. fileName .. ".lua")
end

loadLua(UTILITIES, "Logger")
loadLua(UTILITIES, "JAR")
loadLua(UTILITIES, "JSON")
loadLua(UTILITIES, "Mission")

loadAllLua(LIBRARIES)
loadAllLua(MODEL)

loadLua(WEATHER_OUTPUT, "BuildMetar")
loadLua(WEATHER_OUTPUT, "SetWeather")
loadLua(WEATHER_OUTPUT, "Restart")

DCSDynamicWeather.Logger.info(THIS_FILE, "DCSDynamicWeather.SCRIPTS_PATH: " .. DCSWeather.SCRIPTS_PATH)
DCSWeather.Logger.info(THIS_FILE, "DCSWeather.DATA_FILE: " .. DCSWeather.DTO)
DCSWeather.Logger.info(THIS_FILE, "DCSWeather.MODULE_NAME: " .. DCSWeather.MODULE_NAME)
DCSWeather.Logger.info(THIS_FILE, "Loaded.")

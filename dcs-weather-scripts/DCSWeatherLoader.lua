require "lfs"

DCSWeather.MODULE_NAME = "DCSWeather"
DCSWeather.SCRIPTS_PATH = lfs.writedir() .. "Missions\\" .. DCSWeather.MISSION_FOLDER
DCSWeather.DAO = "dao.json" -- TODO Get from config.json

local LIBRARIES = "libraries"
local UTILITIES = "utilities"
local MODEL = "model"
local WEATHER_OUTPUT = "weatheroutput"
local THIS_FILE = "DCSWeatherLoader"

local function loadAllLua(folder)
    local dir = DCSWeather.SCRIPTS_PATH .. "\\" .. folder
    for file in lfs.dir(dir) do
        if string.find(file, ".lua$") then
            dofile(dir .. "\\" .. file)
            DCSWeather.Logger.Info(THIS_FILE, "Loaded: " .. folder .. "\\" .. file)
        end
    end
end

local function loadLua(folder, fileName)
    dofile(DCSWeather.SCRIPTS_PATH .. "\\" .. folder .. "\\" .. fileName .. ".lua")
    DCSWeather.Logger.Info(THIS_FILE, "Loaded: " .. folder .. "\\" .. fileName .. ".lua")
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

DCSWeather.Logger.Info(THIS_FILE, "DCSWeather.SCRIPTS_PATH: " .. DCSWeather.SCRIPTS_PATH)
DCSWeather.Logger.Info(THIS_FILE, "DCSWeather.DATA_FILE: " .. DCSWeather.DAO)
DCSWeather.Logger.Info(THIS_FILE, "DCSWeather.MODULE_NAME: " .. DCSWeather.MODULE_NAME)
DCSWeather.Logger.Info(THIS_FILE, "Loaded.")

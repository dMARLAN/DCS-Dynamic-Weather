DCSWeather.JSON = {}

local THIS_FILE = DCSWeather.MODULE_NAME .. ".JSON"
local fileExists

function DCSWeather.JSON.setValue(key, value, fileName)
    local THIS_METHOD = THIS_FILE .. ".setValue"
    local filePath = DCSWeather.SCRIPTS_PATH .. "\\" .. fileName
    local dataFilePath = DCSWeather.SCRIPTS_PATH .. "\\" .. DCSWeather.DTO

    if not (fileExists(filePath) and fileExists(dataFilePath)) then
        if not fileExists(filePath) then
            DCSWeather.Logger.Error(THIS_METHOD, "File: \"" .. filePath .. "\" does not exist.")
        else
            DCSWeather.Logger.Error(THIS_METHOD, "File: \"" .. dataFilePath .. "\" does not exist.")
        end
        return
    end

    local readFile = io.open(filePath, "rb")
    local fileContents = io.read(readFile, "*all")
    io.close(readFile)

    local checkComma = string.match(fileContents, "(\"" .. key .. "[%w\": ,]+)")
    if string.sub(checkComma, #checkComma) == "," then
        fileContents = string.gsub(fileContents, "(\"" .. key .. "[%w\": ,]+)", "\"" .. key .. "\": \"" .. value .. "\",")
    else
        fileContents = string.gsub(fileContents, "(\"" .. key .. "[%w\": ,]+)", "\"" .. key .. "\": \"" .. value .. "\"")
    end
    local writeFile = io.open(dataFilePath, "w")
    io.write(writeFile, fileContents)
    io.flush(writeFile)
    io.close(writeFile)
    DCSWeather.Logger.Info(THIS_METHOD, "Key: \"" .. key .. "\" Set to: \"" .. value .. "\" in File: \"" .. fileName .. "\"")
end

function DCSWeather.JSON.getValue(key, fileName)
    local THIS_METHOD = THIS_FILE .. ".getValue"
    local filePath = DCSWeather.SCRIPTS_PATH .. "\\" .. fileName

    if not fileExists(filePath) then
        DCSWeather.Logger.Error(THIS_METHOD, "File: \"" .. filePath .. "\" does not exist.")
        return
    end

    local readFile = io.open(filePath, "rb")
    local fileContents = io.read(readFile, "*all")
    io.close(readFile)

    local value = string.match(fileContents, key .. "\":%s+\"(%w*)")
    DCSWeather.Logger.Info(THIS_METHOD, "Key: \"" .. key .. "\" Value: \"" .. value .. "\" in File: \"" .. fileName .. "\" retrieved.")
    return value
end

function fileExists(file)
    local f = io.open(file, "rb")
    if f then
        io.close(f)
    end
    return f ~= nil
end
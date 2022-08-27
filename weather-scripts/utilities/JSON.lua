DCSDynamicWeather.JSON = {}

local THIS_FILE = DCSDynamicWeather.MODULE_NAME .. ".JSON"
local fileExists

function DCSDynamicWeather.JSON.setValue(key, value, fileName)
    local THIS_METHOD = THIS_FILE .. ".setValue"
    local filePath = DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. fileName
    local dataFilePath = DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. DCSDynamicWeather.DTO

    if not fileExists(filePath) then
        DCSDynamicWeather.Logger.error(THIS_METHOD, "File: \"" .. filePath .. "\" can not be read.")
        return
    end

    local readFile = io.open(filePath, "rb")
    local fileContents = io.read(readFile, "*all")
    io.close(readFile)

    local checkComma = string.match(fileContents, "(\"" .. key .. "\"[%w%_%-./\": ,]+)")
    if string.sub(checkComma, #checkComma) == "," then
        fileContents = string.gsub(fileContents, "(\"" .. key .. "\"[%w%_%-./\": ,]+)", "\"" .. key .. "\": \"" .. value .. "\",")
    else
        fileContents = string.gsub(fileContents, "(\"" .. key .. "\"[%w%_%-./\": ,]+)", "\"" .. key .. "\": \"" .. value .. "\"")
    end
    local writeFile = io.open(dataFilePath, "w")
    io.write(writeFile, fileContents)
    io.flush(writeFile)
    io.close(writeFile)
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Key: \"" .. key .. "\" Set to: \"" .. value .. "\" in File: \"" .. fileName .. "\"")
end

function DCSDynamicWeather.JSON.getValue(key, fileName)
    local THIS_METHOD = THIS_FILE .. ".getValue"
    local filePath = DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. fileName

    if not fileExists(filePath) then
        DCSDynamicWeather.Logger.error(THIS_METHOD, "File: \"" .. filePath .. "\" can not be read.")
        return
    end

    local readFile = io.open(filePath, "rb")
    local fileContents = io.read(readFile, "*all")
    io.close(readFile)

    local value = string.match(fileContents, key .. "\":%s+\"(%w*)")
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Key: \"" .. key .. "\" Value: \"" .. value .. "\" in File: \"" .. fileName .. "\" retrieved.")
    return value
end

function fileExists(file)
    return DCSDynamicWeather.File.exists(file)
end

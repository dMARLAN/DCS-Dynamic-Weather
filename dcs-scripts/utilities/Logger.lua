DCSWeather.Logger = {}

local printLog, checkReplaceNil

-- @param fileName string
-- @param message string
function DCSWeather.Logger.Info(logSource, message)
    message = checkReplaceNil(message)
    printLog(logSource, message, "INFO    ")
end

-- @param fileName string
-- @param message string
function DCSWeather.Logger.Warning(logSource, message)
    message = checkReplaceNil(message)
    printLog(logSource, message, "WARNING ")
end

-- @param fileName string
-- @param message string
function DCSWeather.Logger.Error(logSource, message)
    message = checkReplaceNil(message)
    printLog(logSource, message, "ERROR   ")
end

function checkReplaceNil(message)
    if message == nil then
        return "nil"
    else
        return message
    end
end

function printLog(logSource, message, level)
    local time = os.date("%Y-%m-%d %H:%M:%S ")
    local logFile = io.open(DCSWeather.SCRIPTS_PATH .. "\\logs\\" .. DCSWeather.MODULE_NAME .. ".log", "a")
    io.write(logFile, time .. level .. "[" .. logSource .. "]: " .. message .. "\n")
    io.flush(logFile)
    io.close(logFile)
end
DCSDynamicWeather.Logger = {}

local printLog, checkReplaceNil

-- @param fileName string
-- @param message string
function DCSDynamicWeather.Logger.info(logSource, message)
    message = checkReplaceNil(message)
    printLog(logSource, message, "INFO    ")
end

-- @param fileName string
-- @param message string
function DCSDynamicWeather.Logger.warning(logSource, message)
    message = checkReplaceNil(message)
    printLog(logSource, message, "WARNING ")
end

-- @param fileName string
-- @param message string
function DCSDynamicWeather.Logger.error(logSource, message)
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
    local fullTimeStamp = os.date("%Y-%m-%d %H:%M:%S ")
    local ymdTimeStamp = os.date("%Y%m%d")
    local logFile = io.open(DCSDynamicWeather.SCRIPTS_PATH .. "\\logs\\" .. DCSDynamicWeather.MODULE_NAME .. ymdTimeStamp .. ".log", "a")
    io.write(logFile, fullTimeStamp .. level .. "[" .. logSource .. "]: " .. message .. "\n")
    io.flush(logFile)
    io.close(logFile)
end
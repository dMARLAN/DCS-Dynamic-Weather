DCSDynamicWeather.JAR = {}

local THIS_FILE = DCSDynamicWeather.MODULE_NAME .. ".JAR"

function DCSDynamicWeather.JAR.execute(jarName)
    local THIS_METHOD = THIS_FILE .. ".executeJar"
    local jar = jarName .. ".jar"
    local jarPath = DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. jar

    if not DCSDynamicWeather.File.exists(jarPath) then
        DCSDynamicWeather.Logger.warning(jarName .. ".jar couldn't be read.")
        return
    end

    DCSDynamicWeather.Logger.info(THIS_METHOD, "Executing JAR: " .. jar)
    os.execute("java -jar \"" .. jarPath .. "\" \"" .. DCSDynamicWeather.SCRIPTS_PATH .. "\"")
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Execution Complete.")
end

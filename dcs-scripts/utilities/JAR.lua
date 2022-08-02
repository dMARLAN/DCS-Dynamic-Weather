DCSWeather.JAR = {}

local THIS_FILE = DCSWeather.MODULE_NAME .. ".JAR"

function DCSWeather.JAR.execute(jarName)
    local THIS_METHOD = THIS_FILE .. ".executeJar"
    local jar = jarName .. ".jar"
    DCSWeather.Logger.Info(THIS_METHOD, "Executing JAR: " .. jar)
    os.execute("java -jar \"" .. DCSWeather.SCRIPTS_PATH .. "\\" .. jar .. "\" \"" .. DCSWeather.SCRIPTS_PATH .. "\"")
    DCSWeather.Logger.Info(THIS_METHOD, "Execution Complete.")
end

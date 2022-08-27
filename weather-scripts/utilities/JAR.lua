DCSDynamicWeather.JAR = {}

local THIS_FILE = DCSDynamicWeather.MODULE_NAME .. ".JAR"
local findJar

function DCSDynamicWeather.JAR.execute(jarName)
    local THIS_METHOD = THIS_FILE .. ".executeJar"
    local jar = findJar(jarName)
    local jarPath = DCSDynamicWeather.SCRIPTS_PATH .. "\\" .. jar

    if not jar then
        DCSDynamicWeather.Logger.warning(jarName .. ".jar wasn't found.")
        return
    end

    DCSDynamicWeather.Logger.info(THIS_METHOD, "Executing JAR: " .. jar)
    os.execute("java -jar \"" .. jarPath .. "\" \"" .. DCSDynamicWeather.SCRIPTS_PATH .. "\"")
    DCSDynamicWeather.Logger.info(THIS_METHOD, "Execution Complete.")
end

function findJar(jarName)
    local dir = DCSDynamicWeather.SCRIPTS_PATH
    for file in lfs.dir(dir) do
        if lfs.attributes(dir .. "\\" .. file,"mode") == "file" then
            if string.find(file, jarName, _, true) then
                DCSDynamicWeather.Logger.info(THIS_FILE, "Found JAR: " .. file)
                return file
            end
        end
    end
end

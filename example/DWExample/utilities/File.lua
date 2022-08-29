DCSDynamicWeather.File = {}

function DCSDynamicWeather.File.exists(file)
    local f = io.open(file, "rb")
    if f then
        io.close(f)
    end
    return f ~= nil
end

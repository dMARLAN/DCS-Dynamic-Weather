local BuildMetar = {}

local THIS_FILE = DCSWeather.MODULE_NAME .. ".BuildMetar"
local STATION_REFERENCE_ZONE_NAME = "StationReference"

local STANDARD_PRESSURE_PASCAL = 101325
local PASCALS_TO_INHG = 0.0295299830714
local METERS_TO_FEET = 3.28084
local STD_PRESSURE_MILLIBAR = 1013.25
local METERS_TO_KNOTS = 1.94384
local FEET_TO_STATUTORY_MILES = 0.000189394
local PASCAL_TO_MILLIBAR = 0.01
local ZERO_CELCIUS_IN_KELVIN = 273.15

function BuildMetar.getNearestAirbasePoint()
    local THIS_METHOD = THIS_FILE .. ".getNearestAirbasePoint"

    local stationReference = trigger.misc.getZone(STATION_REFERENCE_ZONE_NAME)

    local searchVolume = {
        id = world.VolumeType.SPHERE,
        params = {
            point = stationReference.point,
            radius = stationReference.radius
        }
    }

    local airbase
    local located = function(located)
        DCSWeather.Logger.Info(THIS_METHOD, "Nearest airbase point found.")
        airbase = located
    end
    world.searchObjects(Object.Category.BASE, searchVolume, located)
    if airbase then
        return airbase:getPoint()
    end

    DCSWeather.Logger.Info(THIS_METHOD, "No nearest airbase point found, setting reference to StationReference.")
    return stationReference.point
end

function BuildMetar.getWind(referencePoint)
    local THIS_METHOD = THIS_FILE .. ".getWind"

    local localReferencePoint = {}
    localReferencePoint.x = referencePoint.x
    localReferencePoint.y = (land.getHeight({ x = localReferencePoint.x, z = localReferencePoint.z })) -- Wind will return 0 within 10m of ground
    localReferencePoint.z = referencePoint.z
    DCSWeather.Logger.Info(THIS_METHOD, "Wind Reference point: { x = " .. localReferencePoint.x .. ", y = " .. localReferencePoint.y .. ", z = " .. localReferencePoint.z .. " }")

    local windVec = atmosphere.getWind(localReferencePoint)
    local windSpeed = math.sqrt((windVec.z) ^ 2 + (windVec.x) ^ 2)
    windSpeed = windSpeed * METERS_TO_KNOTS -- Meters to Knots


    local windDirection = math.deg(math.atan2(windVec.z, windVec.x))


    -- Clamp wind direction between 0 and 360
    if windDirection < 0 then
        windDirection = windDirection + 360
    end
    if windDirection > 180 then
        windDirection = windDirection - 180
    else
        windDirection = windDirection + 180
    end

    windSpeed = math.floor(windSpeed + 0.5)
    windDirection = math.floor(windDirection + 0.5)
    DCSWeather.Logger.Info(THIS_METHOD, "Wind Speed: " .. windSpeed)
    DCSWeather.Logger.Info(THIS_METHOD, "Wind Direction: " .. windDirection)

    -- Add leading zeroes
    local windDirectionLeadingZeroes
    if windSpeed == 0 then
        windDirectionLeadingZeroes = "000"
    elseif windDirection < 10 then
        windDirectionLeadingZeroes = "00" .. windDirection
    elseif windDirection < 100 then
        windDirectionLeadingZeroes = "0" .. windDirection
    else
        windDirectionLeadingZeroes = windDirection
    end

    local windSpeedLeadingZeroes
    if windSpeed < 10 then
        windSpeedLeadingZeroes = "0" .. windSpeed
    else
        windSpeedLeadingZeroes = windSpeed
    end

    return windDirectionLeadingZeroes .. windSpeedLeadingZeroes .. "KT"
end

function BuildMetar.getDayAndTimeZulu()
    local THIS_METHOD = THIS_FILE .. ".getDayAndTime24UTC"

    local theatre = env.mission.theatre
    DCSWeather.Logger.Info(THIS_METHOD, "Theatre: " .. theatre)

    local time = timer.getAbsTime()
    local day = env.mission.date.Day
    local hours = math.floor(time / 3600)
    local minutes = (time / 60) - (hours * 60)
    DCSWeather.Logger.Info(THIS_METHOD, "Local Time: Day: " .. day .. " Hour: " .. hours .. " Minute: " .. minutes)

    local timeChangeToZulu
    local timeChangeToZuluTbl = {}
    timeChangeToZuluTbl["Caucasus"] = -4
    timeChangeToZuluTbl["PersianGulf"] = -4
    timeChangeToZuluTbl["Nevada"] = 7
    timeChangeToZuluTbl["MarianaIslands"] = 2
    timeChangeToZuluTbl["Syria"] = -3
    timeChangeToZuluTbl["SouthAtlantic"] = -3

    if timeChangeToZuluTbl[theatre] then
        timeChangeToZulu = timeChangeToZuluTbl[theatre]
    else
        DCSWeather.Logger.Warning(THIS_METHOD, "Theatre not detected, no time conversion set.")
        timeChangeToZulu = 0
    end
    DCSWeather.Logger.Info(THIS_METHOD, "Zulu Time: Day: " .. day .. " Hour: " .. hours .. " Minute: " .. minutes)

    hours = math.abs(hours + timeChangeToZulu)
    if hours >= 24 then
        hours = hours % 24
        day = day + 1
    end

    if hours < 10 then
        hours = "0" .. hours
    end
    if minutes < 10 then
        minutes = "0" .. minutes
    end

    return os.date("%d") .. hours .. minutes .. "Z"
end

function BuildMetar.getVisibility()
    local weather = env.mission.weather
    local visibility = env.mission.weather.visibility.distance

    if weather.enable_fog == true then
        local fog = weather.fog
        local fogVisibilityFt = fog.visibility * METERS_TO_FEET
        if fogVisibilityFt < visibility then
            visibility = fogVisibilityFt
        end
    end

    local visibilitySM = visibility * FEET_TO_STATUTORY_MILES
    if visibilitySM < 0.25 then
        return "1/4SM"
    elseif visibilitySM < 0.50 then
        return "1/2SM"
    elseif visibilitySM < 1 then
        return "3/4SM"
    elseif visibilitySM < 10 then
        return math.floor(visibilitySM + 0.5) .. "SM"
    else
        return "10SM"
    end
end

function BuildMetar.getWeatherMods() -- TODO: TS = Thunderstorm, DS = Dust Storm, -RA/RA/+RA
    local weatherMods = ""
    local weather = env.mission.weather

    if weather.enable_fog == true then
        local fog = weather.fog
        local fogVisibilityFt = fog.visibility * METERS_TO_FEET

        if fogVisibilityFt < 3300 then
            if fogVisibilityFt < 1300 then
                weatherMods = "+"
            end
            weatherMods = weatherMods .. "FG"
        else
            weatherMods = "BR"
        end
    end

    return weatherMods
end

function BuildMetar.getCloudCover()
    local cloudsPreset = env.mission.weather.clouds.preset
    local cloudsPresetTbl = {}

    cloudsPresetTbl["Preset1"] = "FEW070"
    cloudsPresetTbl["Preset2"] = "FEW080 SCT230"
    cloudsPresetTbl["Preset3"] = "SCT080 FEW210"
    cloudsPresetTbl["Preset4"] = "SCT080 FEW240"
    cloudsPresetTbl["Preset5"] = "SCT080 FEW240"
    cloudsPresetTbl["Preset6"] = "SCT080 FEW400"
    cloudsPresetTbl["Preset7"] = "BKN075 SCT210 SCT400"
    cloudsPresetTbl["Preset8"] = "SCT180 FEW360 FEW400"
    cloudsPresetTbl["Preset9"] = "BKN075 SCT200 FEW410"
    cloudsPresetTbl["Preset10"] = "SCT180 FEW360 FEW400"
    cloudsPresetTbl["Preset11"] = "BKN180 BKN320 FEW410"
    cloudsPresetTbl["Preset12"] = "BKN120 SCT220 FEW410"
    cloudsPresetTbl["Preset13"] = "BKN120 BKN260 FEW410"
    cloudsPresetTbl["Preset14"] = "BKN070 FEW410"
    cloudsPresetTbl["Preset15"] = "BKN140 BKN240 FEW400"
    cloudsPresetTbl["Preset16"] = "BKN140 BKN280 FEW400"
    cloudsPresetTbl["Preset17"] = "BKN070 BKN200 BKN320"
    cloudsPresetTbl["Preset18"] = "BKN130 BKN250 BKN380"
    cloudsPresetTbl["Preset19"] = "OVC090 BKN230 BKN310"
    cloudsPresetTbl["Preset20"] = "BKN130 BKN280 FEW380"
    cloudsPresetTbl["Preset21"] = "BKN070 OVC170"
    cloudsPresetTbl["Preset22"] = "BKN070 OVC170"
    cloudsPresetTbl["Preset23"] = "BKN110 OVC180 SCT320"
    cloudsPresetTbl["Preset24"] = "BKN030 OVC170 BKN340"
    cloudsPresetTbl["Preset25"] = "OVC120 OVC220 OVC400"
    cloudsPresetTbl["Preset26"] = "OVC090 BKN230 SCT320"
    cloudsPresetTbl["Preset27"] = "OVC080 BKN250 BKN340"
    cloudsPresetTbl["RainyPreset1"] = "RA OVC030 OVC280 FEW400"
    cloudsPresetTbl["RainyPreset2"] = "RA OVC030 SCT180 FEW400"
    cloudsPresetTbl["RainyPreset3"] = "RA OVC060 OVC190 SCT340"

    if cloudsPreset == nil or cloudsPresetTbl[cloudsPreset] == nil then
        return "CAVOK"
    end

    return cloudsPresetTbl[cloudsPreset]
end

function BuildMetar.getPressureAltitude(referencePoint)
    local THIS_METHOD = THIS_FILE .. ".getPressureAltitude"

    local _, qfeHPA = atmosphere.getTemperatureAndPressure(referencePoint)
    local qfeMB = qfeHPA * PASCAL_TO_MILLIBAR
    local pressureAltitude = 145366.45 * (1 - math.pow((qfeMB / STD_PRESSURE_MILLIBAR), 0.190284))

    DCSWeather.Logger.Info(THIS_METHOD, "Pressure Altitude: " .. pressureAltitude)
    return pressureAltitude
end

function BuildMetar.getQnh(referencePoint)
    local pressureAltitude = BuildMetar.getPressureAltitude(referencePoint)
    local altitudeDifference = (referencePoint.y * METERS_TO_FEET) - pressureAltitude
    local tempCorrectedQNHPasc = ((altitudeDifference / 27) * 100) + STANDARD_PRESSURE_PASCAL
    local qnhInHg = tempCorrectedQNHPasc * PASCALS_TO_INHG

    return "A" .. math.floor(qnhInHg + 0.5)
end

function BuildMetar.getTempDew(referencePoint)
    -- TODO Improve Dew Calculation, not matching real world, maybe get from Data file instead?
    local THIS_METHOD = THIS_FILE .. ".getTempDew"

    local localReferencePoint = {}
    localReferencePoint.x = referencePoint.x
    localReferencePoint.y = 0
    localReferencePoint.z = referencePoint.z
    DCSWeather.Logger.Info(THIS_METHOD, "Temp/Dew Reference point: { x = " .. localReferencePoint.x .. ", y = " .. localReferencePoint.y .. ", z = " .. localReferencePoint.z .. " }")

    local clouds = env.mission.weather.clouds
    local temperature, _ = atmosphere.getTemperatureAndPressure(localReferencePoint)
    temperature = temperature - ZERO_CELCIUS_IN_KELVIN

    -- Calculate Dew Point
    local cloudBase = clouds.base * METERS_TO_FEET
    local dew = temperature - ((cloudBase / 1000) * 2.5)
    dew = math.floor(dew + 0.5)

    -- Absolute Values but prefix M per METAR formatting.
    temperature = math.floor(temperature + 0.5)
    if temperature < 0 then
        temperature = math.abs(temperature)
        temperature = "M" .. temperature
    end
    if dew < 0 then
        dew = math.abs(dew)
        dew = "M" .. dew
    end

    return temperature .. "/" .. dew
end

function BuildMetar.outputMetar(metar)
    local THIS_METHOD = THIS_FILE .. ".outputMetar"
    DCSWeather.JSON.setValue("metar", metar, DCSWeather.DAO)
    DCSWeather.JSON.setValue("weather_type", "real", DCSWeather.DAO)

    if (DCSWeather.JSON.getValue("discord_api_key", DCSWeather.DAO) == "") then
        DCSWeather.Logger.Warning(THIS_METHOD, "No Discord API Key found, not sending METAR to Discord.")
        return
    end
    DCSWeather.JAR.execute("weather-output") -- TODO: Split out Sheets to a different Program
end

function BuildMetar.getStationId()
    local THIS_METHOD = THIS_FILE .. ".getStationId"
    local icao = DCSWeather.JSON.getValue("icao", DCSWeather.DAO)
    if (icao == "") then
        DCSWeather.Logger.Warning(THIS_METHOD, "ICAO not found.")
        return "UNKN"
    else
        return icao
    end
end

function BuildMetar.writeAirbaseCoordinatesToDataFile(referencePoint)
    local THIS_METHOD = THIS_FILE .. ".writeAirbaseCoordinatesToDataFile"

    local stationLatitude, stationLongitude, _ = coord.LOtoLL(referencePoint)
    DCSWeather.JSON.setValue("station_latitude", stationLatitude, DCSWeather.DAO)
    DCSWeather.JSON.setValue("station_longitude", stationLongitude, DCSWeather.DAO)
end

function BuildMetar.main()
    local referencePoint = BuildMetar.getNearestAirbasePoint()
    BuildMetar.writeAirbaseCoordinatesToDataFile(referencePoint)

    local stationId = BuildMetar.getStationId()
    DCSWeather.Logger.Info(THIS_FILE, "Station ID: " .. stationId)

    local dayAndTimeZulu = BuildMetar.getDayAndTimeZulu()
    DCSWeather.Logger.Info(THIS_FILE, "Day and Time Zulu: " .. dayAndTimeZulu)

    local wind = BuildMetar.getWind(referencePoint)
    DCSWeather.Logger.Info(THIS_FILE, "Wind: " .. wind)

    local visibility = BuildMetar.getVisibility()
    DCSWeather.Logger.Info(THIS_FILE, "Visibility: " .. visibility)

    local weatherMods = BuildMetar.getWeatherMods()
    DCSWeather.Logger.Info(THIS_FILE, "Weather Mods: " .. weatherMods)

    local cloudCover = BuildMetar.getCloudCover()
    DCSWeather.Logger.Info(THIS_FILE, "Cloud Cover: " .. cloudCover)

    local tempDew = BuildMetar.getTempDew(referencePoint)
    DCSWeather.Logger.Info(THIS_FILE, "Temp/Dew: " .. tempDew)

    local qnh = BuildMetar.getQnh(referencePoint)
    DCSWeather.Logger.Info(THIS_FILE, "QNH: " .. qnh)

    local metar = stationId .. " " ..
            dayAndTimeZulu .. " " ..
            wind .. " " ..
            visibility .. " " ..
            weatherMods .. " " ..
            cloudCover .. " " ..
            tempDew .. " " ..
            qnh

    DCSWeather.Logger.Info(THIS_FILE, "METAR: " .. metar)
    BuildMetar.outputMetar(metar)
end
BuildMetar.main()
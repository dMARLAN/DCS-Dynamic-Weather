package com.marlan.weatherupdate.service.missioneditor;

import com.marlan.weatherupdate.model.config.Config;
import com.marlan.weatherupdate.model.dto.DTO;
import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.metar.fields.Temperature;
import com.marlan.weatherupdate.model.metar.fields.WindDirection;
import com.marlan.weatherupdate.model.metar.fields.WindSpeed;
import com.marlan.weatherupdate.model.station.AVWXStation;
import com.marlan.weatherupdate.service.missioneditor.values.Station;
import com.marlan.weatherupdate.service.missioneditor.values.Time;
import com.marlan.weatherupdate.service.missioneditor.values.Wind;
import com.marlan.weatherupdate.utilities.Log;
import com.marlan.weatherupdate.utilities.StationInfoUtility;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class MissionValues {
    private static final Log log = Log.getInstance();
    private static final double ISA_TEMP_C = 15;
    private static final double ISA_PRESSURE_INHG = 29.92;
    private static final double INHG_TO_HPA = 33.86389;
    private final Config config;
    private final DTO dto;
    private final AVWXMetar metarAVWX;

    @Getter
    private final Wind wind;
    @Getter
    private final Station station;
    @Getter
    private final Time time;

    public MissionValues(Config config, DTO dto, AVWXStation stationAVWX, AVWXMetar metarAVWX) {
        this.config = config;
        this.dto = dto;
        this.metarAVWX = metarAVWX;
        this.wind = setWind();
        this.station = setStation();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(StationInfoUtility.getZoneId(stationAVWX.getLatitude(), stationAVWX.getLongitude())));
        this.time = setTime(zonedDateTime);
    }

    private Time setTime(ZonedDateTime zonedDateTime) {
        return new Time(setHour(zonedDateTime), setDay(zonedDateTime), setMonth(zonedDateTime));
    }

    private Station setStation() {
        return new Station(setMetar(), setStationTempC(), setStationQnh());
    }

    private Wind setWind() {
        return new Wind(setWindSpeed(), setWindDirection());
    }

    private double setWindSpeed() {
        String dtoWeatherType = dto.getWeatherType();
        if (dtoWeatherType.contains("real")) {
            return metarAVWX.getWindSpeed().flatMap(WindSpeed::getValue).orElse(0.0);
        } else {
            return 0.0;
        }
    }

    private double setWindDirection() {
        String dtoWeatherType = dto.getWeatherType();
        if (dtoWeatherType.contains("real")) {
            return metarAVWX.getWindDirection().flatMap(WindDirection::getValue).orElse(0.0);
        } else {
            return 0.0;
        }
    }

    private double setStationTempC() {
        String dtoWeatherType = dto.getWeatherType();
        if (dtoWeatherType.contains("real")) {
            return metarAVWX.getTemperature().flatMap(Temperature::getValue).orElse(ISA_TEMP_C);
        } else {
            return ISA_TEMP_C;
        }
    }

    private double setStationQnh() {
        String dtoWeatherType = dto.getWeatherType();
        if (dtoWeatherType.contains("real")) {
            if (metarAVWX.getUnits().getAltimeter().equals("hPa")) {
                return metarAVWX.getAltimeter().getValue() / INHG_TO_HPA;
            } else {
                return metarAVWX.getAltimeter().getValue();
            }
        } else {
            return ISA_PRESSURE_INHG;
        }
    }

    private String setMetar() {
        String dtoWeatherType = dto.getWeatherType();
        if (dtoWeatherType.contains("clear")) {
            log.info("METAR set clear");
            return "";
        } else {
            if (metarAVWX.getMeta().getWarning() != null) {
                log.warning(metarAVWX.getMeta().getWarning());
            }
            String assignedMetar = metarAVWX.getSanitized();
            log.info("METAR: " + assignedMetar);
            return assignedMetar;
        }
    }

    private int setDay(@NotNull ZonedDateTime zonedDateTime) {
        return zonedDateTime.getDayOfMonth();
    }

    private int setMonth(@NotNull ZonedDateTime zonedDateTime) {
        return zonedDateTime.getMonthValue();
    }

    private int setHour(ZonedDateTime zonedDateTime) {
        int assignedHour;
        String dtoWeatherType = dto.getWeatherType();
        if (dtoWeatherType.equals("real")) {
            if (zonedDateTime.getHour() + config.getTimeOffset() < 0) {
                assignedHour = 24 + zonedDateTime.getHour() + config.getTimeOffset();
            } else {
                assignedHour = zonedDateTime.getHour() + config.getTimeOffset();
            }
        } else {
            switch (dtoWeatherType) {
                case "real0400" -> assignedHour = 4;
                case "real0600" -> assignedHour = 6;
                case "real1800" -> assignedHour = 18;
                case "real2200" -> assignedHour = 22;
                case "real0000", "clearNight" -> assignedHour = 0;
                default -> assignedHour = 12;
            }
        }
        return assignedHour;
    }

}

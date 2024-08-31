package com.marlan.weatherupdate.service.missioneditor;

import com.marlan.shared.model.Config;
import com.marlan.shared.model.DTO;
import com.marlan.shared.utilities.Log;
import com.marlan.weatherupdate.model.metar.AVWXMetar;
import com.marlan.weatherupdate.model.metar.fields.Temperature;
import com.marlan.weatherupdate.model.metar.fields.WindDirection;
import com.marlan.weatherupdate.model.metar.fields.WindSpeed;
import com.marlan.weatherupdate.model.station.AVWXStation;
import com.marlan.weatherupdate.service.missioneditor.values.Station;
import com.marlan.weatherupdate.service.missioneditor.values.Time;
import com.marlan.weatherupdate.service.missioneditor.values.Wind;
import com.marlan.weatherupdate.utilities.StationInfoUtility;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        if (dtoWeatherType.contains("real") || dtoWeatherType.equals("cvops")) {
            return metarAVWX.getWindSpeed().flatMap(WindSpeed::getValue).orElse(0.0);
        } else {
            return 0.0;
        }
    }

    private double setWindDirection() {
        String dtoWeatherType = dto.getWeatherType();
        if (dtoWeatherType.contains("real") || dtoWeatherType.equals("cvops")) {
            return metarAVWX.getWindDirection().flatMap(WindDirection::getValue).orElse(0.0);
        } else {
            return 0.0;
        }
    }

    private double setStationTempC() {
        String dtoWeatherType = dto.getWeatherType();
        if (dtoWeatherType.contains("real") || dtoWeatherType.equals("cvops")) {
            return metarAVWX.getTemperature().flatMap(Temperature::getValue).orElse(ISA_TEMP_C);
        } else {
            return ISA_TEMP_C;
        }
    }

    private double setStationQnh() {
        String dtoWeatherType = dto.getWeatherType();
        if (dtoWeatherType.contains("real") || dtoWeatherType.equals("cvops")) {
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

    private float setHour(ZonedDateTime zonedDateTime) {
        float assignedHour;
        String dtoWeatherType = dto.getWeatherType();
        if (dtoWeatherType.equals("real")) {
            if (zonedDateTime.getHour() + config.getTimeOffset() < 0) {
                assignedHour = (float) 24 + zonedDateTime.getHour() + config.getTimeOffset();
            } else {
                assignedHour = (float) zonedDateTime.getHour() + config.getTimeOffset();
            }
        } else if (dtoWeatherType.contains("cvops")) {
            int currentTimeInSecs = (int) Double.parseDouble(dto.getCurrentGameTime());
            if (currentTimeInSecs < 0) {
                currentTimeInSecs = 0;
            }
            List<Integer> listOfCVEventStarts = getCVEventStarts();
            int preEventTime = config.getPreEventTime();

            final int finalCurrentTimeInSecs = currentTimeInSecs;
            int closestEvent = listOfCVEventStarts
                    .stream()
                    .min(Comparator.comparingInt(a -> Math.abs(finalCurrentTimeInSecs - a)))
                    .orElse(0);

            if (currentTimeInSecs > listOfCVEventStarts.get(listOfCVEventStarts.size() - 1) + 1800){
                assignedHour = ((float)(listOfCVEventStarts.get(0) - preEventTime) / 3600) % 24;
            } else {
                assignedHour = ((float)(closestEvent - preEventTime) / 3600) % 24;
            }
        } else {
            Pattern pattern = Pattern.compile("(real|clear)(\\d{4})");
            Matcher matcher = pattern.matcher(dtoWeatherType);

            if (matcher.matches()) {
                String timeStr = matcher.group(2);
                int hour = Integer.parseInt(timeStr.substring(0, 2));
                int minute = Integer.parseInt(timeStr.substring(2, 4));
                assignedHour =  hour + minute / 60.0f;
            } else if ("clearNight".equals(dtoWeatherType)) {
                assignedHour = 0.0f;
            } else {
                assignedHour = 12.0f;
            }
        }
        return assignedHour;
    }

    private List<Integer> getCVEventStarts() {
        List<Integer> cvEventStarts = new ArrayList<>();
        int cyclicWindows = config.getCyclicWindows();
        int firstCyclicTimeInSecs = config.getFirstCyclicTimeInSecs();
        int cyclicLengthInSecs = config.getCyclicLength() * 60;

        for (int i = 0; i < cyclicWindows; i++) {
            cvEventStarts.add(firstCyclicTimeInSecs + (i * cyclicLengthInSecs));
        }

        return cvEventStarts;
    }

}

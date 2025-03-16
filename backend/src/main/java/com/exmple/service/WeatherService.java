package com.exmple.service;

import com.exmple.entity.vo.response.WeatherVo;

public interface WeatherService {
    WeatherVo getWeather(double lon, double lat);
}

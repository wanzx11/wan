package com.exmple.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.exmple.entity.vo.response.WeatherVo;
import com.exmple.service.WeatherService;
import com.exmple.utils.Const;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

@Slf4j
@Service
public class WeatherServiceImpl implements WeatherService {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    RestTemplate restTemplate;

    @Override
    public WeatherVo getWeather(double lon, double lat) {
        return getWeatherCache(lon, lat);
    }

    private WeatherVo getWeatherCache(double lon, double lat) {
        byte[] data = restTemplate.getForObject(
                "https://geoapi.qweather.com/v2/city/lookup?location="+lon+","+lat+"&key="+ Const.WEATHER_KEY, byte[].class);
        JSONObject jsonObject = this.decompressToJson(data);
        JSONArray locationArray = jsonObject.getJSONArray("location");
        if (locationArray == null || locationArray.isEmpty()) {
            log.error("Location data is empty or not found.");
            return null;
        }
        // 获取第一个 location 元素
        JSONObject location = locationArray.getJSONObject(0);
        log.info(location.toString());
        if (location == null) {
            return null;
        }
        Object idValue = location.get("id");
        Integer id = null;
        if (idValue instanceof String) {
            try {
                id = Integer.parseInt((String) idValue);
            } catch (NumberFormatException e) {
                log.error("id字段转换失败: 非法数字格式");
            }
        } else if (idValue instanceof Number) {
            id = ((Number) idValue).intValue();
        } else {
            log.error("id字段不是数字类型");
        }

        String key = "weather:"+id;
        String cache = stringRedisTemplate.opsForValue().get(key);
        if (cache != null) {
            return JSONObject.parseObject(cache, WeatherVo.class);
        }
        WeatherVo weatherVo  = this.callFromApi(id,location);
        stringRedisTemplate.opsForValue().set(key,JSONObject.toJSONString(weatherVo),24, TimeUnit.HOURS);
        return weatherVo;
    }

    private WeatherVo callFromApi(int id,JSONObject location){
        WeatherVo vo = new WeatherVo();
        vo.setLocation(location);
        JSONObject jsonObject = this.decompressToJson(restTemplate.getForObject(
                "https://devapi.qweather.com/v7/weather/now?location="+id+"&key="+ Const.WEATHER_KEY, byte[].class));
        JSONObject now = jsonObject.getJSONObject("now");
        log.info(now.toString());
        if(now == null){
            log.error("Now data is empty or not found.");
            return null;
        }
        vo.setNow(now);
        JSONObject jsonObject2 = this.decompressToJson(restTemplate.getForObject(
                "https://devapi.qweather.com/v7/weather/7d?location="+id+"&key="+ Const.WEATHER_KEY, byte[].class));
        JSONArray daily = jsonObject2.getJSONArray("daily");
        log.info(daily.toString());
        if(daily == null){
            log.error("Daily data is empty or not found.");
            return null;
        }
        vo.setDaily(daily);
        return vo;
    }



    /**
     * 解压字节数组并解析为 JSON 对象
     *
     * @param bytes byte[] - GZIP 压缩的字节数组
     * @return JSONObject - 解压后的 JSON 对象
     * @throws RuntimeException 如果解压失败
     */
    //返回数据是JSON格式并进行了Gzip压缩。
    private JSONObject decompressToJson(byte[] bytes) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            gis.close();
            byteArrayOutputStream.close();
            log.info("Decompressed JSON String: {}", byteArrayOutputStream);
            return JSONObject.parseObject(byteArrayOutputStream.toString());
        } catch (IOException e) {
            log.error("Decompression failed: ", e);
            return null;
        }
    }

}

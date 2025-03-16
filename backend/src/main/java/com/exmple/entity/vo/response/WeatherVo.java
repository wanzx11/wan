package com.exmple.entity.vo.response;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

@Data
public class WeatherVo {
    JSONObject location;
    JSONObject now;
    JSONArray daily;
}

package com.example.sample;

import javax.servlet.http.HttpServletRequest;

public class SampleController {
    public String inspect(HttpServletRequest request) {
        return request.getMethod();
    }
}

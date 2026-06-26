package com.example.musicbackend;

import java.util.HashMap;
import java.util.Map;

public class ApiResponse {
    public static Map<String, Object> message(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}

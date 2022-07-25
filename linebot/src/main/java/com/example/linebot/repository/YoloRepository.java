package com.example.linebot.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class YoloRepository {

    private final RestTemplate restTemplate;

    @Autowired
    public YoloRepository(RestTemplateBuilder templateBuilder) {
        this.restTemplate = templateBuilder.build();
    }

    public String findYoloAPI(HttpEntity<byte[]> request) {
        // ローカル
        String url = "http://127.0.0.1:5000/detection";
        // aws ec2
        // String url = "http://{IPアドレス}:5000/detection";

        return restTemplate.postForObject(url, request, String.class);
    }
}

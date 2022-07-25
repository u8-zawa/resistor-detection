package com.example.linebot.replier;

import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class Yolo implements Replier {

    private final String path;

    public Yolo(String path) {
        this.path = path;
    }

    @Override
    public Message reply() {
        RestTemplateBuilder templateBuilder = new RestTemplateBuilder();
        RestTemplate restTemplate = templateBuilder.build();

        try {
            var file = new File(path);
            var bytes = Files.readAllBytes(file.toPath());
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(Files.probeContentType(file.toPath())));
            headers.setContentLength(bytes.length);
            var request = new HttpEntity<>(bytes, headers);

            // ローカル
            String url = "http://127.0.0.1:5000/detection";
            // aws ec2
            // String url = "http://{IPアドレス}:5000/detection";

            String result = restTemplate.postForObject(url, request, String.class);
            return new TextMessage(Objects.requireNonNull(result));
        } catch (RestClientException | IOException e) {
            return new TextMessage(Objects.requireNonNull(e.getMessage()));
        }
    }
}

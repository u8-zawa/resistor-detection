package com.example.linebot.service;

import com.example.linebot.replier.Yolo;
import com.example.linebot.repository.YoloRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class YoloService {

    private final YoloRepository yoloRepository;

    @Autowired
    public YoloService(YoloRepository yoloRepository) {
        this.yoloRepository = yoloRepository;
    }

    public Yolo doReplyWithYolo(String path) {
        HttpEntity<byte[]> request = null;

        var file = new File(path);
        try {
            var bytes = Files.readAllBytes(file.toPath());
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(Files.probeContentType(file.toPath())));
            headers.setContentLength(bytes.length);
            request = new HttpEntity<>(bytes, headers);
        } catch (IOException e) {
            e.printStackTrace();
        }

        var result = yoloRepository.findYoloAPI(request);
        return new Yolo(result);
    }
}

package com.example.linebot.replier;

import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;

public class Yolo implements Replier {

    private final String result;

    public Yolo(String result) {
        this.result = result;
    }

    @Override
    public Message reply() {
        return new TextMessage(result);
    }
}

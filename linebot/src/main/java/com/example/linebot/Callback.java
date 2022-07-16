package com.example.linebot;

import com.example.linebot.replier.*;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@LineMessageHandler
public class Callback {

    private static final Logger log = LoggerFactory.getLogger(Callback.class);

    // フォローイベントに対応する
    @EventMapping
    public Message handleFollow(FollowEvent event) {
        // 実際はこのタイミングでフォロワーのユーザIDをデータベースにに格納しておくなど
        Follow follow = new Follow(event);
        return follow.reply();
    }

    // 文章で話しかけられたとき（テキストメッセージのイベント）に対応する
    @EventMapping
    public Message handleMessage(MessageEvent<TextMessageContent> event) {
        TextMessageContent tmc = event.getMessage();
        String text = tmc.getText();
        // 改行を削除（要約で改行の入っている文章も取り扱いたいため）
        text = text.replace("\n", "");
        text = text.replace("\r", "");
        Intent intent = Intent.whichIntent(text);
        switch (intent) {
            case PYTHONAPI:
                PythonApi pythonApi = new PythonApi(event);
                return pythonApi.reply();
            case SUMMARY:
                Summary summary = new Summary(event);
                return summary.reply();
            case UNKNOWN:
            default:
                Parrot parrot = new Parrot(event);
                return parrot.reply();
        }
    }
}

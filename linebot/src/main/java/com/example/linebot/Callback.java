package com.example.linebot;

import com.example.linebot.replier.*;
import com.linecorp.bot.client.LineBlobClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@LineMessageHandler
public class Callback {

    private static final Logger log = LoggerFactory.getLogger(Callback.class);
    private final LineBlobClient client;

    @Autowired
    public Callback(LineBlobClient client) {
        this.client = client;
    }

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

        PythonApi pythonApi = new PythonApi(event);
        Summary summary = new Summary(event);
        Parrot parrot = new Parrot(event);

        Intent intent = Intent.whichIntent(text);
        switch (intent) {
            case PYTHONAPI -> {
                return pythonApi.reply();
            }
            case SUMMARY -> {
                return summary.reply();
            }
            case UNKNOWN -> {
                return parrot.reply();
            }
        }
        return parrot.reply();
    }

    // 画像のメッセージイベントに対応する
    @EventMapping
    public Message handleImg(MessageEvent<ImageMessageContent> event) {
        // 画像メッセージのidを取得する
        String msgId = event.getMessage().getId();
        Optional<String> opt = Optional.empty();
        try {
            // 画像メッセージのidを使って MessageContentResponse を取得する
            MessageContentResponse resp = client.getMessageContent(msgId).get();
            log.info("get content{}:", resp);
            // MessageContentResponse からファイルをローカルに保存する
            // ※LINEでは、どの解像度で写真を送っても、サーバ側でjpgファイルに変換される
            opt = makeTmpJPGFile(resp);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        String path = opt.orElse("ファイル書き込みNG");

        Yolo yolo = new Yolo(path);
        return yolo.reply();
    }

    // MessageContentResponseの中のバイト入力ストリームを、拡張子を指定してファイルに書き込む。
    // また、保存先のファイルパスをOptional型で返す。
    private Optional<String> makeTmpJPGFile(MessageContentResponse resp) {
        // tmpディレクトリに一時的に格納して、ファイルパスを返す
        try (InputStream is = resp.getStream()) {
            Path tmpFilePath = Files.createTempFile("linebot", ".jpg");
            Files.copy(is, tmpFilePath, StandardCopyOption.REPLACE_EXISTING);
            return Optional.of(tmpFilePath.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}

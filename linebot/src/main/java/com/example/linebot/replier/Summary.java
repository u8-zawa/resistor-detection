package com.example.linebot.replier;

import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Summary implements Replier {

    private final MessageEvent<TextMessageContent> event;

    public Summary(MessageEvent<TextMessageContent> event) {
        this.event = event;
    }

    @Override
    public Message reply() {
        TextMessageContent tmc = event.getMessage();
        String text = tmc.getText();
        // 改行を削除（要約で改行の入っている文章も取り扱いたいため）
        text = text.replace("\n", "");
        text = text.replace("\r", "");

        RestTemplateBuilder templateBuilder = new RestTemplateBuilder();
        RestTemplate restTemplate = templateBuilder.build();

        String regexp = Intent.SUMMARY.getRegexp();
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(text);
        String planeText;
        if (matcher.matches()) {
            // テキストの中から元文を抜き出す
            planeText = matcher.group(1);
        } else {
            // 正規表現にマッチしない場合、実行時例外を throw する
            throw new IllegalArgumentException("text をスロットに分けられません");
        }

        // ローカル
        String url = String.format("http://127.0.0.1:5000/summary?doc=%s", planeText);
        // aws ec2
        // String url = String.format("http://{IPアドレス}:5000/summary?doc=%s", planeText);

        try {
            String[] results = restTemplate.getForObject(url, String[].class);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i< Objects.requireNonNull(results).length; i++) {
                stringBuilder.append(results[i]);
            }
            return new TextMessage(stringBuilder.toString());
        } catch (RestClientException e) {
            return new TextMessage(Objects.requireNonNull(e.getMessage()));
        }
    }
}

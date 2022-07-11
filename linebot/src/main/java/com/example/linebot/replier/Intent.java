package com.example.linebot.replier;

import java.util.EnumSet;
import java.util.regex.Pattern;

public enum Intent {

    // メッセージの正規表現パターンに対応するやりとり状態の定義
    UNKNOWN(".+");

    private final String regexp;

    private Intent(String regexp) {
        this.regexp = regexp;
    }

    // メッセージからやりとり状態を判断
    public static Intent whichIntent(String text) {
        // 全てのIntentを取得
        EnumSet<Intent> set = EnumSet.allOf(Intent.class);
        // 引数 text が、どの Intent のパターンに当てはまるかチェック
        // 当てはまった Intent を戻り値とする
        for (Intent intent : set) {
            if (Pattern.matches(intent.regexp, text)) {
                return intent;
            }
        }
        return UNKNOWN;
    }

    public String getRegexp() {
        return regexp;
    }

}

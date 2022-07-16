from flask import Flask, request, jsonify

from pysummarization.nlpbase.auto_abstractor import AutoAbstractor
from pysummarization.tokenizabledoc.mecab_tokenizer import MeCabTokenizer
from pysummarization.abstractabledoc.top_n_rank_abstractor import TopNRankAbstractor

app = Flask(__name__)
app.config["JSON_AS_ASCII"] = False


@app.route('/')
def hello():
    return 'Hello'


# 文章要約
@app.route('/summary', methods=["GET"])
def document_summarize():
    # クエリパラメータを取得
    req = request.args
    document = req.get("doc")
    # 全ての行を結合
    document = ''.join(document)
    # 自動要約のオブジェクトを生成
    auto_abstractor = AutoAbstractor()
    # トークナイザー（単語分割）にMeCabを指定
    auto_abstractor.tokenizable_doc = MeCabTokenizer()
    # 文書の区切り文字を指定
    auto_abstractor.delimiter_list = ["。", "\n"]
    # キュメントの抽象化、フィルタリングを行うオブジェクトを生成
    abstractable_doc = TopNRankAbstractor()
    # 文書の要約を実行
    result_dict = auto_abstractor.summarize(document, abstractable_doc)

    # 重要度が一番高い文章のindexを取得する
    # priorities = []
    # for x in result_dict["scoring_data"]:
    #     priorities.append(x[1])
    # max_priority_index = priorities.index(max(priorities))

    return jsonify(result_dict["summarize_result"])


if __name__ == "__main__":
    app.run(debug=True)

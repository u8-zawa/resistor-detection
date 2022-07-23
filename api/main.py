from flask import Flask, request, jsonify

from pysummarization.nlpbase.auto_abstractor import AutoAbstractor
from pysummarization.tokenizabledoc.mecab_tokenizer import MeCabTokenizer
from pysummarization.abstractabledoc.top_n_rank_abstractor import TopNRankAbstractor

import torch
from PIL import Image
from io import BytesIO

# model = torch.hub.load('path/to/yolov5', 'custom', path='path/to/best.pt', source='local')
model = torch.hub.load('yolov5', 'custom', path='yolov5/runs/train/exp2/weights/best.pt', source='local')
names = model.names

app = Flask(__name__)
app.config["JSON_AS_ASCII"] = False


def _detection(img):
    results = model(img)
    detects = []
    for result in results.xyxy[0]:
        p1x, p1y, p2x, p2y, score, index = result
        detects.append({
            "p1": {"x": float(p1x), "y": float(p1y)},
            "p2": {"x": float(p2x), "y": float(p2y)},
            "score": float(score),
            "label": names[int(index)]
        })
    message = "判別出来ませんでした。"
    if len(detects) != 0:
        message = ""
        for detect in detects:
            # 100, 150, 330, 470, 620, 820ω
            message = message + "抵抗値: " + detect.get("label")[:3] + "ω\n"
            message = message + "信頼度: " + str(format(detect.get("score"), ".4f")) + "\n"
        message = message.rstrip("\r\n")
    return message


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


@app.route('/detection', methods=["POST"])
def detection():
    img = Image.open(BytesIO(request.data))
    label = _detection(img)
    return label


if __name__ == "__main__":
    app.run(debug=True)

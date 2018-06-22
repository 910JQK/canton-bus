#!/usr/bin/env python3


from flask import Flask, Response, render_template, redirect, url_for
from process import *


app = Flask(__name__)


@app.route('/', methods=['GET'])
def 首頁():
    return 查詢('')


@app.route('/<search_str>', methods=['GET'])
def 查詢(search_str):
    # blah
    return render_template('index.html') 


@app.route('/route/<int:routeId>', methods=['GET'])
def 查詢線路(routeId):
    資訊 = 取得線路全資訊(routeId)
    線路圖 = 資訊['線路圖']
    for 車站 in 線路圖:
        車站['將到車輛'] = {}
        for 方向 in ['上行', '下行']:
            車站['將到車輛'][方向] = []
            for 車輛 in 資訊[方向+'車輛表']:
                if 車輛['下一站'] == 車站['車站名稱']:
                    車站['將到車輛'][方向].append(車輛)
    return render_template('route.html', 線路圖=線路圖)


@app.route('/station/<int:stationNameId>', methods=['GET'])
def 查詢車站(stationNameId):
    站距表 = 取得站距表(stationNameId)
    車站名稱 = 站距表['車站名稱']
    各線路站距表 = [
        {'線路名稱': 線路名稱, **資訊}
        for 線路名稱,資訊 in 站距表['各線路站距表'].items()
    ]
    各線路站距表.sort(key=lambda t: t['線路名稱'])
    return render_template(
        'station.html',  車站名稱=車站名稱, 站距表=各線路站距表
    )


def run():
    app.run(debug=True)


if __name__ == '__main__':
    run()

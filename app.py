#!/usr/bin/env python3


from flask import Flask, Response, render_template, request, url_for
from datetime import datetime, timedelta
from functools import wraps
from process import *
import sys


class PrefixMiddleware(object):
    def __init__(self, app, prefix=''):
        self.app = app
        self.prefix = prefix
    def __call__(self, environ, start_response):
        #environ['PATH_INFO'] = environ['PATH_INFO'][len(self.prefix):]
        environ['SCRIPT_NAME'] = self.prefix
        return self.app(environ, start_response)


app = Flask(__name__)
if len(sys.argv) > 1:
    app.wsgi_app = PrefixMiddleware(app.wsgi_app, prefix=sys.argv[1])


app.add_template_filter(刪除尾字, '刪除尾字')
app.add_template_filter(去除括號, '去除括號')
app.add_template_filter(去除括號內容, '去除括號內容')
app.add_template_filter(提取括號, '提取括號')
app.add_template_filter(簡化總站名, '簡化總站名')
app.add_template_filter(簡化站名, '簡化站名')
app.add_template_filter(修飾分站名, '修飾分站名')
app.add_template_filter(取得分站類型, '取得分站類型')


def time_interval(sec):
    access_time = {}
    def decorator(f):
        @wraps(f)
        def F(*args, **kwargs):
            nonlocal access_time
            ip = request.remote_addr
            if access_time.get(ip):
                if datetime.now() - access_time[ip] < timedelta(seconds=sec):
                    response = Response(render_template('frequent.html'))
                    response.status_code = 400
                    return response
                else:
                    access_time[ip] = datetime.now()
            else:
                access_time[ip] = datetime.now()
            return f(*args, **kwargs)
        return F
    return decorator


@app.route('/', methods=['GET'])
@time_interval(1)
def 搜尋頁():
    搜尋字串 = request.args.get('search')
    if 搜尋字串:
        搜尋結果 = 搜尋(搜尋字串)
    else:
        搜尋結果 = {'線路': [], '車站': []}
    return render_template('search.html', 搜尋結果=搜尋結果)


@app.route('/route/<int:routeId>', methods=['GET'])
@time_interval(4)
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
    return render_template('route.html', 線路圖=線路圖, 資訊=資訊)


@app.route('/station/<int:stationNameId>', methods=['GET'])
@time_interval(3)
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

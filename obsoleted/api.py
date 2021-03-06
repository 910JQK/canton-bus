#!/usr/bin/env python3


import re
import sys
import json
import argparse
import urllib.request
from urllib.parse import urlencode, unquote


quiet = False
TIMEOUT = 10
API_URL = 'https://rycxapi.gci-china.com/xxt-min-api/bus/'
BOOL = {'0': False, '1': True}
方向名 = {'0': '上行', '1': '下行'}
方向號 =  {'上行': '0', '下行': '1'}


def info(*args, **kwargs):
    if not quiet:
        print(*args, file=sys.stderr, **kwargs)


def gen_url(action, **kwargs):
    return API_URL + action + '?' + urlencode(kwargs)


def fetch(action, **kwargs):
    url = gen_url(action, **kwargs)
    info('【請求】%s' % url)
    response = urllib.request.urlopen(url, timeout=TIMEOUT)
    return json.loads(response.read())


def 搜尋(線路或車站名稱):
    data = fetch('getByName.do', name=線路或車站名稱)
    if data['retCode'] != 0:
        return None
    結果 = {'線路':[],'車站':[]}
    線路列表 = data['retData']['route']
    車站列表 = data['retData']['station']
    for 線路 in 線路列表:
        結果['線路'].append({
            '編號': 線路['i'],
            '始發': 線路['start'],
            '終到': 線路['end'],
            '名稱': 線路['n']
        })
    for 車站 in 車站列表:
        結果['車站'].append({
            '編號': 車站['i'],
            '名稱': 車站['n'],
            'c': 車站['c']
        })
    return 結果


def 以位置搜尋(經度, 緯度):
    data = fetch(
        'station/getByCoord.do',
        longitude=經度, latitude=緯度,
        range='500', withLCheck='t'
    )
    if data['retCode'] != 0:
        return None
    結果 = []
    for 車站 in data['retData']:
        結果.append({
            '車站編號': 車站['i'],
            '車站名稱': 車站['n'],
            'c': 車站['c']
        })
    return 結果


def 車站資訊(車站編號):
    data = fetch('routeStation/getByStation.do', stationNameId=車站編號)
    runbus = fetch('runbus/getByStation.do', stationNameId=車站編號)
    if data['retCode'] != 0 or runbus['retCode'] != 0:
        return None
    結果 = {'車站編號':車站編號,'車站名稱':data['retData']['n'],'線路':[]}
    for 線路 in data['retData']['l']:
        次車距離 = -1
        for 到站資訊 in runbus['retData']:
            if str(到站資訊['i']) == 線路['rsi']:
                次車距離 = 到站資訊['c']
        結果['線路'].append({
            '線路名稱': 線路['rn'],
            '方向': 方向名[線路['d']],
            '線路編號': 線路['ri'],
            '線路車站號': 線路['rsi'],
            '始發終到': 線路['dn'],
            '次車距離': 次車距離
        })
    return 結果


def 線路資訊(線路編號, 方向號):
    data = fetch(
        'route/getByRouteIdAndDirection.do',
        direction=方向號,
        routeId=線路編號
    )
    if data['retCode'] != 0:
        return None
    rb = data['retData']['rb']
    結果 = {
        '巴士公司': rb['organName'],
        '線路名稱': rb['rn'],
        '方向': 方向名[rb['d']],
        '首班車': rb['ft'],
        '尾班車': rb['lt'],
        'c': rb['c'],
        '車站列表': [],
        '車輛列表': []
    }
    for 車站 in rb['l']:
        地鐵 = []
        if 車站.get('sw') == '1':
            for 地鐵站 in 車站['sinfo']:
                地鐵.append(地鐵站['name'])
        結果['車站列表'].append({
            '車站名稱': 車站['n'],
            '分站': 車站['order'],
            '車站編號': 車站['sni'],
            '線路車站編号': 車站['i'],
            '過站車站編號': 車站['si'],
            '經度': 車站['lon'],
            '緯度': 車站['lat'],
            '地鐵': 地鐵,
            'BRT': BOOL[車站['brt']]
        })
    runb = data['retData']['runb']
    bus_list = []
    for I in runb:
        for bl in I['bl']:
            bus_list.append(bl)
        for bbl in I['bbl']:
            bus_list.append(bbl)
    for 車輛 in bus_list:
        結果['車輛列表'].append({
            'SubID': 車輛['sub'],
            'BusID': 車輛['i'],
            '車牌': 車輛['no'],
            '經度': 車輛['lo'],
            '緯度': 車輛['la'],
            '司機': 車輛['en'],
            'ec': 車輛['ec'],
            't': 車輛['t']
        })
    return 結果


def 車輛資訊(BusID, SubID):
    data = fetch(
        'routeSub/getBySubId.do',
        busId=BusID,
        subId=SubID
    )
    if data['retCode'] != 0:
        return None
    if data['retData']['d'] == '':
        return None
    結果 = {
        '發班時間': ':'.join(data['retData']['d']['fbt'].split(':')[:-1]),
        '班次類型': data['retData']['d']['n'],
        '過站表': [],
        's': data['retData']['s'],
        't': data['retData']['d']['t'],
        'nb': data['retData']['d']['nb'],
    }
    for 車站 in data['retData']['d']['l']:
        結果['過站表'].append({
            '車站名稱': 車站['n'],
            '預估時間': 車站['ti'],
            '過站車站編號': 車站['i']   
        })
    return 結果


def main():
    if len(sys.argv) in range(1,3):
        sys.argv.append('--help')
    parser = argparse.ArgumentParser(description='Realtime Bus Info')
    subparsers = parser.add_subparsers()
    lookup = subparsers.add_parser('lookup', help='Lookup Bus or Station')
    lookup.add_argument('string')
    lookup.set_defaults(func=lambda args: print(搜尋(args.string)),)
    location = subparsers.add_parser('location', help='Lookup by Geolocation')
    location.add_argument('lon', help='Longitude')
    location.add_argument('lat', help='Latitude')
    location.set_defaults(
        func=lambda args: print(以位置搜尋(args.lon, args.lat))
    )
    station = subparsers.add_parser('station', help='Station Info')
    station.add_argument('id', help='Station ID')
    station.set_defaults(func=lambda args: print(車站資訊(args.id)) )
    route = subparsers.add_parser('route', help='Route Info')
    route.add_argument('id', help='Route ID')
    route.add_argument('direction', help='0 or 1')
    route.set_defaults(func=lambda args: print(線路資訊(args.id, args.direction)) )
    bus = subparsers.add_parser('bus', help='Specific Bus Info')
    bus.add_argument('busid', help='Bus ID')
    bus.add_argument('subid', help='Sub ID')
    bus.set_defaults(func=lambda args: print(車輛資訊(args.busid, args.subid)))
    args = parser.parse_args()
    args.func(args)


if __name__ == '__main__':
    main()

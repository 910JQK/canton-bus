from api import *


def 取得線路圖(上行資訊, 下行資訊):
    線路圖 = []
    #環線 = '环线' in 上行資訊['線路名稱']
    def 取得分站號(車站):
        if 車站['BRT']:
            return 車站['分站']
        else:
            return 車站['分站'][1:]
    上行站表 = 上行資訊['車站列表']
    上行有站 = {上行站['車站名稱'] for 上行站 in 上行站表}
    下行站表 = 下行資訊['車站列表']
    下行有站 = {下行站['車站名稱'] for 下行站 in 下行站表}
    下行站表.reverse()
    上行索引 = 0
    下行索引 = 0
    while 上行索引 < len(上行站表) and 下行索引 < len(下行站表):
        上行站 = 上行站表[上行索引]
        下行站 = 下行站表[下行索引]
        if 上行站['車站名稱'] == 下行站['車站名稱']:
            上行索引 += 1
            下行索引 += 1
            線路圖.append({
                '車站名稱': 上行站['車站名稱'],
                '車站編號': 上行站['車站編號'],
                '上行分站': 取得分站號(上行站),
                '下行分站': 取得分站號(下行站)
            })
        elif 上行站['車站名稱'] in 下行有站:
            下行索引 += 1
            線路圖.append({
                '車站名稱': 下行站['車站名稱'],
                '車站編號': 下行站['車站編號'],
                '上行分站': None,
                '下行分站': 取得分站號(下行站)
            })
        else:
            上行索引 += 1
            線路圖.append({
                '車站名稱': 上行站['車站名稱'],
                '車站編號': 上行站['車站編號'],
                '上行分站': 取得分站號(上行站),
                '下行分站': None
            })
    return 線路圖


def 取得線路全資訊(線路編號):
    上行資訊 = 線路資訊(線路編號, 方向號['上行'])
    下行資訊 = 線路資訊(線路編號, 方向號['下行'])
    上行終點 = 上行資訊['車站列表'][-1]['過站車站編號']
    下行終點 = 下行資訊['車站列表'][-1]['過站車站編號']
    上行車輛資訊 = []
    下行車輛資訊 = []
    #環線 = '环线' in 上行資訊['線路名稱']
    for 車輛 in 上行資訊['車輛列表']:
        上行車輛資訊.append(車輛資訊(車輛['BusID'], 車輛['SubID']))
    for 車輛 in 下行資訊['車輛列表']:
        下行車輛資訊.append(車輛資訊(車輛['BusID'], 車輛['SubID']))
    線路圖 = 取得線路圖(上行資訊, 下行資訊)
    def 取得車輛表(車輛資訊):
        車輛表 = []
        for 車輛 in 車輛資訊:
            if 車輛 is None:
                continue
            下一站 = 車輛['過站表'][0]['車站名稱']
            終點站 = 車輛['過站表'][-1]['車站名稱']
            班次終點 = 車輛['過站表'][-1]['過站車站編號']
            for 車站 in 車輛['過站表']:
                if int(車站['預估時間']) < 0:
                    下一站 = 車站['車站名稱']
                else:
                    break
            if 班次終點 in [上行終點, 下行終點]:
                班次類型 = '全程'
            else:
                班次類型 = '短线'
            車輛表.append({
                '發班時間': 車輛['發班時間'],
                '班次類型': 班次類型,
                #'班次類型': 車輛['班次類型'], (該資訊可能有誤)
                '下一站': 下一站,
                '終點站': 終點站
            })
        車輛表.sort(key=lambda t: t['發班時間'])
        return 車輛表
    上行車輛表 = 取得車輛表(上行車輛資訊)
    下行車輛表 = 取得車輛表(下行車輛資訊)
    return {
        '線路圖': 線路圖,
        '上行車輛表': 上行車輛表,
        '下行車輛表': 下行車輛表,
        '線路名稱': 上行資訊['線路名稱'],
        '上行首班車': 上行資訊['首班車'],
        '上行尾班車': 上行資訊['尾班車'],
        '下行首班車': 下行資訊['首班車'],
        '下行尾班車': 下行資訊['尾班車']
    }


def 取得站距表(車站編號):
    資訊 = 車站資訊(車站編號)
    站距表 = {}
    for 線路 in 資訊['線路']:
        線路編號 = 線路['線路編號']
        線路名 = 線路['線路名稱']
        方向 = 線路['方向']
        終點站 = 線路['始發終到'].split('-')[-1]
        次車距離 = 線路['次車距離']
        if not 站距表.get(線路名):
            站距表[線路名] = {}
        站距表[線路名]['線路編號'] = 線路編號
        站距表[線路名][方向] = {}
        站距表[線路名][方向]['終點站'] = 終點站
        站距表[線路名][方向]['次車距離'] = 次車距離
    return {
        '車站編號': 車站編號,
        '車站名稱': 資訊['車站名稱'],
        '各線路站距表': 站距表
    }


def 刪除尾字(線路名):
    數字 = '0123456789'
    if 線路名[-1] == '路' or (線路名[-1] == '线' and 線路名[-2] in 數字):
        return 線路名[:-1]
    else:
        return 線路名


def 去除括號(車站名):
    return ''.join([
        (字符 if 字符 not in ['(', ')', '（', '）'] else '') for 字符 in 車站名
    ])


def 去除括號內容(車站名):
    結果 = ''
    括號 = 0
    for 字符 in 車站名:
        if 字符 in ['(', '（']:
            括號 += 1
        if 括號 == 0:
            結果 += 字符
        if 字符 in [')', '）']:            
            括號 -= 1
    return 結果


def 提取括號(車站名):
    結果 = ''
    括號 = 0
    for 字符 in 車站名:
        if 字符 in [')', '）']:            
            括號 -= 1
        if 括號 == 1:
            結果 += 字符
        if 字符 in ['(', '（']:
            括號 += 1
    return 結果


def 去除總站後綴(車站名):
    if 車站名[-2:] == '总站':
        return 車站名[:-2]
    else:
        return 車站名


def 簡化總站名(車站名):
    return 去除括號(去除總站後綴(車站名))


def 簡化站名(車站名):
    if 車站名[-2:] == '总站':
        #return 簡化總站名(車站名)
        return 車站名
    elif 車站名[-1] == '站':
        return 車站名[:-1]
    else:
        return 車站名


def 修飾分站名(分站名):
    圈數字 = ' ①②③④⑤⑥⑦⑧⑨'
    if len(分站名) == 1:
        return 圈數字[int(分站名)]
    else:
        return 分站名


def 取得分站類型(分站名):
    if 分站名 is None:
        return '無站'
    elif 分站名[0] in ['N', 'S']:
        return 'BRT'
    elif 分站名 == '0':
        return '唯一站'
    else:
        return '多站'

# coding=utf-8
import os
import zipfile
import urllib
import sys
import shutil
import stat
import re

if sys.getdefaultencoding() != 'utf-8':
    reload(sys)
    sys.setdefaultencoding('utf-8')

print '----------开始初始化FIT2CLOUD部署环境----------'
print ''


try:
    default_path = '${default_path}'
    artifact_name = '${artifact_name}'
    download_url = '${download_url}'
except Exception as e:
    print 'args error', e
    raise Exception('参数错误')


def callback(a, b, c):
    if c < 0:
        raise Exception('file not found!')
    per = 100.0 * a * b / c
    if per > 100:
        per = 100
        print '下载完成  --总共:%d' % (c)


try:
    print '-step1: 创建默认临时目录------'
    is_default_path_exists = os.path.exists(default_path)
    if is_default_path_exists:
        print '--'+default_path + ' 已存在'
    else:
        print '--创建:'+default_path
        os.mkdir(default_path)
        os.chmod(default_path, stat.S_IRWXU | stat.S_IRWXG | stat.S_IRWXO)

    local = default_path+'/'+artifact_name
    print ''
    print '-step2: 下载artifact------'
    print '--开始下载:'+artifact_name
    re_try = 3
    try:
        schema_index = download_url.find('//')
        if schema_index != -1:
            schema = download_url[:schema_index]
            content_index = download_url.find('@')
            if content_index != -1:
                content = download_url[content_index + 1:]
                print '下载地址为: ' + schema + '//' + content
            else:
                print '下载地址为: '+ download_url
        else:
            print '下载地址为: '+ download_url
        re_try = re_try-1
        urllib.urlretrieve(download_url, local, callback)
    except Exception as e:
        if re_try != 0:
            urllib.urlretrieve(download_url, local, callback)
        else:
            raise Exception('下载失败！')
    print ''
    print '-step3: 解压artifact------'
    zip_path = default_path+'/'+(artifact_name.split('.zip'))[0]
    artifact_file = zipfile.ZipFile(local)
    if not os.path.exists(zip_path):
        os.mkdir(zip_path)
        os.chmod(zip_path, 0o777)
    else:
        shutil.rmtree(zip_path)
    print '目录结构为:'
    for info in artifact_file.infolist():
        artifact_file.extract(info, zip_path)
        unix_attributes = info.external_attr >> 16
        target = os.path.join(zip_path, info.filename)
        if unix_attributes:
            os.chmod(target, unix_attributes)
        print target
    artifact_file.close()

    print ''
    print '-step4: 校验 appspec.yml-----'
    f = open(zip_path+'/appspec.yml', 'r')
    lines = f.readlines()
    for line in lines:
        if 'location' in line:
            path = line.split(':')[1].replace(' ','').replace('\n','').replace('\r','')
            p = os.path.join(zip_path,path)
            if os.path.exists(p):
                    os.chmod(p,0o777)
                    print '校验: '+p+' [OK]'
            else:
                    raise Exception('脚本文件:'+p+' 不存在!')
    f.close()

except Exception as e:
    print 'error:', e
    raise Exception('初始化环境失败！')

print ''
print '----------已完成初始化FIT2CLOUD部署环境----------'

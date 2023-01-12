# coding=utf-8
import os
import sys
import shutil

if sys.getdefaultencoding() != 'utf-8':
    reload(sys)
    sys.setdefaultencoding('utf-8')

print '\n----------开始清理FIT2CLOUD部署环境----------'
print ''
try:
    default_path = '${default_path}'
    artifact_name = '${artifact_name}'

except Exception as e:
    print('args error', e)
    raise Exception('参数错误')

try:
    local_dir = default_path + '/' + artifact_name
    if os.path.exists(local_dir):
        print('--清理' + local_dir + ' ---')
        os.remove(local_dir)
    zip_path = default_path + '/' + (artifact_name.split('.zip'))[0]
    if os.path.exists(zip_path):
        print('--清理' + zip_path + '---')
        shutil.rmtree(zip_path)

except Exception as e:
    print('cleanEnv error', e)
    raise Exception('清理失败')
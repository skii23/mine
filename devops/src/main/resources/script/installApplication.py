# coding=utf-8

import glob
import os
import subprocess
import sys

if sys.getdefaultencoding() != 'utf-8':
    reload(sys)
    sys.setdefaultencoding('utf-8')

print('\n')
print('----------开始执行安装脚本----------')
print('')

rootPath = '${rootPath}'
windows = ${windows}
sources = [${sources}]
destinations = [${destinations}]
permissions = ${permissions}

try:
    def format_path(string):
        if windows:
            # 由于ansible-api发送字符串转义有问题，暂时先用ASCII码代替双引号
            return chr(34) + string + chr(34)
        else:
            return string.replace(' ', r'\ ')


    def do_copy(source, destination):
        if not destination.endswith(os.sep):
            destination += os.sep
        real_path = source
        if real_path.endswith('/'):
            real_path += '*'
        for file_item in glob.glob(real_path):
            print(file_item.rstrip() + ' ----------> ' + destination + file_item.split(os.path.sep)[-1].rstrip())
        if not os.path.exists(destination):
            os.makedirs(destination)
        if windows:
            return subprocess.Popen(['powershell','cp','-r','-force',format_path(source),format_path(destination)])
        else:
            return subprocess.Popen('cp -rpf ' + format_path(real_path) + ' ' + format_path(destination), shell=True)


    print('-step1: 检出安装路径-------------')
    if len(sources) != len(destinations):
        raise Exception('source files conflict with target folders')
    for index, source in enumerate(sources):
        print('源路径为: ' + source)
        print('安装路径为: ' + destinations[index] + '\n')

    print('-step2: 拷贝文件-------------')
    cp_groups = []
    for index, source in enumerate(sources):
        try:
            print('拷贝源文件(夹): ' + source + ' 到 ' + destinations[index])
            cp_groups.append(do_copy(source, destinations[index]))
            print('')
        except Exception as e:
            print('copy file error : ', e)
            raise Exception('copy files failed')
    
    print('-step2-1: 等待批量拷贝文件结束-------------')
    for do_cp in cp_groups:
        try:
            if isinstance(do_cp, subprocess.Popen):
                do_cp.communicate()
        except Exception as e:
            print('wait copy file error : ', e)
            raise Exception('wait copy files failed')
    
    print('-step2-2: 批量更新权限-------------')
    permission_groups = []
    for permission in permissions:
        try:
            process = None
            for item in glob.glob(permission['object']):
                command = ''
                if 'owner' in permission.keys():
                    if 'group' in permission.keys():
                        command = 'chown -R ' + permission['owner'] + ':' + permission['group'] + ' ' + item.rstrip()
                    else:
                        command = 'chown -R ' + permission['owner'] + ' ' + item.rstrip()
                    print(command)
                    permission_groups.append(subprocess.Popen(command, shell=True))
                if 'mode' in permission.keys():
                    command = 'chmod -R ' + permission['mode'] + ' ' + item.rstrip()
                    print(command)
                    permission_groups.append(subprocess.Popen(command, shell=True))
        except Exception as e:
            print('change permission failed',e)
            raise Exception('change permission failed')
    
    print('-step2-3: 等待批量更新权限结束-------------')
    for do_permission in permission_groups:
        try:
            if isinstance(do_permission, subprocess.Popen):
                do_permission.communicate()
        except Exception as e:
            print('wait change permission failed : ', e)
            raise Exception('change permission failed')

except Exception as e:
    print('error:', e)
    raise Exception('----------执行安装脚本失败！----------')

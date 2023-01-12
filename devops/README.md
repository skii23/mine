# FIT2CLOUD 2.0 DevOps
 云管devops模块
# maven
版本：maven-3.6.3-openjdk-8
默认maven地址使用：
http://nexus3.apps.test1.dc-fz.paas.cisc.cn/repository/maven-public/

## Build

基础仓库：registry.test1.dc-fz.paas.cisc.cn/xyzq-cmp/

### 构建发布版本：
```
mvn clean package -Dmaven.test.skip=true
```
## 镜像构建


```
docker build -t registry.test1.dc-fz.paas.cisc.cn/xyzq-cmp/devops:dev .
```
# 本地开发调试

待补充
# Docker环境调试

## 前置条件
1. 环境按照docker（window 可以按照wsl2的Linux环境，再安装docker服务）；
2. 安装docker-compose命令；
3. 配置镜像地址，确保能拉取镜像仓库registry.test1.dc-fz.paas.cisc.cn/xyzq-cmp；
4. 依赖mysql服务，端口占用本地3306，默认镜像运行mysql，如需外部mysql，需要修改docker-compose.yml；
5. devops占用80端口，具体看docker-compose.yml配置；

## 调试命令

1. 默认mysql端口为3306
2. devops地址为：http://127.0.0.1
3. 默认开发镜像已经构建，可直接从registry.test1.dc-fz.paas.cisc.cn/xyzq-cmp拉取；

```
# 启动服务
docker-compose up -d

# 查看日志
docker-compose logs -f devops

# 编译开发版本
mvn clean package -Dmaven.test.skip=true -Dmaven.antrun.skip=true

# 重启devops，自动加载最新的开发版本
docker-compose restart devops

# 卸载
docker-compose down -v
```
## mysql开发镜像构建
```
docker build -t registry.test1.dc-fz.paas.cisc.cn/xyzq-cmp/mysql:5.7.25-dev -f Dockerfile.mysql-dev .
docker run -it registry.test1.dc-fz.paas.cisc.cn/xyzq-cmp/mysql:5.7.25-dev sh
```
## devops开发镜像
```
# 编译开发版本
mvn clean package -Dmaven.test.skip=true -Dmaven.antrun.skip=true

docker build -t registry.test1.dc-fz.paas.cisc.cn/xyzq-cmp/devops:dev -f Dockerfile.dev .

docker run -it registry.test1.dc-fz.paas.cisc.cn/xyzq-cmp/devops:dev sh
```

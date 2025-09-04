# Flink Docker 本地部署

## 独立网络创建

```
docker network create flink-network
```

## container 启动

### JobManager 启动

```
docker run \
  -itd \
  --name=jobmanager \
  --publish 8081:8081 \
  --network flink-network \
  --env FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" \
  flink:1.20.2-scala_2.12-java17 jobmanager 
```

### TaskManager 启动

```
docker run \
  -itd \
  --name=taskmanager \
  --network flink-network \
  --env FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" \
  flink:1.20.2-scala_2.12-java17 taskmanager 
```

## 拷贝jobManager和taskManager配置文件

```
mkdir -p ~/docker-data/flink/
cd ~/docker-data/flink/
docker cp jobmanager:/opt/flink/conf ./JobManager/
docker cp taskmanager:/opt/flink/conf ./TaskManager/
```

## 修改jobManager和taskManager配置文件

```
#修改 JobManager/flink-conf.yaml web 端口号为 18081
rest.port: 18081
#修改 TaskManager/flink-conf.yaml 容器任务槽为 5
taskmanager.numberOfTaskSlots: 5
```

## 删除已启动container

```
docker rm -f taskmanager
docker rm -f jobmanager
```

## 重新挂载并启动容器

### JobManager 启动

```
docker run \
-itd  \
-v ./docker-data/flink/JobManager/:/opt/flink/conf/ \
--name=jobmanager \
--publish 18081:18081 \
--env FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager" \
--network flink-network flink:1.20.2-scala_2.12-java17 jobmanager
```

### TaskManager 启动

```
docker run \
-itd  \
-v ./docker-data/flink/TaskManager/:/opt/flink/conf/ \
--name=taskmanager --network flink-network \
--env FLINK_PROPERTIES="jobmanager.rpc.address: jobmanager"  \
flink:1.20.2-scala_2.12-java17 taskmanager
```


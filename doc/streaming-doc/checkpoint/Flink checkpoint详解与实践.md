## Flink checkpoint详解与实践
#### 目录
1、checkpoint简介

2、checkpoint配置

3、checkpoint存储检查点(State Backend)

4、checkpoint重启策略

5、checkpoint恢复

checkpoint demo传送门：https://github.com/ash-ali/MyFlink-doc/blob/master/src/main/java/com/example/demo/streaming/checkpoint/StreamingCheckPoint.java
#### 1、checkpoint简介
>Flink的每个函数和算子都可以是有状态(state)的，有状态函数在各个元素/事件的处理中存储数据。为了使状态容错，Flink需要检查状态，即引入了checkpoint机制。checkpoint允许Flink恢复Stream中的状态和位置，从而为应用程序提供与无故障执行相同的语义。

所以综上，checkpoint是为了保证state的容错性。checkpoint是Flink容错机制最核心的功能。



#### 2、checkpoint配置
其配置可参考：https://ci.apache.org/projects/flink/flink-docs-release-1.6/dev/stream/state/checkpointing.html



#### 3、checkpoint存储检查点(State Backend)
首先在默认情况下，state是存储在taskmanager的内存中，checkpoint存储在jobmanage的内存中，但是存储位置都是可设置的，通过State Backend。

##### Flink提供三种State Backend
如果没有配置，系统将使用MemoryStateBackend
- MemoryStateBackend（存储于内存）
- FsStateBackend（存储于文件，如hdfs分布式文件系统）
- RocksDBStateBackend（存储于RocksDB）

在实际应用中，大多使用FsStateBackend或者RocksDBStateBackend，因为基于内存不利于数据的持久化


##### 配置State Backend

###### 1、设置每个任务的State Backend
```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
env.setStateBackend(new FsStateBackend("hdfs://namenode:40010/flink/checkpoints"));
或者new MemoryStateBackend()
或者new RocksDBStateBackend(filebackend, true);添加RocksDB依赖
```

###### 2、设置默认的State Backend
配置flink-conf.yaml

配置文件中的示例部分可能如下所示：
```java
# The backend that will be used to store operator state checkpoints

state.backend: filesystem


# Directory for storing checkpoints

state.checkpoints.dir: hdfs://namenode:40010/flink/checkpoints
```
###### 3、通过传送门的demo，演示结果可参考Flink Web界面checkpoint配置认证

![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/checkpoint基本信息.png)


![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/State%20Backend信息.png)




#### 4、checkpoint重启策略
默认重启策略是通过Flink的配置文件设置的flink-conf.yaml。配置参数restart-strategy定义采用的策略

##### 常用的重启策略如下
![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/重启策略.png)


- 如果没有使用checkpoint，则使用"无重启"策略（no restart）
- 如果激活了checkpoint且尚未配置重启策略，则使用固定延迟策略（fixed-delay），其中 Integer.MAX_VALUE 参数是尝试重启次数

重启策略配置可参考：https://ci.apache.org/projects/flink/flink-docs-release-1.6/dev/restart_strategies.html



#### 5、checkpoint恢复
可通过以下命令将最近的checkpoint点进行恢复
```java
./flink run -s hdfs://master:9000/flink/checkpoints/1ca2cf3c8d9c82908800a0bda76b0558/chk-227/_metadata flink-job.jar
```


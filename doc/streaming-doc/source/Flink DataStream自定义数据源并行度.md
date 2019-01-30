## Flink DataStream自定义数据源并行度

#### 什么是并行度(parallel)
> 可以简单理解为一个Flink程序由多个任务组成，一个任务由多个并行的实例(线程)来执行，一个任务的并行实例(线程)数目就被称为该任务的并行度。

Flink官方文档中关于并行源是这样使用的
>Sources are where your program reads its input from. You can attach a source to your program by using StreamExecutionEnvironment.addSource(sourceFunction). Flink comes with a number of pre-implemented source functions, but you can always write your own custom sources by implementing the SourceFunction for non-parallel sources, or by implementing the ParallelSourceFunction interface or extending the RichParallelSourceFunction for parallel sources.

也就是说
- 实现SourceFunction其数据源的并行度只能为1，称之为非并行源
    - 传送门：https://github.com/ash-ali/MyFlink-doc/blob/master/src/main/java/com/example/demo/streaming/source/StreamingOneParalleSource.java
    - 演示结果
    ![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/非并行源演示结果.png)
    
- 实现ParallelSourceFunction或者继承RichParallelSourceFunction可以自定义并行度，称之为并行源
    - 传送门(实现ParallelSourceFunction)：https://github.com/ash-ali/MyFlink-doc/blob/master/src/main/java/com/example/demo/streaming/source/StreamingManyParalleSource.java
    - 演示结果
    ![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/并行源演示结果.png)

    


#### 实现ParallelSourceFunction与继承RichParallelSourceFunction区别分析
通过源码结果分析
ParallelSourceFunction继承于SourceFunction
    
    实现run与cancel
    
RichParallelSourceFunction继承于AbstractRichFunction并且实现ParallelSourceFunction
  
    实现run、cancel、open、close（open与close的主要功能是获取(打开)资源链接或关闭资源链接，比如打开数据库等操作，如果放在run中则会每次都去打开一次，而放在open中则不会每次打开，从而减少资源浪费。）

## DataStream与DataSet概述
>flink具有特殊类DataStream与DataSet，这两者是flink中主要的数据源，根据数据源可分为有界源和无界源，而DataStream则是无界源，DataSet是有界源，也就是说DataStream常用于流处理，而DataSet用于批处理。

---

### DataStream API 

- DataStream Sources（数据来源）
- DataStream Transformations（数据转化）
- DataStream Sinks（数据的目的地）

#### DataStream Sources（数据来源）
>大多实际应用环境都以自定义数据源为主,常常从kafka中读取数据，flink官方也有专门的kafka连接器提供
- 基于文件
- 基于Socket
- 基于集合
- 自定义


#### DataStream Transformations（数据转化）
> 官方提供许多处理的方法
- map：输入一个数据，返回一个数据，中间可做相关处理
```java
DataStream<Integer> dataStream = //...
dataStream.map(new MapFunction<Integer, Integer>() {
    @Override
    public Integer map(Integer value) throws Exception {
        return 2 * value;
    }
});
```
- flatmap：输入一个数据，返回零个、一个、多个数据，中间可做相关处理
```java
dataStream.flatMap(new FlatMapFunction<String, String>() {
    @Override
    public void flatMap(String value, Collector<String> out)
        throws Exception {
        for(String word: value.split(" ")){
            out.collect(word);
        }
    }
});
```
- filter：对数据进行过滤
```java
dataStream.filter(new FilterFunction<Integer>() {
    @Override
    public boolean filter(Integer value) throws Exception {
        return value != 0;
    }
});
```
- keyBy:根据指定的key进行分区

注意：以下类型无法作为key

1、即使它是pojo类型，但不覆盖hashcode方法并依赖于Object.hashcode()方法

2、它是任何类型的数组
```java
dataStream.keyBy("someKey") // Key by field "someKey"
dataStream.keyBy(0) // Key by the first element of a Tuple(指定tuple中的第一个元素作为分组key)
```
等等具体可查阅官方文档：https://ci.apache.org/projects/flink/flink-docs-release-1.7/dev/stream/operators/index.html


#### DataStream Sinks（数据的目的地）
> Sink将处理后的数据转发到文件、sockets、外部系统(如kafka、redis)、print()打印，flink有许多已经封装好的内置方法。在实际应用中经常使用addSink自定义将数据输出到外部系统

- writeAsText()：按字符串顺序写入，这些字符串通过调用每个元素的toString()方法来获取
- writeAsCsv(...)：将元组写为逗号分隔值文件。行和字段分隔符是可配置的
- print()/printToErr(): 打印每个元素的toString()方法
- writeUsingOutputFormat()：自定义文件输出的方法和基类，支持自定义对象到字节的转换。
- writeToSocket：根据SerializationSchema写入元素到socket
- addSink：自定义输出，如kafka、redis

注意：write*()方法主要是用于调试的目的，它们并没有参与flink的checkpoint，这就意味着这些函数是at-least-once(至少一次)的语义，通过addSink()方法的自定义可以实现flink的exactly-once(精确一次)(可靠)的语义

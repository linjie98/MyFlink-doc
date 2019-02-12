## Flink Time介绍
>Flink在流式处理程序中支持不同的时间概念，如下三种时间概念

- Event Time
- Processing Time
- Ingestion Time

### Event Time
Event Time是每个事件在生产设备上产生的时间，它通常在事件进入Flink之前被记录，并且可以从每个记录中提取该事件时间戳，在Event Time中，时间取决于数据，而根其他没有关系。Event Time程序必须指定如何生成Event Time Watermarks(事件时间水印)。

---

### Processing Time
Processing Time是事件被计算处理时机器的系统时间。

---

### Ingestion Time
Ingestion Time是事件进入Flink的时间。它在概念上处于Event Time和Processing Time之间。
- Ingestion Time与Processing Time相比，比Processing Time更加复杂一些，Ingestion Time提供更加可预测的结果，因为Ingestion Time使用稳定的时间戳（在源处分配一次），所以对事件的不同窗口操作将引用相同的时间戳。而在Processing Time每个窗口算子可以将事件分配给不同的窗口（基于机器系统时间和到达延迟）。
- Ingestion Time与Event Time相比，Ingestion Time程序无法处理任何无序事件或延迟数据，但程序不必指定如何生成Watermarks。在Flink中Ingestion Time与Event Time非常相似，但Ingestion Time具有自动分配时间戳和自动生成Watermarks功能。

---

### Event Time / Processing Time / Ingestion Time流程
![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/countwindowTime.png)

---

##### 根据上图的流程可以分为以下步骤
1、事件产生，可以理解为数据的产生时间(如获取到爬虫数据并带上系统时间)

2、将产生的数据放到Message Queue(如kafka)

3、Flink需要去消息队列中消费数据，通过source取数据，这时会有Ingestion Time，数据进入Flink的时间

4、数据进入Flink后，会被进行窗口操作，这时会有Processing Time（处理时间）


### 设置Time类型
```java
final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);

// alternatively:
// env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime);
// env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

DataStream<MyEvent> stream = env.addSource(new FlinkKafkaConsumer09<MyEvent>(topic, schema, props));

stream
    .keyBy( (event) -> event.getUser() )
    .timeWindow(Time.hours(1))
    .reduce( (a, b) -> a.add(b) )
    .addSink(...);
```
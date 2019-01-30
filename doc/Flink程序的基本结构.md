## Flink程序的基本结构

1、Obtain an execution environment(获得一个flink运行环境)

2、Load/create the initial data(加载或创建一个初始数据源)

3、Specify transformations on this data(转化处理此数据源)

4、Specify where to put the results of your computations(指定放置此数据源的位置)

5、Trigger the program execution(触发程序执行)

#### 1、Obtain an execution environment(获得一个flink运行环境)
通常通过getExecutionEnvironment()来创建一个本地环境
```java
StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
```

#### 2、Load/create the initial data(加载或创建一个初始数据源)
例如获取自定义数据源
```java
DataStream<Long> text = env.addSource(...);
```

#### 3、Specify transformations on this data(转化处理此数据源)
```java
DataStream<String> input = ...;

DataStream<Integer> parsed = input.map(new MapFunction<String, Integer>() {
    @Override
    public Integer map(String value) {
        return Integer.parseInt(value);
    }
});
```

#### 4、Specify where to put the results of your computations(指定放置此数据源的位置)
```java
writeAsText(String path)

print()


``` 

#### 5、 Trigger the program execution(触发程序执行)
```java
env.execute()
```
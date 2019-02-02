## Flink累加器
>Flink累加器是具有add operation(add操作)和final accumulated result(最终累计结果)的简单构造，得到最终结果需要在作业结束后才可获得

#### Counters(计数器)
>最直接的累加器是一个计数器，可以使用Accumulator.add(V value)方法来递增它。在工作结束时，Flink将汇总(合并)所有部分结果，并将结果发送给client。

#### Flink当前的内置累加器
- IntCounter、LongCounter、DoubleCounter
- Histogram(离散数量的区间的直方图实现。在内部，它只是一个从Integer到Integer的映射。您可以使用它来计算值的分布，例如字数统计程序的每行字数的分布。)

#### 如何使用累加器

##### 1、创建累加器对象(此处为计数器)
```java
private IntCounter numLines = new IntCounter();
```    

##### 2、注册累加器对象，通常在rich function的open()方法中
```java
getRuntimeContext().addAccumulator("num-lines", this.numLines);
```

##### 3、使用累加器，可以在任何运算位置，包括open、close方法中
```java
this.numLines.add(1);
```

##### 4、结果将存储在JobExecutionResult从execute()执行环境的方法返回的对象中
```java
myJobExecutionResult.getAccumulatorResult("num-lines")
```


#### 注意点
- 1、使用累加器需要使用Rich Function
```java
stringDataSource.map(new RichMapFunction<String, String>(){}
```
- 2、使用累加器不能使用print()方法输出，因为最后需要execute(),所以测试阶段可以尝试写入文件
```java
        JobExecutionResult myJobExecutionResult = null;
        try {
            myJobExecutionResult = env.execute("StreamingCounter任务执行");
            Integer countresult = myJobExecutionResult.getAccumulatorResult("num-lines");
            System.out.println(countresult);
        } catch (Exception e) {
            e.printStackTrace();
        }
```

#### Demo传送门
https://github.com/ash-ali/MyFlink-doc/blob/master/src/main/java/com/example/demo/streaming/StreamingCounter.java
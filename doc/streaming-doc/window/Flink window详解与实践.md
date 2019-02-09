## Flink Window详解与实践
### 目录
##### 1、窗口类型
##### 2、窗口功能(窗口增量/全量聚合)
##### 3、自定义window

---


>window是Flink处理流数据的核心，window将流拆分成有限大小的"桶",我们可以在每个桶上进行计算。举个例子，比如现在我们需要对我们的系统每10分钟汇总出一份在过去10分钟内有多少用户浏览了我们的系统，这就需要我们定义窗口，用来收集最近10分钟内的数据，并对这个窗口进行计算。

>window可以是由时间驱动的(例如：每30秒）或数据驱动（例如：每100个元素）

![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/窗口驱动.png)


---

### 窗口类型
-  tumbling windows (no overlap) 滚动窗口(没有重叠)
-  sliding windows (with overlap) 滑动窗口(有重叠)
-  session windows (punctuated by a gap of inactivity) 会话窗口(由不同的时间间隙打断)


#### 1、滚动窗口(没有重叠)
>滚动窗口指定窗口的窗口大小，它具有固定的大小，不重叠。例如：指定大小为5分钟的滚动窗口，当前窗口将被计算，并且每5分钟将启动一个新窗口，可以看如下官方给的示例图

![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/滚动窗口.png)


#### 2、滑动窗口(有重叠)
>窗口大小由窗口大小参数配置，并且附加了一个窗口滑动参数，该参数控制滑动窗口的启动频率。因此窗口滑动参数小于窗口大小参数时，滑动窗口可以重叠，在这只情况下，数据被分配给多个窗口进行计算。例如：大小为10分钟的窗口每5分钟滑动一次，这样你每隔5分钟就会得到一个窗口，其中包含过去10分钟内到达的事件，可以看如下官方给的示例图

![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/滑动窗口.png)


#### 3、session windows (punctuated by a gap of inactivity) 会话窗口(由不同的时间间隙打断)
>会话指的是活动阶段，其前后都是非活动阶段，例如用户与网站进行一系列交互(活动阶段)之后，关闭浏览器或者不再交互(非活动阶段)。会话需要有自己的处理机制，因为它们通常没有固定的持续时间(有些30秒就结束，有些则长达一小时)，或者没有固定的交互次数(有些可能是3次点击后购买，另一些可能是40次点击缺没有购买)。

>在Flink中，会话窗口由超时时间设定，即希望等待多久才认为会话已经结束。举个例子，以下代码表示，如果用户处于非活动状态长达5分钟，则认为会话结束

```java
stream.window(SessionWindows.withGap(Time).minutes(5));
```

如下是官方给的示例图

![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/会话窗口.png)

---

### 窗口功能(窗口增量/全量聚合)
>在定义好窗口后，我们需要指定要在每个窗口上执行的计算，这就是窗口函数的职责，窗口函数用于在系统确定窗口准备好后进行处理每个窗口的元素

#### 1、窗口函数如下4种
- ReduceFunction
- AggregateFunction
- FoldFunction
- ProcessWindowFunction

**ReduceFunction和AggregateFunction用于递增的聚合，可以理解为窗口每进入一条数据就计算一次。**

**ProcessWindowFunction用于全量的聚合，等到该窗口所有数据到齐了之后，再进行聚合计算，比如计算该窗口中的top3。**

#### 2、窗口函数示例
##### a、ReduceFunction
```java
DataStream<Tuple2<String, Long>> input = ...;

input
    .keyBy(<key selector>)
    .window(<window assigner>)
    .reduce(new ReduceFunction<Tuple2<String, Long>> {
      public Tuple2<String, Long> reduce(Tuple2<String, Long> v1, Tuple2<String, Long> v2) {
        return new Tuple2<>(v1.f0, v1.f1 + v2.f1);
      }
    });
```

##### b、AggregateFunction
需要实现AggregateFunction，这个示例为了求平均值
```java
/**
 * The accumulator is used to keep a running sum and a count. The {@code getResult} method
 * computes the average.
 */
private static class AverageAggregate
    implements AggregateFunction<Tuple2<String, Long>, Tuple2<Long, Long>, Double> {
  @Override
  public Tuple2<Long, Long> createAccumulator() {
    return new Tuple2<>(0L, 0L);
  }

  @Override
  public Tuple2<Long, Long> add(Tuple2<String, Long> value, Tuple2<Long, Long> accumulator) {
    return new Tuple2<>(accumulator.f0 + value.f1, accumulator.f1 + 1L);
  }

  @Override
  public Double getResult(Tuple2<Long, Long> accumulator) {
    return ((double) accumulator.f0) / accumulator.f1;
  }

  @Override
  public Tuple2<Long, Long> merge(Tuple2<Long, Long> a, Tuple2<Long, Long> b) {
    return new Tuple2<>(a.f0 + b.f0, a.f1 + b.f1);
  }
}

DataStream<Tuple2<String, Long>> input = ...;

input
    .keyBy(<key selector>)
    .window(<window assigner>)
    .aggregate(new AverageAggregate());
```

##### c、ProcessWindowFunction
ProcessWindowFunction获取包含窗口所有元素的Iterable，以及可访问时间和状态信息的Context对象，这使其能够提供比其他窗口函数更多的灵活性。这是以性能和资源消耗为代价的，因为元素不能以递增方式聚合，而是需要在内部进行缓冲，直到认为窗口已准备好进行处理。

**如下是ProcessWindowFunction抽象类，可以看出其中的对象信息**

```java
public abstract class ProcessWindowFunction<IN, OUT, KEY, W extends Window> implements Function {

    /**
     * Evaluates the window and outputs none or several elements.
     *
     * @param key The key for which this window is evaluated.
     * @param context The context in which the window is being evaluated.
     * @param elements The elements in the window being evaluated.
     * @param out A collector for emitting elements.
     *
     * @throws Exception The function may throw exceptions to fail the program and trigger recovery.
     */
    public abstract void process(
            KEY key,
            Context context,
            Iterable<IN> elements,
            Collector<OUT> out) throws Exception;

   	/**
   	 * The context holding window metadata.
   	 * 保存窗口元数据的上下文
   	 */
   	public abstract class Context implements java.io.Serializable {
   	    /**
   	     * Returns the window that is being evaluated.
   	     * 返回正在进行计算的窗口
   	     */
   	    public abstract W window();

   	    /** Returns the current processing time.
   	     *  返回当前的处理时间
   	     * */
   	    public abstract long currentProcessingTime();

   	    /** Returns the current event-time watermark.
   	     *  返回当前事件时间水印
   	     * */
   	    public abstract long currentWatermark();

   	    /**
   	     * State accessor for per-key and per-window state.
   	     * 
   	     * 每个键和每个窗口状态的访问器
   	     *
   	     * <p><b>NOTE:</b>If you use per-window state you have to ensure that you clean it up
   	     * by implementing {@link ProcessWindowFunction#clear(Context)}.
   	     */
   	    public abstract KeyedStateStore windowState();

   	    /**
   	     * State accessor for per-key global state.
   	     * 每个键全局状态的状态访问器
   	     */
   	    public abstract KeyedStateStore globalState();
   	}

}
```

**如下是ProcessWindowFunction的使用,该示例作用是ProcessWindowFunction对窗口中的元素进行计数的情况。此外，窗口功能将有关窗口的信息添加到输出**
```java
DataStream<Tuple2<String, Long>> input = ...;

input
  .keyBy(t -> t.f0)
  .timeWindow(Time.minutes(5))
  .process(new MyProcessWindowFunction());

/* ... */

public class MyProcessWindowFunction 
    extends ProcessWindowFunction<Tuple2<String, Long>, String, String, TimeWindow> {

  @Override
  public void process(String key, Context context, Iterable<Tuple2<String, Long>> input, Collector<String> out) {
    long count = 0;
    for (Tuple2<String, Long> in: input) {
      count++;
    }
    out.collect("Window: " + context.window() + "count: " + count);
  }
}
```
##### d、FoldFunction
注意：fold()不能与会话窗口或其他可合并窗口一起使用。
```java

DataStream<Tuple2<String, Long>> input = ...;

input
    .keyBy(<key selector>)
    .window(<window assigner>)
    .fold("", new FoldFunction<Tuple2<String, Long>, String>> {
       public String fold(String acc, Tuple2<String, Long> value) {
         return acc + value.f1;
       }
    });
```

上面的示例将所有输入Long值附加到最初为空String。

---

### 自定义window
其实自定义window和Flink本身封装好的timewindow/timewindowall这些原理相同，可以通过如下选择Keyed/Non-Keyed Windows来进行自定义封装

![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/自定义窗口.png)

也可查看Flink封装好的window，其实也是通过window来实现的(例如countwindow/timewindow)
```java
	// ------------------------------------------------------------------------
	//  Windowing
	// ------------------------------------------------------------------------

	/**
	 * Windows this {@code KeyedStream} into tumbling time windows.
	 *
	 * <p>This is a shortcut for either {@code .window(TumblingEventTimeWindows.of(size))} or
	 * {@code .window(TumblingProcessingTimeWindows.of(size))} depending on the time characteristic
	 * set using
	 * {@link org.apache.flink.streaming.api.environment.StreamExecutionEnvironment#setStreamTimeCharacteristic(org.apache.flink.streaming.api.TimeCharacteristic)}
	 *
	 * @param size The size of the window.
	 */
	public WindowedStream<T, KEY, TimeWindow> timeWindow(Time size) {
		if (environment.getStreamTimeCharacteristic() == TimeCharacteristic.ProcessingTime) {
			return window(TumblingProcessingTimeWindows.of(size));
		} else {
			return window(TumblingEventTimeWindows.of(size));
		}
	}

	/**
	 * Windows this {@code KeyedStream} into sliding time windows.
	 *
	 * <p>This is a shortcut for either {@code .window(SlidingEventTimeWindows.of(size, slide))} or
	 * {@code .window(SlidingProcessingTimeWindows.of(size, slide))} depending on the time
	 * characteristic set using
	 * {@link org.apache.flink.streaming.api.environment.StreamExecutionEnvironment#setStreamTimeCharacteristic(org.apache.flink.streaming.api.TimeCharacteristic)}
	 *
	 * @param size The size of the window.
	 */
	public WindowedStream<T, KEY, TimeWindow> timeWindow(Time size, Time slide) {
		if (environment.getStreamTimeCharacteristic() == TimeCharacteristic.ProcessingTime) {
			return window(SlidingProcessingTimeWindows.of(size, slide));
		} else {
			return window(SlidingEventTimeWindows.of(size, slide));
		}
	}

	/**
	 * Windows this {@code KeyedStream} into tumbling count windows.
	 *
	 * @param size The size of the windows in number of elements.
	 */
	public WindowedStream<T, KEY, GlobalWindow> countWindow(long size) {
		return window(GlobalWindows.create()).trigger(PurgingTrigger.of(CountTrigger.of(size)));
	}

	/**
	 * Windows this {@code KeyedStream} into sliding count windows.
	 *
	 * @param size The size of the windows in number of elements.
	 * @param slide The slide interval in number of elements.
	 */
	public WindowedStream<T, KEY, GlobalWindow> countWindow(long size, long slide) {
		return window(GlobalWindows.create())
				.evictor(CountEvictor.of(size))
				.trigger(CountTrigger.of(slide));
	}
```
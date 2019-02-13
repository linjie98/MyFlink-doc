## EventTime和Watermarks关于有序和序流的理解

>支持EventTime的流处理需要能够获取EventTime的处理进度。对于一个小时的时间窗口，当EventTime处理超过了这个小时的时候需要被通知，以便operator可以关闭该小时的窗口处理。

>事件时间能够独立于处理时间进行处理。例如，在一个程序中，当前operator处理的事件时间可能轻微落后于当前的处理时间，然而两者都已相同的速度进行处理。在另一方面，一个程序可能在数秒之内处理完几周的事件时间，通过快速转发缓存在kafka等消息队列中的消息。（其实主要考虑的是实时系统中，由于各种原因造成的延时，造成某些消息发到flink的时间延时于事件产生的时间，如果基于事件时间的时间窗，可能该时间窗采集了一小时之后还需要等待几分钟，才能接收到这条延时的事件，因此需要检测当前时间窗口的处理进度，可能等待一段时间是必要的，但是不可能无限等待某些延时的时间。）

**所以Flink中Watermark就是检测事件时间处理进度的机制**

---

### Watermark作用
Watermark主要用于处理乱序数据，在流处理中，从数据的产生，到source，再到window处理，中间需要花费一些时间。流到window处理的数据都是按照数据产生的时间顺序来的，但是不能排除网络延迟等原因导致乱序数据的产生。比如我们要计算5之前的所有数，但由于乱序，可能4一直没来，但我们又不可能无限期的等下去，所以我们必须利用Watermark机制来保证在一个特定的时间之后，必须触发window来进行计算。

---

### 单并行度有序流
![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/watermark有序流.png)
此为有序流，每一个方框代表事件(数据)，里面的数字代表时间戳(有序)，w(11)代表Flink发送的watermark，也就是要计算11之前的数据了，不能再等其他数据了，然后w(20)就要计算20之前的数据了，不能再等之后的数据了。所以其实在有序流中watermark的作用并不大，它只是一个周期性标记。

---

### 单并行度无序流
![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/watermark无序流.png)
此为无序流，可以看到，事件时间戳的顺序是无序的，这时候watermark就非常重要了，比如到w(11)时候，会计算11之前的数据9、11、7，然后将12、15放到下一个窗口计算，然后到w(17),就计算17、12、17、14、12、15这些时间戳的数据。

---
### 多并行度流的Watermark
![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/多并行度流的watermark.png)
在多并行度流的环境下，每个线程都会生成watermark，然后在最后操作window的时候，第一次需要使用watermark值最小的来使用，如图29和14应该先选择14，把14之前的数据先算完，然后下一次再用29。

---

### 生成Watermark
#### 生成Watermark的时间点
1、会在source数据后，立刻生成watermark

2、在source数据后，进行一些处理(map、filter)后，再生成watermark

#### 生成方式
1、With Periodic Watermarks
- 实现AssignerWithPeriodicWatermarks 分配时间戳并定期生成watermark(周期性)
- 可定义最大乱序时间 
2、With Punctuated Watermarks
- 实现AssignerWithPunctuatedWatermarks 要在某个事件表明可能生成新watermark时生成watermark(事件触发)

#### With Periodic Watermarks应用解析
```java
/**
 * This generator generates watermarks assuming that elements arrive out of order,
 * but only to a certain degree. The latest elements for a certain timestamp t will arrive
 * at most n milliseconds after the earliest elements for timestamp t.
 */
public class BoundedOutOfOrdernessGenerator implements AssignerWithPeriodicWatermarks<MyEvent> {
    
    //允许数据的最大乱序时间
    private final long maxOutOfOrderness = 3500; // 3.5 seconds
    //当前数据的最大时间戳
    private long currentMaxTimestamp;

    //从事件中抽取时间戳
    @Override
    public long extractTimestamp(MyEvent element, long previousElementTimestamp) {
        long timestamp = element.getCreationTime();
        currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp);
        return timestamp;
    }

    //获取当前的watermark
    @Override
    public Watermark getCurrentWatermark() {
        // return the watermark as current highest timestamp minus the out-of-orderness bound
        // 当前最大时间戳-允许数据的最大乱序时间
        return new Watermark(currentMaxTimestamp - maxOutOfOrderness);
    }
}
```
关于return new Watermark(currentMaxTimestamp - maxOutOfOrderness);还是可以根据下图来理解
![image](https://github.com/ash-ali/MyFlink-doc/blob/master/img/watermark无序流.png)
前5条数据，当前最大时间戳是15，可以看到w(11),所以其允许最大乱序时间是4，当然其中可能会有网络原因有所延迟现象。

watermark测试案例可参考该篇博客：https://blog.csdn.net/lmalds/article/details/52704170#commentsedit
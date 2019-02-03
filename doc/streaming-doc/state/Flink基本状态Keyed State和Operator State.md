## Flink基本状态Keyed State和Operator State


#### State
state一般指一个具体的任务或者运算的状态，state数据默认保存在Java的堆内存中

#### Keyed State
Keyed State始终与键相关，且只能在functions和operators中使用KeyedStream

#### Operator State
Operator State,也可以理解为是(no-Keyed State)，每个Operator State都绑定到一个并行运算符实例

#### Raw and Managed State(原始状态和托管状态)
Keyed State和Operator State有两种形式：就是原始状态和托管状态

- 托管状态(Managed State):由Flink运行时控制的数据结构表示，例如内部哈希表或者RocksDB，例如“ValueState”，“ListState”等，Flink的运行时对状态进行编码并将它们写入检查点。
- 原始状态(Row State):是算子保留在自己数据结构中的状态，checkpoint时，它们只会将一个字节序列写入checkpoint，Flink对状态的数据结构一无所知，只能看到原始字节

**所以DataStream上的状态推荐使用托管状态，当实现一个自定义的operator时，会使用到原始状态**

具体使用可参考官方文档：https://ci.apache.org/projects/flink/flink-docs-release-1.7/dev/stream/state/state.html
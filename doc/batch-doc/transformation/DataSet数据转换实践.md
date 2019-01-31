## DataSet数据转化实践

相关方法操作可查看：https://ci.apache.org/projects/flink/flink-docs-release-1.7/dev/batch/

笔者实践了两种方法提供参考
-  Distinct：返回一个数据集中去重之后的元素。
    - 传送门：https://github.com/ash-ali/MyFlink-doc/blob/master/src/main/java/com/example/demo/batch/transformation/BatchDistinct.java
```java
data.distinct();
```


- Join：通过创建在其键上相等的所有元素对来连接两个数据集。(注意：连接转换仅适用于等连接。其他连接类型需要使用OuterJoin或CoGroup表示)
    - 传送门：https://github.com/ash-ali/MyFlink-doc/blob/master/src/main/java/com/example/demo/batch/transformation/BatchJoin.java
```java
result = input1.join(input2)
               .where(0)       // key of the first input (tuple field 0)
               .equalTo(1);    // key of the second input (tuple field 1)
```


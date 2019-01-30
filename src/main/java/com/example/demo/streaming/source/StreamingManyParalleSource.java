package com.example.demo.streaming.source;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * 使用可设置多并行度的数据源，数据源由ManyParalleSource提供
 *
 * by：linjie
 */
public class StreamingManyParalleSource {

    public static void main(String[] args) {
        //构建flink环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //获取数据源
        DataStreamSource<Long> text = env.addSource(new ManyParalleSource()).setParallelism(2);

        //处理数据源
        SingleOutputStreamOperator<Long> num = text.map(new MapFunction<Long, Long>() {
            @Override
            public Long map(Long value) {
                System.out.println("接收到数据：" + value);
                return value;
            }
        });

        //每两秒聚合一次
        SingleOutputStreamOperator<Long> sum = num.timeWindowAll(Time.seconds(2)).sum(0);

        sum.print().setParallelism(1);

        try {
            env.execute("StreamingManyParalleSource任务执行");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

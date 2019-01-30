package com.example.demo.streaming.source;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * 使用并行度为1的数据源，数据源由OneParalleSource生产
 *
 * by：linjie
 */
public class StreamingOneParalleSource {

    public static void main(String[] args){
        //获取flink运行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //获取数据源
        DataStreamSource<Long> text = env.addSource(new OneParalleSource()).setParallelism(1);//针对此source,并行度只能设置为1

        //处理数据
        SingleOutputStreamOperator<Long> num = text.map(new MapFunction<Long, Long>() {
            @Override
            public Long map(Long value) {
                System.out.println("接收到数据：" + value);
                return value;
            }
        });

        //每两秒处理一次数据,并做一次聚合
        //sum(0)因为只有一个数据，所以参数为0
        SingleOutputStreamOperator<Long> sum = num.timeWindowAll(Time.seconds(2)).sum(0);

        sum.print().setParallelism(1);

        try {
            env.execute("StreamingNoParalleSource任务执行");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

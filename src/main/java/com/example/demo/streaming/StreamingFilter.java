package com.example.demo.streaming;

import com.example.demo.streaming.source.OneParalleSource;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;

/**
 * DataStream 算子Transformations的filter过滤方法
 * filter：对传入的数据进行判断，符合条件的数据会被留下
 * by：linjie
 */
public class StreamingFilter {
    public static void main(String[] args){
        //获取flink运行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //获取数据源
        DataStreamSource<Long> text = env.addSource(new OneParalleSource()).setParallelism(1);//针对此source,并行度只能设置为1

        //处理数据,输出
        //过滤之前
        SingleOutputStreamOperator<Long> num = text.map(new MapFunction<Long, Long>() {
            @Override
            public Long map(Long value) {
                System.out.println("原始没有经过filter接收到数据：" + value);
                return value;
            }
        });

        //执行filter过滤，满足条件的会被留下
        SingleOutputStreamOperator<Long> filterData = num.filter(new FilterFunction<Long>() {
            @Override
            public boolean filter(Long value) throws Exception {
                //能被2整出的数据会被留下
                return value % 2 == 0;
            }
        });

        //处理数据，输出
        //过滤之后
        SingleOutputStreamOperator<Long> resuleData = filterData.map(new MapFunction<Long, Long>() {
            @Override
            public Long map(Long value) {
                System.out.println("经过filter接收到数据：" + value);
                return value;
            }
        });
        //每两秒处理一次数据,并做一次聚合
        //sum(0)因为只有一个数据，所以参数为0
        SingleOutputStreamOperator<Long> sum = resuleData.timeWindowAll(Time.seconds(2)).sum(0);

        sum.print().setParallelism(1);

        try {
            env.execute("StreamingNoParalleSource任务执行");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

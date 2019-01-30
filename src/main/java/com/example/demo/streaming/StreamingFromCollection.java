package com.example.demo.streaming;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;

/**
 *  把collection集合作为数据源
 *
 *  by:linjie
 */
public class StreamingFromCollection {

    public static void main(String[] args) {
        //获取flink运行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //构建collection数据源
        ArrayList<Integer> data =  new ArrayList<Integer>();
        data.add(10);
        data.add(15);
        data.add(20);

        //指定数据源
        DataStreamSource<Integer> collectionData = env.fromCollection(data);

        //通过map对数据进行处理
        //MapFunction第一个参数代表输入的数据类型，第二个参数代表输出的数据类型
        DataStream<Integer> num = collectionData.map(new MapFunction<Integer, Integer>() {
            @Override
            public Integer map(Integer value)throws Exception {
                return value+1;
            }
        });

        //打印
        num.print();

        //提交任务执行
        try {
            env.execute("StreamingFromCollection");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.example.demo.streaming.sink;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.redis.RedisSink;
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;
import scala.Tuple2;

/**
 * 接收socket数据，把数据保存到redis中
 *
 * by:linjie
 */
public class StreamingSinkToRedis {
    public static void main(String[] args) {
        //构建flink环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //获取数据
        DataStreamSource<String> text = env.socketTextStream("localhost", 9000, "\n");

        //对数据进行组装
        //把输入的String转化为Tuple2<String,String>
        SingleOutputStreamOperator<Tuple2<String, String>> wordsData = text.map(new MapFunction<String, Tuple2<String, String>>() {
            @Override
            public Tuple2<String, String> map(String value) {
                return new Tuple2<>("l_words", "words");
            }
        });

        //创建redis的配置
        FlinkJedisPoolConfig conf = new FlinkJedisPoolConfig.Builder().setHost("localhost").setPort(6379).build();

        //创建redissink
        RedisSink<Tuple2<String, String>> redisSink = new RedisSink<>(conf, new MyRedisMapper());

        wordsData.addSink(redisSink);

        try {
            env.execute("StreamingSinkToRedis任务执行");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class MyRedisMapper implements RedisMapper<Tuple2<String, String>>{

        @Override
        public RedisCommandDescription getCommandDescription() {
            return new RedisCommandDescription(RedisCommand.LPUSH);
        }

        /**
         * 表示从接收的数据中获取需要操作的redis key
         * @param data
         * @return
         */
        @Override
        public String getKeyFromData(Tuple2<String, String> data) {
            return data._1;
        }

        /**
         * 表示从接收的数据中获取需要操作的redis value
         * @param data
         * @return
         */
        @Override
        public String getValueFromData(Tuple2<String, String> data) {
            return data._2;
        }
    }
}

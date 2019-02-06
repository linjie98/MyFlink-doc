package com.example.demo.streaming.checkpoint;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.ArrayList;

/**
 * 1、设置并开启checkpoint
 * 2、设置checkpoint存储位置
 */
public class StreamingCheckPoint {
    public static void main(String[] args) {
        //获取flink环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //checkpoint周期，每隔1000ms启动一个检查点
        env.enableCheckpointing(1000);

        //设置模式语义为EXACTLY_ONCE（默认）
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

        //设置checkpoint最小时间间隔，也就是说checkpoint时间间隔至少500ms
        env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);

        //设置checkpoint超时时间，也就是检查点必须在60000ms(1分钟)内完成，否则会被丢弃
        env.getCheckpointConfig().setCheckpointTimeout(60000);

        //同一时间，只允许进行一个检查点
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);

        //RETAIN_ON_CANCELLATION：一旦Flink处理程序被cancel后，会保留Checkpoint数据，以便根据实际需要恢复到指定的Checkpoint
        //DELETE_ON_CANCELLATION：一旦Flink处理程序被cancel后，会删除Checkpoint数据，只有job执行失败的时候才会保存checkpoint
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);


        //设置State Backends
        //将checkpoint保存到内存
        //env.setStateBackend(new MemoryStateBackend());

        //将checkpoint保存到文件系统，比如hdfs
        env.setStateBackend(new FsStateBackend("hdfs://master:9000/flink/checkpoints"));

        //将checkpoint保存到RocksDB
        //env.setStateBackend(new RocksDBStateBackend("hdfs://master:9000/flink/checkpoints",true));

        DataStreamSource<String> text = env.socketTextStream("192.168.1.105", 9000, "\n");

        DataStream<WordWithCount> windowCounts = text.flatMap(new FlatMapFunction<String, WordWithCount>() {
            public void flatMap(String value, Collector<WordWithCount> out) throws Exception {
                String[] splits = value.split("\\s");
                for (String word : splits) {
                    out.collect(new WordWithCount(word, 1L));
                }
            }
        }).keyBy("word")
                .timeWindow(Time.seconds(2), Time.seconds(1))
                .sum("count");
                /*.reduce(new ReduceFunction<WordWithCount>() {
                                    public WordWithCount reduce(WordWithCount a, WordWithCount b) throws Exception {

                                        return new WordWithCount(a.word,a.count+b.count);
                                    }
                                })*/
        //把数据打印到控制台并且设置并行度
        windowCounts.print().setParallelism(1);

        try {
            env.execute("StreamingCheckPoint");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static class WordWithCount{
        public String word;
        public long count;
        public  WordWithCount(){}
        public WordWithCount(String word,long count){
            this.word = word;
            this.count = count;
        }
        @Override
        public String toString() {
            return "WordWithCount{" +
                    "word='" + word + '\'' +
                    ", count=" + count +
                    '}';
        }
    }
}

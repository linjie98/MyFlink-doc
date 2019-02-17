package com.example.demo.streaming.kafka;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;

import java.util.Properties;


/**
 * Flink从kafka中消费数据
 *
 * by:linjie
 */
public class StreamingKafkaSource {
    public static void main(String[] args) {
        //获取flink允许环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //配置checkpoint
        //checkpoint周期，每隔1000ms启动一个检查点
        env.enableCheckpointing(10000);

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

        //将checkpoint保存到文件系统，比如hdfs
        env.setStateBackend(new FsStateBackend("hdfs://master:9000/flink/checkpoints"));

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers","192.168.1.200:9092");
        properties.setProperty("group.id","myconsumer");
        FlinkKafkaConsumer011<String> consumer011 = new FlinkKafkaConsumer011<>("ashalitest01", new SimpleStringSchema(), properties);

        consumer011.setStartFromGroupOffsets();//kafka默认的消费策略

        DataStreamSource<String> stringDataStreamSource = env.addSource(consumer011);

        stringDataStreamSource.print();

        try {
            env.execute("StreamingKafkaSource任务执行");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

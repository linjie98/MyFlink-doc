package com.example.demo.streaming.kafka;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchemaWrapper;

import java.util.Properties;

/**
 * Flink向kafka中sink数据
 *
 * by:linjie
 */
public class StreamingKafkaSink {
    public static void main(String[] args) {
        //获取环境
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

        DataStreamSource<String> stringDataStreamSource = env.socketTextStream("127.0.0.1", 9000, "\n");

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers","192.168.1.200:9092");
        //设置事务超时时间15分分钟，kafka事务超时时间默认15分钟，而FlinkKafkaProducer011事务超时时间默认1小时，所以将其1小时改为15分钟(其事务超时时间不能大于kafka的，不然会报错)
        //properties.setProperty("transaction.timeout.ms",60000*15+"");
        //FlinkKafkaProducer011<String> producer011 = new FlinkKafkaProducer011<>("192.168.1.200:9092", "ashalitest01", new SimpleStringSchema());
        //以下可以实现仅一次语义,实现的前提先开启checkpoint
        FlinkKafkaProducer011<String> producer011 = new FlinkKafkaProducer011<>("ashalitest01", new KeyedSerializationSchemaWrapper<String>(new SimpleStringSchema()), properties, FlinkKafkaProducer011.Semantic.EXACTLY_ONCE);
        stringDataStreamSource.addSink(producer011);

        try {
            env.execute("StreamingKafkaSink任务执行");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

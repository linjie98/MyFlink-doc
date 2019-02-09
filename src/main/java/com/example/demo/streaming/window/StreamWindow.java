package com.example.demo.streaming.window;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 流数据下window
 * 窗口countwindow
 *
 * 窗口函数ReduceFunction
 *
 * 据流数据进行分组，每两个相同分组的数据进行一次countwindow
 *
 * by：linjie
 */
public class StreamWindow {
    public static void main(String[] args) {
        //获取flink环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //获取网络数据流
        DataStreamSource<String> text = env.socketTextStream("localhost", 9000, "\n");
        //处理，这里将网络的流数据存储到pojo中并打印流数据
        SingleOutputStreamOperator<WordWithCount> mapdata = text.map(new MapFunction<String, WordWithCount>() {
            @Override
            public WordWithCount map(String value) throws Exception {
                WordWithCount wordWithCount = new WordWithCount();
                wordWithCount.setWord(value);
                System.out.println(value);
                return wordWithCount;
            }
        });
        //根据流数据进行分组，每两个相同分组的数据进行一次countwindow
        mapdata.keyBy("word")
                .countWindow(2)
                    .reduce(new ReduceFunction<WordWithCount>() {
                        @Override
                        public WordWithCount reduce(WordWithCount value1, WordWithCount value2) throws Exception {
                            System.out.println("根据word分组countwindow计算后"+value1.getWord()+value2.getWord());
                            return value1;
                        }
                    }).print();

        try {
            env.execute("执行任务");
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

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
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

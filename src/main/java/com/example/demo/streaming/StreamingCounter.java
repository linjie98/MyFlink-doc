package com.example.demo.streaming;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.api.common.accumulators.IntCounter;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.MapOperator;
import org.apache.flink.configuration.Configuration;

import java.util.ArrayList;

/**
 * Flink 全局累加器
 * 最直接的累加器：计数器
 * 可以实现多并行度的计数，如果不使用Flink的全局累加器，而使用普通的计数方法,只能实现并行度为1的计数
 *
 */
public class StreamingCounter {
    public static void main(String[] args) {
        //构建环境
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        //模拟数据
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("a");
        arrayList.add("b");
        arrayList.add("c");

        //获取数据源
        DataSource<String> stringDataSource = env.fromCollection(arrayList);

        //处理数据,使用全局累加器需要使用RichMapFunction
        MapOperator<String, String> stringStringMapOperator = stringDataSource.map(new RichMapFunction<String, String>() {

            //创建累加器
            private IntCounter numLines = new IntCounter();

            @Override
            public void open(Configuration parameters) throws Exception {
                //注册累加器对象
                getRuntimeContext().addAccumulator("num-lines", this.numLines);
                super.open(parameters);
            }

            @Override
            public String map(String value) throws Exception {
                //使用累加器计数，记录map处理多少数据
                this.numLines.add(1);
                return value;
            }
        }).setParallelism(1);

        //写入文件
        //这里不能使用print()，因为这里需要使用execute获得JobExecutionResult对象
        stringStringMapOperator.writeAsText("/Users/linjie/资料库/test");

        JobExecutionResult myJobExecutionResult = null;
        try {
            //获得并行度为3的累加结果
            myJobExecutionResult = env.execute("StreamingCounter任务执行");
            Integer countresult = myJobExecutionResult.getAccumulatorResult("num-lines");
            System.out.println(countresult);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}

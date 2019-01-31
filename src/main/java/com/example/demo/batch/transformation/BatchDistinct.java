package com.example.demo.batch.transformation;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.operators.FlatMapOperator;
import org.apache.flink.util.Collector;

import java.util.ArrayList;

/**
 * DataSet Transformations中Distinct方法
 *
 * 返回一个数据集中去重之后的元素
 *
 * by：linjie
 */
public class BatchDistinct {

    public static void main(String[] args) {
        //获取运行环境
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        //模拟list批数据
        ArrayList<String> data = new ArrayList<>();
        data.add("xlj");
        data.add("mm");
        data.add("xlj");

        //获取数据集
        DataSource<String> stringDataSource = env.fromCollection(data);

        //处理
        FlatMapOperator<String, String> stringStringFlatMapOperator = stringDataSource.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String value, Collector<String> out) {
                String[] valuesplit = value.split("\\W+");
                for (String data : valuesplit) {
                    System.out.println("数据: "+data);
                    //发出记录
                    out.collect(data);
                }
            }
        });

        //去重输出
        try {
            stringStringFlatMapOperator.distinct().print();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //探讨为什么该Flink程序不需要env.execute(),因为stringStringFlatMapOperator.distinct().print();的print方法中调用了collect()，而collect()中调用了getExecutionEnvironment().execute()来获取JobExecutionResult
    }
}

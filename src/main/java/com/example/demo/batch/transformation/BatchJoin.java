package com.example.demo.batch.transformation;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;

import java.util.ArrayList;

/**
 * DataSet Transformations中join方法
 *
 * 通过创建在其键上相等的所有元素对来连接两个数据集
 *
 * by：linjie
 */
public class BatchJoin {

    public static void main(String[] args) throws Exception {
        //获取环境
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        //模拟键相等的数据
        ArrayList<Tuple2<Integer,String>> input1 = new ArrayList<>();
        ArrayList<Tuple2<Integer,String>> input2 = new ArrayList<>();

        input1.add(new Tuple2<>(1,"linjie"));
        input1.add(new Tuple2<>(2,"mm"));

        input2.add(new Tuple2<>(1,"lanxi"));
        input2.add(new Tuple2<>(2,"hangzhou"));

        //获取数据源
        DataSource<Tuple2<Integer, String>> tuple2DataSource = env.fromCollection(input1);
        DataSource<Tuple2<Integer, String>> tuple2DataSource1 = env.fromCollection(input2);

        //where(0):// key of the first input (tuple field 0)
        //equalTo(0)://key of the second input (tuple field 0)
        tuple2DataSource.join(tuple2DataSource1)
                          .where(0)
                           .equalTo(0)
                            .map(new MapFunction<Tuple2<Tuple2<Integer,String>,Tuple2<Integer,String>>, Object>() {
                                @Override
                                public Object map(Tuple2<Tuple2<Integer, String>, Tuple2<Integer, String>> value) throws Exception {
                                    return new Tuple3<>(value.f0.f0,value.f0.f1,value.f1.f1);
                                }
                            }).print();

    }
}

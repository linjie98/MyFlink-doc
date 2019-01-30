package com.example.demo.streaming.source;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * 自定义数据源，并行度为1的数据源
 *
 * 模拟产生从1开始的递增数据
 *
 * 注意：SourceFunction和SourceContext都需要指定产生的数据的数据类型
 *
 * by：linjie
 */
public class OneParalleSource implements SourceFunction<Long> {

    //计数变量初始化
    private long count = 1L;

    //循环变量控制
    private boolean isRunning = true;

    /**
     * 启动一个数据源
     * 大多数情况都会在run中实现一个循环，就可以循环产生数据，使之为流(stream)
     * @param sourceContext
     * @throws Exception
     */
    @Override
    public void run(SourceContext<Long> sourceContext) throws Exception {
        while (isRunning){
            sourceContext.collect(count);
            count++;
            //设置1秒产生1条数据
            Thread.sleep(1000);
        }
    }

    /**
     * 停止任务调用该方法
     * 如果想在停止任务之前关闭一些服务或者资源，可以写在cancel里面
     */
    @Override
    public void cancel() {
        isRunning = false;
    }
}

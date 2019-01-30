package com.example.demo.streaming.source;


import org.apache.flink.streaming.api.functions.source.ParallelSourceFunction;

/**
 * 自定义数据源 ， 支持可设置并行度的数据源
 *
 * by：linjie
 */
public class ManyParalleSource implements ParallelSourceFunction<Long> {

    private long count = 1L;

    //循环变量控制
    private boolean isRunning = true;

    /**
     * 启动一个数据源
     * 大多数情况都会在run中实现一个循环，就可以循环产生数据，使之为流(stream)
     * @param ctx
     * @throws Exception
     */
    @Override
    public void run(SourceContext<Long> ctx) throws Exception {
        while (isRunning){
            ctx.collect(count);
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

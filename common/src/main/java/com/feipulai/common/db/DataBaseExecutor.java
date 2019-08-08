package com.feipulai.common.db;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 数据库操作队列
 */
public class DataBaseExecutor {

	/**  数据库操作线程池队列，同时只允许一个线程操作数据库*/
	private static ExecutorService executorService = Executors.newFixedThreadPool(1);
	
	/**
	 * 往线程池添加线程
	 * @param task
	 */
	public static void addTask(Runnable task){
		executorService.submit(task);
	}
	
	/**
	 * 往线程池添加事务
	 *
	 * @param task 数据库操作事务对象
	 */
	public static void addTask(DataBaseTask task){
		executorService.submit(task);
	}
	
	/**
	 * 关闭线程池
	 *
	 */
	public static void shutdown(){
		executorService.shutdown();
	}
	
		
}

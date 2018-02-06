package download;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 对线程池的封装和管理
 * 
 * @author Administrator
 * 
 */
public class ThreadPoolManager {
	private static ThreadPoolManager mIntance = new ThreadPoolManager();
	
	public static ThreadPoolManager getInstance(){return mIntance;}
	private int corePoolSize;
	private int maximumPoolSize;
	private long keepAliveTime = 1;//
	private TimeUnit unit = TimeUnit.HOURS;//
	private ThreadPoolExecutor executor;
	
	private ThreadPoolManager(){
		//corePoolSize = Runtime.getRuntime().availableProcessors()*2 + 1;
		corePoolSize = 10;
		maximumPoolSize = corePoolSize;
		executor = new ThreadPoolExecutor(
				corePoolSize,
				maximumPoolSize,
				keepAliveTime,
				unit, 
				new LinkedBlockingQueue<Runnable>(),
				Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.AbortPolicy()
				);
	}
	/**
	 * @param runnable
	 */
	public void execute(Runnable runnable){
		if(runnable==null)return;
		
		executor.execute(runnable);
	}
	/**
	 * @param runnable
	 */
	public void remove(Runnable runnable){
		if(runnable==null)return;
		
		executor.remove(runnable);
	}
}

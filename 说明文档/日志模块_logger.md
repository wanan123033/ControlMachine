智能主机的logger模块是一个Android library,基于GitHub上的开源项目[orhanobut/logger](https://github.com/orhanobut/logger),其主要特性如下图,在其基础上,添加了加密日志保存的功能.

![](https://github.com/orhanobut/logger/raw/master/art/logger_output.png)

智能主机收到**来自测试设备的数据**和**对数据库进行修改**时保存log:

1. app因出现异常奔溃时,通过log查看考试成绩,并做处理(app本身并不提供恢复手段);
2. 测试人员对成绩有异议时,通过log查看考试成绩,作为依据;
3. bug调试;

### 文档编写依据
产品需求

### 提供API

所有日志记录都应该只通过Logger.xxx()来实现,其主要提供的API如下:
```java

	// 设置Printer
	public static void printer(Printer printer)
	// 设置下一次打印使用指定tag,只在下一次有效
	public static Printer t(String tag)
	// 添加log适配器,控制log是否打印和打印格式,在打印log之前,需要设置好;算是初始化
	public static void addLogAdapter(LogAdapter adapter)
    // 清除所有的LogAdapter
    public static void clearLogAdapters()
    // 打印指定信息
	public static void log(int priority,String tag,String message,Throwable throwable)
	// 打印指定信息,debug级别
	public static void d(String message,Object... args)
	public static void d(Object object)
	// 打印指定信息,error级别
	public static void e(String message,Object... args)
	public static void e(Throwable throwable,String message,Object... args)
	// 打印指定信息,info级别
	public static void i(String message,Object... args)
	// 打印指定信息,verbose级别
	public static void v(String message,Object... args)
	// 打印指定信息,warn级别
	public static void w(String message,Object... args)
	// 打印指定信息,assert级别(wtf:what the fuck),排查错误bug时使用
	public static void wtf(String message,Object... args)
	// 打印指定信息,将其转换为格式化的json字符串
	public static void json(String json)
	// 打印指定信息,将其转换为格式化的xml字符串
	public static void xml(String xml)
    // 将当前应用的所有日志打印到指定文件中(原生格式)
    public static void rawLogToFile(String filePath)
```

### 使用示例

```java
//初始化日志工具
private void initLogger(){
	//日志打印到控制台,在发布release版本时，会自动不打印
	Logger.addLogAdapter(new AndroidLogAdapter(){
		@Override
		public boolean isLoggable(int priority,String tag){
			return BuildConfig.DEBUG;
		}
	});
	DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss",Locale.CHINA);
	// 非加密日志存储在在sd卡中“logger”目录中
	String diskLogFilePath = Environment.getExternalStorageDirectory() + "/logger/" + dateFormat.format(Calendar.getInstance().getTime()) + ".txt";
	Logger.addLogAdapter(new DiskLogAdapter(diskLogFilePath));
	//加密日志存储
	String encryptFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/fair/play/";
	Logger.addLogAdapter(new EncryptDiskLogAdapter(encryptFolder,"James",LOG_ENCRYPT_KEY));
	String rawLogFilePath = Environment.getExternalStorageDirectory() + "/raw_log/" + dateFormat.format(Calendar.getInstance().getTime()) +".txt";
	Logger.rawLogToFile(rawLogFilePath);
}
```



### 实现原理 ###

其主要原理可以参考logger的官方文档,其主要原理可以参考如下图:

![How it works](https://github.com/orhanobut/logger/raw/master/art/how_it_works.png)   

- logger所有的任务均是通过Printer完成,默认的Printer为一个LoggerPrinter;

- Printer根据已经添加的LogAdapter打印log,LogAdapter定义如下:

  ```java
  public interface LogAdapter {
    // 是否打印log
    boolean isLoggable(int priority, String tag);
    // 依据指定信息打印log
    void log(int priority, String tag, String message);
  }
  ```

- 每个LogAdapter选择一个FormatStrategy,将每一条打印的数据转化为其指定格式;

- FormatStrategy还可以指定一个LogStrategy,决定log打印的方式(比如打印到控制台或者输出到文件等);

logger本身自带了3个LogAdapter:

1. AndroidLogAdapter:将日志打印到控制台,默认格式为优化过的格式,如下图;![](https://github.com/orhanobut/logger/raw/master/art/logcat_options.png)
2. DiskLogAdapter:将日志打印到指定文件中,默认格式为`时间(yyyy/MM/dd  HH:mm:ss) 日志级别(VERBOSE->ASSERT) tag message`
3. EncryptDiskLogAdapter:对打印的日志按照指定的秘钥进行DES加密,并打印到指定文件中;

### 注意事项

**正式版本和测试版本**的区别:

- 正式版本的日志应该要比测试版本日志少,涉及重要机密的日志在正式版本不应该未加密打印;
- 正式版有些打印方式应该被禁止掉,比如正式版不应该往控制台打印日志;
- 加密日志取出来后,不要进行任何修改,否则会造成无法解密;



**需要使用指定的解密工具进行解密**

> 解密工具-LogDecrypt.exe

解压`日志解密工具.zip`文件，打开`LogDecrypt.exe`程序，把需要解密的文件拖入输入框中，点击解密按钮即可，解密后的文件保存在需解密文件的同一目录下，文件名称为`decrypt-解密文件名称.txt`
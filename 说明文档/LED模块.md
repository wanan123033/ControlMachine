LED模块通过无线868模块发送LED命令,控制LED显示屏显示具体信息;



```sequence
LED模块->串口模块:LED命令
串口模块->LED显示屏:LED命令(通过\n串口转换器)
Note right of LED显示屏:显示内容
```

### 编写依据

- 产品需求
- 串口模块
- LED命令文档:[LED通信协议](./ref/LED通信协议.txt)

### 提供API

LED相关操作通过`LEDManager`来进行操作,需要实例化一个`LEDManager`对象,然后进行如下操作:

```java
/**
 * 连接LED屏幕
 * @param machineCode 测试项目机器码{@link ItemDefault.CODE_XXX}
 * @param hostId      主机号
 */
public void link(int machineCode,int hostId)
/**
 * LED显示屏自检
 * @param machineCode 测试项目机器码
 * @param hostId      主机号
 */
public void test(int machineCode,int hostId)
/**
 * 清空LED显示屏
 * @param machineCode 测试项目机器码
 * @param hostId      主机号
 */
public void clearScreen(int machineCode,int hostId)
/**
 * 显示字符串
 * @param machineCode 测试项目机器码
 * @param hostId      主机号
 * @param str         需要显示的字符串
 * @param x           字符串显示X轴位置
 * @param y           字符串显示Y轴位置
 * @param clearScreen 是否清空LED显示屏
 * @param update      是否立即更新显示屏信息
 */
public void showString(int machineCode,int hostId,String str,int x,int y,boolean clearScreen,boolean update)
/**
 * 指定对齐方式和y轴位置,显示字符串
 * @param align 对齐方式 默认为{@link #LEFT}   {@link #MIDDLE}   {@link #RIGHT}
 */
public void showString(int machineCode,int hostId,String str,int y,boolean clearScreen,boolean update,int align)
/**
 * 该命令没有调试过,不推荐使用,也没有需要使用的地方
 */
public void showBitmap(int machineCode,int hostId,Bitmap bitmap,int x,int y,boolean clearScreen,boolean update)
/**
 * 默认机器码(TestConfigs中指定),显示字符串
 */
public void showString(int hostId,String str,int y,boolean clearScreen,boolean update,int align)
/**
 * 默认机器码(TestConfigs中指定)
 * 指定对齐方式和y轴位置,显示字符串显示字符串
 */
public void showString(int hostId,String str,int x,int y,boolean clearScreen,boolean update)
/**
 * 增加亮度
 * @param machineCode 测试项目机器码
 * @param hostId      主机号
 */
public void increaseLightness(int machineCode,int hostId)
/**
 * 减少亮度
 * @param machineCode 测试项目
 * @param hostId      主机号
 */
public void decreaseLightness(int machineCode,int hostId)
/**
 * 亮度最暗
 * @param machineCode 测试项目
 * @param hostId      主机号
 */
public void darkest(int machineCode,int hostId)
/**
 * 显示当前项目信息
 * @param hostId 主机号
 */
public void resetLEDScreen(int hostId)
```

### 使用示例

```java
private LEDManager mLEDManager =- new LEDManager();
mLEDManager.showString(hostId,"跳绳",5,0,true,false);
mLEDManager.showString(hostId,"请检录",5,1,false,true);
mLEDManager.showString(hostId,"菲普莱体育",3,3,false,true);
```

### 实现原理

如本文档最前的序列图所示,LED模块所有的功能都是通过串口模块完成,通过串口模块发送信息给LED显示屏,这里涉及到几个具体的问题:

**LED尺寸**:可显示字符尺寸为16×4

**命令字节中机器码与传入的机器码区别**

[LED通信协议](LED通信协议.txt)中,传给LED显示屏的命令需要提供机器码;该机器码与`ItemDefault`中的机器码值不一致,通过一个`HashMap`提供相应的映射:

```Java
static final Map<Integer,Integer> machineCodesForLed = new HashMap<>();
	
	static{
		// 体侧版所有项目
		machineCodesForLed.put(0,0);//公共频道	0
		machineCodesForLed.put(ItemDefault.CODE_TS,1);//跳绳计数
		machineCodesForLed.put(ItemDefault.CODE_HW,5);//身高体重
		machineCodesForLed.put(ItemDefault.CODE_FHL,8);//肺活量
		machineCodesForLed.put(ItemDefault.CODE_LDTY,4);//立定跳远
		machineCodesForLed.put(ItemDefault.CODE_YWQZ,10);//仰卧起坐
		machineCodesForLed.put(ItemDefault.CODE_ZWTQQ,2);//坐位体
		machineCodesForLed.put(ItemDefault.CODE_HWSXQ,6);//实心球
		// 体侧版没有的项目
		machineCodesForLed.put(ItemDefault.CODE_SL,0);//视力
		machineCodesForLed.put(ItemDefault.CODE_FWC,7);//俯卧撑
		machineCodesForLed.put(ItemDefault.CODE_MG,3);//摸高测试
		machineCodesForLed.put(ItemDefault.CODE_YTXS,9);//引体向上
		machineCodesForLed.put(ItemDefault.CODE_ZCP,0);//中长跑
		machineCodesForLed.put(ItemDefault.CODE_PQ,110);//排球垫球
	}
```

**通讯频率选择**

1. 各个项目对应不同的起始频段号;

2. 不同主机,依据 项目基频  + 主机号, 确定具体频段号;

   依据[LED通信协议](LED通信协议.txt),各个项目对应的起始频段号也通过一个`HashMap`来进行映射:

   ```java
   #SerialConfigs.java
   //每个项目对应的开始频道号
   public static final Map<Integer, Integer> sProChannels = new HashMap<>();
   static {
   	sProChannels.put(0, 0);//公共频道	0
   	sProChannels.put(ItemDefault.CODE_HW, 41);//身高体重
   	sProChannels.put(ItemDefault.CODE_FHL, 110);//肺活量
   	sProChannels.put(ItemDefault.CODE_LDTY, 55);//立定跳远
   	sProChannels.put(ItemDefault.CODE_MG, 40);//摸高测试
   	sProChannels.put(ItemDefault.CODE_FWC, 25);//俯卧撑
   	sProChannels.put(ItemDefault.CODE_YWQZ, 10);//仰卧起坐
   	sProChannels.put(ItemDefault.CODE_ZWTQQ, 41);//坐位体
   	sProChannels.put(ItemDefault.CODE_TS, 1);//跳绳计数
   	sProChannels.put(ItemDefault.CODE_SL, 0);//视力
   	sProChannels.put(ItemDefault.CODE_YTXS, 25);//引体向上
   	sProChannels.put(ItemDefault.CODE_HWSXQ, 70);//实心球
   	sProChannels.put(ItemDefault.CODE_ZCP, 0);//中长跑
   	sProChannels.put(ItemDefault.CODE_PQ, 110);//排球垫球
   }
   ```

   具体主机与LED通讯频段计算为:

   `SerialConfigs.sProChannels.get(machineCode) + hostId - 1`

   **连接过程**

   1. 切到0频段;
   2. 发送LED连接命令;
   3. 切换到主机与LED通讯频段;

### 注意事项

无

### 主要测试点

参考项目LED设置和具体项目测试过程文档


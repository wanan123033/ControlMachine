# 个人测试基类（BasePersonTestActivity）



 



| 文档版本号： | 1.0      | 文档编号：      |      |
| ------------ | -------- | --------------- | ---- |
| 文档密级：   |          | 归属部门/项目： |      |
| 系统名：     | 智能主机 | 子系统名：      |      |
| 编写人：     |          | 编写日期：      |      |

 

 

 

**文档修订记录**

| **版本号** | **修订日期** | **修订人** | **修订说明** | **修订状态** | **审核日期** | **审核人** |
| ---------- | ------------ | ---------- | ------------ | ------------ | ------------ | ---------- |
| v1.0       | 2018-10-18   | 郑梓笙     |              | A            |              |            |
|            |              |            |              |              |              |            |
|            |              |            |              |              |              |            |
|            |              |            |              |              |              |            |
|            |              |            |              |              |              |            |
|            |              |            |              |              |              |            |
|            |              |            |              |              |              |            |
|            |              |            |              |              |              |            |
|            |              |            |              |              |              |            |

修订状态：A--增加，M--修改，D--删除

日期格式：YYYY-MM-DD



## 编写依据

- 产品需求

- 具体实现代码

  

## **参考资料**

| **参考文件**                  | **备注** |
| ----------------------------- | -------- |
| [检录.md](检录.md)            |          |
| [串口模块_SerialManager.md]() |          |
| [应用整体流程.md]()           |          |

## 

## 功能流程

- 设置测试项目的可用测试位`findDevice()`，不为空则调用`addDevice(@NonNull BaseDeviceState device)` 方法添加测试位

- 考生检入[检录.md](检录.md)，获取考生信息后将学生添加到闲置的测试位`private void addStudent(Student student)`，更新测试位状态`updateDevice(@NonNull BaseDeviceState deviceState)`，若当前没有可用测试位则提示《当前无设备可添加学生测试》

- 考生添加成功将调用`sendTestCommand(BaseStuPair baseStuPair)`方法通知子类那个设备添加学生成功，可以开启该设备的成绩获取指令的发送，串口相关信息可参考[串口模块_SerialManager.md]()

- 获取测试设备成绩通过调用updateResult(@NonNull BaseStuPair baseStu)更新成绩信息与显示，测试结束将该设备状态修改为`BaseDeviceState.STATE_END`结束状态，基类将保存考生成绩，根据系统设置进行成绩播报`broadResult()`，打印`printResult()`，自动成绩上传`uploadResult()`等操作，4秒后清除该测试位学生，测试位状态更新为闲置状态可以再次检入学生测试



```flow
st=>startTime: Start
e=>end 
test=>operation: 考生检入  
cond=>condition: 设备是否可用 
addTest=>operation: 开始测试  
endtest=>operation: 测试结束显示成绩重新开始新测试  
st->test->cond
cond(no)->test
cond(yes)->addTest
addTest->endtest
endtest->test
```



# **详细设计**

### 概要说明

通过个人测试基类对项目中（立地跳远，肺活量，坐位体前屈，红外实心球）的测试功能流程的封装



### 提供API

``` java
/**
 * 发送测试指令 并且此时应将设备状态改变
 */
public abstract void sendTestCommand(BaseStuPair baseStuPair);

/**
 * 查询设备,初始化测试设备
 * 找到设备后需添加设备调用  addDevice方法
 */
public abstract List<BaseDeviceState> findDevice();

/**
 * 设置项目单位
 */
public abstract String setUnit();
 /**
  * 添加设备
  * @param device
  */
public synchronized void addDevice(@NonNull BaseDeviceState device) 
/**
 * 修改已添加设备状态，设备状态为STATE_END判定为测试结束，可进行成绩打印、播报、保存
 * @param deviceState
 */
public void updateDevice(@NonNull BaseDeviceState deviceState) 
 /**
  * 更新学生成绩
  *
  * @param baseStu
  */
public synchronized void updateResult(@NonNull BaseStuPair baseStu) 
```

#### BaseStuPair考生测试位相关信息

``` java
//成绩
private int result;
private int resultState;//成绩状态 0正常  -1犯规    -2中退    -3放弃
//学生
private Student student;
//设备
private BaseDeviceState baseDevice;
```

#### BaseDeviceState设备状态（ 空闲、未开始 、正在使用中、结束、断开，或故障）

``` java
//状态   
private int state;
//设备ID
private int deviceName;
/**
*空闲
*/
public static final int STATE_FREE = 1;
 /**
  * 正在使用中
  */
 public static final int STATE_ONUSE = 5;
 /**
  * 结束
  */
 public static final int STATE_END = 6;
 /**
  *已添加学生，未开始测试
  */
 public static final int STATE_NOT_BEGAIN = 7;
 /**
  * 断开，或故障
  */
 public static final int STATE_ERROR = 8;
```





### 实现原理

根据产品需求以抽象类的形式完成个人测试的整体测试流程，实现随到随测的功能

### **接口设计**

``` java
/**
 * 设备故障状态图标点击事件监听
 */
public interface OnMalfunctionClickListener {
      void malfunctionClickListener(BaseStuPair baseStuPair);
}
```



### 注意事项

- 更新测试位为结束状态前必须先调用`updateResult(@NonNull BaseStuPair baseStu)` 更新最新成绩，避免获取的成绩不是最新成绩
- 成绩更新中的成绩为测试项目设备获取的成绩根据单位转换后的成绩



### 主要测试点

测试（立地跳远，肺活量，坐位体前屈，红外实心球）项目相关流程






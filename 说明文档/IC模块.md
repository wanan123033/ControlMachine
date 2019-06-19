

`IC`卡模块,通过系统自带的NFC机制,负责读取`IC`卡信息和向`IC`卡写入信息;

#### 编写时间轴

|     时间     |  版本  | 修改内容 | 修改人 |
| :----------: | :----: | :------: | :----: |
| `2018-10-12` | `V1.0` | 新建文档 | 王伟杰 |

### 编写依据

- [产品需求](./ref/智能主机项目1.3原型演示.exe)
- [IC卡存储格式协议（V2.1）](./ref/IC卡存储格式协议（V2.1）.doc)

### 提供API

> 在使用IC卡模块读取数据前需要初始化Android NFC模块.
>
> NFC初始化相关请参考官方文档或现有代码

在用户刷`IC`卡时,注册了对应NFC意图且处于有效(enable)状态的Activity会被调用,并且其`onNewIntent`方法会被调用,传入`IC`卡相关Intent.该intent作为`IC`卡模块的输入.

使用`IC`卡功能,首先需要利用该`intent`创建`ICCardDealer`对象:

```java
ICCardDealer icCardDealer = new ICCardDealer(intent);
```

利用该对象,我们可以操作当前刷卡的`IC`卡,对其进行信息读写,其相关`API`如下:

```java
/**
 * 读学生信息
 *
 * @return 学生信息对象,含学号、姓名和性别等信息
 * @throws IOException 读取异常时抛出
 */
public StuInfo IC_ReadStuInfo() throws IOException

/**
 * 向IC卡中写入学生信息
 * 注意:在写入学生数据之前必须先读取学生信息,在读取出来的对象上修改后再写入
 *
 * @return 成功返回true,失败时返回false
 * @throws IOException 写入成绩时发生写异常时抛出
 */
public boolean IC_WriteStuInfo() throws IOException

/**
 * 读取扩展信息
 *
 * @return 扩展信息对象，含考生项目树、学年、单位信息等
 * @throws IOException 读取异常时抛出
 */
public ExpandInfo IC_ReadExpandInfo() throws IOException

/**
 * 写入扩展信息
 * 注意:在写入扩展信息必须先读取扩展信息,在读取出来的对象上修改再写入
 *
 * @return 成功返回true,失败时返回false
 * @throws IOException 写入成绩时发生写异常时抛出
 */
public boolean IC_WriteExpandInfo() throws IOException

/**
 * 读取项目属性信息,目前最多16个的项目属性
 *
 * @return 具体项目属性信息数组,每个项目属性信息包含对应项目存储在IC卡中的位置
 * @throws IOException 读取异常时抛出
 */
public ItemProperty[] IC_ReadItemProperties() throws IOException

/**
 * 写入项目属性信息
 * 注意:在写入扩展信息必须先读取扩展信息,在读取出来的对象上修改再写入
 *
 * @return 成功返回true,失败时返回false
 * @throws IOException 写入成绩时发生写异常时抛出
 */
public boolean IC_WriteItemProperties() throws IOException

/**
 * 指定项目是否报名
 *
 * @param machineCode 机器码
 * @return 报名了则返回true,否则返回false
 * @throws IOException 读取异常时抛出
 */
public boolean isItemRegistered(int machineCode) throws IOException

/**
 * 读取IC卡中指定项目成绩
 *
 * @param machineCode 机器码
 * @return 指定项目成绩信息对象,一个该对象可能有多个成绩,身高体重和视力成绩格式比较特殊,具体参考 {@link ItemResult} 中的注释; 读取的项目成绩单位为IC卡中的单位,与数据库中取出的数据进行比较需要先进行单位转换,具体可以参考 {@link com.feipulai.host.ic.utils.ICResultConverter};没有报名该项目时返回null
 * @throws IOException 读取异常时抛出
 */
public ItemResult IC_ReadItemResult(int machineCode) throws IOException

/**
 * 向IC卡中写入指定项目成绩
 * @param itemResult  要写入的成绩对象,身高体重和视力成绩格式比较特殊,具体参考 {@link ItemResult} 注释;在写入成绩到IC卡中之前,需要将成绩转换为IC卡中的单位格式,具体参考 {@link com.feipulai.host.ic.utils.ICResultConverter};写入的成绩应该是在读取的项目成绩上进行修改,而不是新建一个成绩并设置参数,后一种方式是不安全的
 * @param machineCode 机器码
 * @return 报名了则返回true,否则返回false
 * @throws IOException 读取异常时抛出
 */
public boolean IC_WriteItemResult(ItemResult itemResult,int machineCode) throws IOException

```

### 实现原理

参考[IC卡存储格式协议（V2.1）](./ref/IC卡存储格式协议（V2.1）.doc)

### 注意事项

1. 使用`NFC`机制时,在预期会使用`NFC`机制的`Activity`的`onResume`方法中启用`NFC`的前台分发,在`onPause`方法中及时取消前台分发,避免因为非预期的刷`IC`卡导致应用流程混乱;
2. 在向`IC`卡中写入信息之前,需要先读取对应的信息,并在该信息上修改,写入的信息必须是读取出来的信息对象;不要新建一个对象并赋值,这样不仅操作复杂,而且,如果赋值存在不合理,那么可能出现错误,且几乎无法排查;
3. 补充第2点,代码中目前对写入学生信息、扩展信息和项目属性信息都进行了这方面的限制,写入成绩没有进行限制;
4. `IC`卡中读取的成绩,其单位为[IC卡存储格式协议（V2.1）](IC卡存储格式协议（V2.1）.doc)中规定的单位;反之,写入成绩,也要转换为`IC`卡中对应项目的单位;如果需要进行数据库单位形式和`IC`卡单位形式互转,参考`ICResultConverter`类;

### 主要测试点

1. 刷卡不应该有反应的界面刷`IC`卡,不应该有反应;
2. 读写学生信息;
3. 读写成绩信息;


## excel导入导出



| 文档版本号： | 1.0      | 文档编号：      |           |
| ------------ | -------- | --------------- | --------- |
| 文档密级：   | 绝密     | 归属部门/项目： | 开发部    |
| 系统名：     | 智能主机 | 子系统名：      |           |
| 编写人：     | 王伟杰   | 编写日期：      | 2018-11-2 |



**文档修订记录**

| **版本号** | **修订日期** | **修订人** | **修订说明** | **修订状态** | **审核日期** | **审核人** |
| ---------- | ------------ | ---------- | ------------ | ------------ | ------------ | ---------- |
|            |              |            |              |              |              |            |
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

### 编写依据

- 产品需求

| **参考文件**                               | **备注** |
| ------------------------------------------ | -------- |
| [exel导出模板.xls](./ref/exel导出模板.xls) |          |
| [exel导入模板.xls](./ref/exel导入模板.xls) |          |
| [POI](https://poi.apache.org)              |          |

### 提供API

为了解耦,提供两个抽象类-----`ExlReader`和`ExlWriter`,分别用于Excel文件读取和写入;

提供API如下:

`ExlWriter:`Excel文件写入

```java
/**
 * 创建对象时必须传入回调listener
 * @param listener 读写结果返回(成功或失败,及原因)
 */ 
public ExlWriter(ExlListener listener)
/**
 * 写数据到指定excel中
 * @param path 指定excel文件路径
 */
public void writeExelData(final String path)
/**
 * 子类实现的具体写入方式
 * @param path 指定excel文件路径,该方法在子线程中完成
 */
protected abstract void write(String path);
```

`ExlReader:`Excel文件读取

```java
/**
 * 创建对象时必须传入回调listener
 * @param listener 读写结果返回(成功或失败,及原因)
 */
public ExlReader(ExlListener listener)
/**
 * 读指定excel文件数据
 * @param path 指定excel文件路径
 */
public void readExlData(final String path)
/**
 * 子类实现的具体读取方式
 * @param path 指定excel文件路径,该方法在子线程中完成
 */
protected abstract void read(String path);
```

`ExlListener:`提供一些与excel读取有关的常量和回调方法

### 实现原理

使用[POI](https://poi.apache.org)框架读写Excel文件

除上述框架内容外,提供了学生名单导入和成绩信息导出.

#### 学生名单信息导入

学生名单导入模板:[exel导入模板.xls](./ref/exel导入模板.xls)

导入流程大致如下:

```flow
st=>start: 打开excel文件
openSuc=>condition: 打开成功?
fail=>operation: 导入失败
readFirstRow=>operation: 读取第一行信息(标题)
allCols=>condition: 必要列均存在?
getFromRow=>operation: 读取每一行数据
checkEachRow=>condition: 检查每一行数
						据的合法性?
import=>operation: 导入学生名单到数据库中

st->openSuc
openSuc(yes)->readFirstRow
readFirstRow->allCols
allCols(yes)->getFromRow
getFromRow->checkEachRow
checkEachRow(yes)->import
checkEachRow(no)->fail
allCols(no)->fail
openSuc(no)->fail
```

**注意事项**

1. 导入失败时会有相应的错误信息返回;
2. 导入的必要列包括:`{"性别","准考证号","姓名","项目","项目代码"};`

2. 待导入信息必须在文件的第一张表中;
3. 除必要列外,还会自动识别列`"身份证号"`;
4. 每一行数据的合法性检验包括:
   1. 每一个必要列必须存在数据内容;
   2. 性别`列内容必须为`男`或`女`.`
   3. `项目`和`项目代码`列,每一行必须相同.
   4. 如果有`身份证号`列,且该列内容不为空,正常导入身份证号.

5. 导入学生名单到数据库中,这里涉及到机器码和项目代码的机制,参考文档[机器码与项目代码特殊机制](./机器码与项目代码特殊机制.md)中exel导入相关部分;
6. 身高体重信息导入时,只需要导入其中任一一项即可,在应用中均是当做身高信息导入,检录时也以身高信息为准.

#### 成绩信息导出

学生信息导出模板:[exel导出模板.xls](./ref/exel导出模板.xls)

成绩导出会将当前项目已测试的成绩导出到指定excel文件中,大致流程如下:

```flow
st=>start: 输出文件头
searchreg=>operation: 读取当前项目所有报名信息
searchresults=>operation: 读取当前项目所有成绩信息
output=>operation: 输出成绩信息

st->searchreg->searchresults->output
```

**注意事项**

1. 如果是身高体重项目,后先输出所有身高信息,后再输出所有体重信息;

### 主要测试点

1. 不同格式文件,学生信息导入的正确性;
2. 切换项目,导入文件;
3. 成绩导出功能正常;
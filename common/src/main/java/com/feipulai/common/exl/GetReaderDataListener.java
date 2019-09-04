package com.feipulai.common.exl;

import java.util.List;

/**
 * Created by zzs on  2019/8/26
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public interface GetReaderDataListener {
    void readerLineData(int rowNum, List<String> data);
}

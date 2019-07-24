package com.feipulai.device.serial.parser;


import com.feipulai.device.serial.beans.RS232Result;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by James on 2018/11/9 0009.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */

public class VolleyBallParser extends RS232Parser {

    @Override
    public RS232Result parse(InputStream is) throws IOException, InterruptedException {
        RS232Result result = null;
        byte[] head = new byte[4];
        while (is.available() < 1) {
            Thread.sleep(10);
        }
        is.read(head, 0, 1);

        if ((head[0] & 0xff) == 0x54) {
            while (is.available() < 1) {
                Thread.sleep(10);
            }
            is.read(head, 1, 1);

            if ((head[1] & 0xff) == 0x55) {
                while (is.available() < 2) {
                    Thread.sleep(10);
                }
                is.read(head, 2, 2);

                int length = ((head[2] & 0xff) << 8) + (head[3] & 0xff);
                byte[] data = new byte[length];
                System.arraycopy(head, 0, data, 0, 4);
                while (is.available() < length - 4) {
                    Thread.sleep(10);
                }
                is.read(data, 4, length - 4);
                result = new RS232Result(data);
            }
        }
        return result;
    }

}

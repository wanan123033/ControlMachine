package com.feipulai.device.manager;

public interface VisionManager {
    //允许视力检测 //0xF0,0x3F,0x06,0x32,0x00,0x00,0x00,0x00,0x67
    byte[] START_TEST = {(byte) 0xF0,0x3F,0x01, 0x34};
}

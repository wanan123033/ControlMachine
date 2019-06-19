#include <jni.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <termios.h>

#define IODEV_IOC_MAGIC  'H'
#define IODEV_IOCTL_BARCODE_PWR_EN   _IOWR(IODEV_IOC_MAGIC, 2, uint8_t)
#define IODEV_IOCTL_BARCODE_TRIG     _IOWR(IODEV_IOC_MAGIC, 3, uint8_t)
#define IODEV_IOCTL_IDENTITY_PWR_EN  _IOWR(IODEV_IOC_MAGIC, 4, uint8_t)
#define IODEV_IOCTL_UHFCOMM_PWR_EN   _IOWR(IODEV_IOC_MAGIC, 5, uint8_t)
#define IODEV_IOCTL_FINGER_PWR_EN    _IOWR(IODEV_IOC_MAGIC, 6, uint8_t)
#define IODEV_IOCTL_ICCARD_PWR_EN    _IOWR(IODEV_IOC_MAGIC, 7, uint8_t)
#define IODEV_IOCTL_PRINTER_PWR_EN   _IOWR(IODEV_IOC_MAGIC, 8, uint8_t)

extern "C"
JNIEXPORT void JNICALL
Java_com_feipulai_device_serial_IOPower_setBarcodePwr(JNIEnv *env, jobject instance, jint status) {
    int dev = open("/dev/iodev", O_RDWR);
    if(dev > 0){
        ioctl(dev, IODEV_IOCTL_BARCODE_PWR_EN, &status);
        close(dev);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_feipulai_device_serial_IOPower_setBarcodetrig(JNIEnv *env, jobject instance, jint status) {
    int dev = open("/dev/iodev", O_RDWR);
    if(dev > 0){
        ioctl(dev, IODEV_IOCTL_BARCODE_TRIG, &status);
        close(dev);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_feipulai_device_serial_IOPower_setFingerPwr(JNIEnv *env, jobject instance, jint status) {
    int dev = open("/dev/iodev", O_RDWR);
    if(dev > 0){
        ioctl(dev, IODEV_IOCTL_FINGER_PWR_EN, &status);
        close(dev);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_feipulai_device_serial_IOPower_setIdentityPwr(JNIEnv *env, jobject instance, jint status) {
    int dev = open("/dev/iodev", O_RDWR);
    if(dev > 0){
        ioctl(dev, IODEV_IOCTL_IDENTITY_PWR_EN, &status);
        close(dev);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_feipulai_device_serial_IOPower_setICCardPwr(JNIEnv *env, jobject instance, jint status) {
    int dev = open("/dev/iodev", O_RDWR);
    if(dev > 0){
        ioctl(dev, IODEV_IOCTL_ICCARD_PWR_EN, &status);
        close(dev);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_feipulai_device_serial_IOPower_setPrinterPwr(JNIEnv *env, jobject instance, jint status) {
    int dev = open("/dev/iodev", O_RDWR);
    if(dev > 0){
        ioctl(dev, IODEV_IOCTL_PRINTER_PWR_EN, &status);
        close(dev);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_feipulai_device_serial_IOPower_setUhfcommPwr(JNIEnv *env, jobject instance, jint status) {
    int dev = open("/dev/iodev", O_RDWR);
    if(dev > 0){
        ioctl(dev, IODEV_IOCTL_UHFCOMM_PWR_EN, &status);
        close(dev);
    }
}

// IGpioService.aidl
package android.os;

// Declare any non-default types here with import statements

interface IGpioService
{
    int gpioWrite(int gpio, int value);
    int gpioRead(int gpio);
    int gpioDirection(int gpio, int direction, int value);
    int gpioRegKeyEvent(int gpio);
    int gpioUnregKeyEvent(int gpio);
    int gpioGetNumber();
}
package com.feipulai.exam.activity.ranger.usb;

import com.feipulai.exam.activity.ranger.driver.CdcAcmSerialDriver;
import com.feipulai.exam.activity.ranger.driver.ProbeTable;
import com.feipulai.exam.activity.ranger.driver.UsbSerialProber;

/**
 * add devices here, that are not known to DefaultProber
 *
 * if the App should auto start for these devices, also
 * add IDs to app/src/main/res/xml/device_filter.xml
 */
class CustomProber {

    static UsbSerialProber getCustomProber() {
        ProbeTable customTable = new ProbeTable();
        customTable.addProduct(0x16d0, 0x087e, CdcAcmSerialDriver.class); // e.g. Digispark CDC
        return new UsbSerialProber(customTable);
    }

}

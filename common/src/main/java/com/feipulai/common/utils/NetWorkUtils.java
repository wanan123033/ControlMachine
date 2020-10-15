package com.feipulai.common.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import okhttp3.HttpUrl;

public class NetWorkUtils {
	
	/**
	 * 判断字符串是否为URL
	 *
	 * @return true:是URL、false:不是URL
	 */
	public static boolean isValidUrl(String urlStr) {
		if(TextUtils.isEmpty(urlStr)){
			return false;
		}
		HttpUrl httpUrl = HttpUrl.parse(urlStr);
		return !(httpUrl == null);
		// try {
		// 	new URL(urlStr);
		// 	return true;
		// } catch (MalformedURLException e) {
		// 	e.printStackTrace();
		// 	return false;
		// }
	}
	/**
	 * 得到有限网关的IP地址
	 *
	 * @return
	 */
	public static String getLocalIp() {

		try {
			// 获取本地设备的所有网络接口
			Enumeration<NetworkInterface> enumerationNi = NetworkInterface
					.getNetworkInterfaces();
			while (enumerationNi.hasMoreElements()) {
				NetworkInterface networkInterface = enumerationNi.nextElement();
				String interfaceName = networkInterface.getDisplayName();
				Log.i("tag", "网络名字" + interfaceName);

				// 如果是有限网卡
				if (interfaceName.equals("eth0")) {
					Enumeration<InetAddress> enumIpAddr = networkInterface
							.getInetAddresses();

					while (enumIpAddr.hasMoreElements()) {
						// 返回枚举集合中的下一个IP地址信息
						InetAddress inetAddress = enumIpAddr.nextElement();
						// 不是回环地址，并且是ipv4的地址
						if (!inetAddress.isLoopbackAddress()
								&& inetAddress instanceof Inet4Address) {
							Log.i("tag", inetAddress.getHostAddress() + "   ");

							return inetAddress.getHostAddress();
						}
					}
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}
		return "";

	}

	/**
	 * 将ip的整数形式转换成ip形式
	 *
	 * @param ipInt
	 * @return
	 */
	public static String int2ip(int ipInt) {
		StringBuilder sb = new StringBuilder();
		sb.append(ipInt & 0xFF).append(".");
		sb.append((ipInt >> 8) & 0xFF).append(".");
		sb.append((ipInt >> 16) & 0xFF).append(".");
		sb.append((ipInt >> 24) & 0xFF);
		return sb.toString();
	}

	/**
	 * 获取当前ip地址
	 *
	 * @param context
	 * @return
	 */
	public static String getLocalIpAddress(Context context) {
		try {

			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int i = wifiInfo.getIpAddress();
			return int2ip(i);
		} catch (Exception ex) {
			return " 获取IP出错鸟!!!!请保证是WIFI,或者请重新打开网络!\n" + ex.getMessage();
		}
		// return null;
	}


}

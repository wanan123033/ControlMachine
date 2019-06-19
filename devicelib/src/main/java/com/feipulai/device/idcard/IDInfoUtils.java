package com.feipulai.device.idcard;

import android.graphics.Bitmap;

import com.zkteco.android.IDReader.IDPhotoHelper;
import com.zkteco.android.IDReader.WLTService;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

/**
 * Created by James on 2018/11/13 0013.
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class IDInfoUtils{
	
	/**
	 * 获取身份证头像
	 * @return 成功时返回头像,失败时返回null
	 */
	public static Bitmap getPhotoBitmap(IDCardInfo idCardInfo){
		if(idCardInfo.getPhotolength() > 0){
			byte[] buf = new byte[WLTService.imgLength];
			if(1 == WLTService.wlt2Bmp(idCardInfo.getPhoto(),buf)){
				return IDPhotoHelper.Bgr2Bitmap(buf);
			}
		}
		return null;
	}
	
}

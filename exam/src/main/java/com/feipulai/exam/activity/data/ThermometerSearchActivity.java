package com.feipulai.exam.activity.data;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.feipulai.common.tts.TtsManager;
import com.feipulai.common.utils.ToastUtils;
import com.feipulai.common.view.baseToolbar.BaseToolbar;
import com.feipulai.device.CheckDeviceOpener;
import com.feipulai.device.ic.ICCardDealer;
import com.feipulai.device.ic.NFCDevice;
import com.feipulai.device.ic.entity.StuInfo;
import com.feipulai.exam.MyApplication;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.activity.data.adapter.ThermometerSearchAdapter;
import com.feipulai.exam.activity.jump_rope.utils.InteractUtils;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.Student;
import com.feipulai.exam.entity.StudentItem;
import com.feipulai.exam.entity.StudentThermometer;
import com.feipulai.exam.view.StuSearchEditText;
import com.zkteco.android.biometric.module.idcard.meta.IDCardInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 体温查询
 * Created by zzs on  2020/4/16
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class ThermometerSearchActivity extends BaseTitleActivity implements CheckDeviceOpener.OnCheckDeviceArrived {


    @BindView(R.id.img_portrait)
    ImageView imgPortrait;
    @BindView(R.id.txt_stu_code)
    TextView txtStuCode;
    @BindView(R.id.txt_stu_name)
    TextView txtStuName;
    @BindView(R.id.txt_stu_sex)
    TextView txtStuSex;
    @BindView(R.id.rv_result)
    RecyclerView rvResult;
    @BindView(R.id.et_input_text)
    EditText etInputText;
    private List<StudentThermometer> thermometerList = new ArrayList<>();
    private ThermometerSearchAdapter adapter;

    @Nullable
    @Override
    protected BaseToolbar.Builder setToolbar(@NonNull BaseToolbar.Builder builder) {
        return builder.setTitle("体温查询");
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_thermometer_search;
    }

    @Override
    protected void initData() {
        rvResult.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ThermometerSearchAdapter(thermometerList);
        rvResult.setAdapter(adapter);


    }

    @Override
    public void onICCardFound(NFCDevice nfcd) {
        ICCardDealer icCardDealer = new ICCardDealer(nfcd);
        StuInfo stuInfo = icCardDealer.IC_ReadStuInfo();

        if (stuInfo == null || TextUtils.isEmpty(stuInfo.getStuCode())) {
            TtsManager.getInstance().speak("读卡(ka3)失败");
            InteractUtils.toast(this, "读卡失败");
            return;
        }
        searchStudent(stuInfo.getStuCode());
    }

    @Override
    public void onIdCardRead(IDCardInfo idCardInfo) {
        Student student = DBManager.getInstance().queryStudentByIDCode(idCardInfo.getId());
        if (student == null) {
            InteractUtils.toast(this, "该考生不存在");
        } else {
            searchStudent(student.getStudentCode());
        }
    }

    @Override
    public void onQrArrived(String qrCode) {
        searchStudent(qrCode);
    }

    @Override
    public void onQRWrongLength(int length, int expectLength) {
        InteractUtils.toast(this, "条码与当前设置位数不一致,请重扫条码");
    }


    private void searchStudent(String stuCode) {

        Student student = DBManager.getInstance().queryStudentByStuCode(stuCode);
        if (student == null) {
            toastSpeak("该考生不存在");
            return;

        }
        StudentItem studentItem = DBManager.getInstance().queryStuItemByStuCode(stuCode);
        if (studentItem == null) {
            toastSpeak("无此项目");
            return;
        }
        List<StudentThermometer> dbThermometerList = DBManager.getInstance().getThermometerList(studentItem);
        thermometerList.clear();
        thermometerList.addAll(dbThermometerList);
        adapter.notifyDataSetChanged();


        txtStuCode.setText(student.getStudentCode());
        txtStuName.setText(student.getStudentName());
        txtStuSex.setText(student.getSex() == 0 ? "男" : "女");
//        if (TextUtils.isEmpty(student.getPortrait())) {
//            imgPortrait.setImageResource(R.mipmap.icon_head_photo);
//        } else {
//            imgPortrait.setImageBitmap(student.getBitmapPortrait());
//        }
        Glide.with(imgPortrait.getContext()).load(MyApplication.PATH_IMAGE + student.getStudentCode() + ".jpg")
                .error(R.mipmap.icon_head_photo).placeholder(R.mipmap.icon_head_photo).into(imgPortrait);

    }


    @OnClick(R.id.btn_query)
    public void onViewClicked() {
        String input = etInputText.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            ToastUtils.showShort("请输入学生考号");
        } else if (!StuSearchEditText.patternStuCode(input)) {
            ToastUtils.showShort("请输入正常学生考号");
        } else {
            searchStudent(input);
        }
    }
}

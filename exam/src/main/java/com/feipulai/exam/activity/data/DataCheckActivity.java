package com.feipulai.exam.activity.data;

import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.feipulai.common.db.DataBaseExecutor;
import com.feipulai.common.db.DataBaseRespon;
import com.feipulai.common.db.DataBaseTask;
import com.feipulai.common.utils.HandlerUtil;
import com.feipulai.common.utils.print.PrintBean;
import com.feipulai.exam.R;
import com.feipulai.exam.activity.base.BaseTitleActivity;
import com.feipulai.exam.db.DBManager;
import com.feipulai.exam.entity.RoundResult;
import com.feipulai.exam.utils.EncryptUtil;

import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DataCheckActivity extends BaseTitleActivity {

    @BindView(R.id.progress_storage)
    ProgressBar progressStorage;
    @BindView(R.id.txt_page)
    TextView txtPage;
    @BindView(R.id.txt_total)
    TextView txtTotal;
    @BindView(R.id.rvData)
    RecyclerView rvData;
    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_data_check;
    }

    @Override
    protected void initData() {

    }

    private void getAllResult() {
        DataBaseExecutor.addTask(new DataBaseTask() {
            @Override
            public DataBaseRespon executeOper() {
                List<RoundResult> resultList = DBManager.getInstance().queryAllRoundResult();
                return new DataBaseRespon(true, "", resultList);
            }

            @Override
            public void onExecuteSuccess(DataBaseRespon respon) {
                List<RoundResult> resultList = (List<RoundResult>) respon.getObject();
                txtPage.setText("1");
                txtTotal.setText(resultList.size() + "");
                progressStorage.setMax(resultList.size());
                progressStorage.setProgress(0);
            }

            @Override
            public void onExecuteFail(DataBaseRespon respon) {

            }
        });
    }

    private void checkResult(final List<RoundResult> resultList) {
        DataBaseExecutor.addTask(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < resultList.size(); i++) {

                    RoundResult roundResult = resultList.get(i);
                    String checkData = EncryptUtil.setDecodeData(roundResult.getRemark3(), new SecretKeySpec(RoundResult.ENCRYPT_KEY.getBytes(), "AES"));
//                    String encryptData = allScore.getStudentCode() + "," + allScore.getItemCode() + "," + allScore.getExamType()
//                            + "," + allScore.getResult() + "," + allScore.getResultState() + "," + allScore.getTestTime();
                    String decodeData[] = checkData.split(",");
                    String stuCode = decodeData[0];
                    String itemCode = decodeData[1];
                    int examType = Integer.parseInt(decodeData[2]);
                    int result = Integer.parseInt(decodeData[3]);
                    int resultState = Integer.parseInt(decodeData[4]);
                    String testTime = decodeData[5];

                    HandlerUtil.sendMessage(mHandler, i);
                }
            }
        });
    }

    @Override
    protected void handleMessage(Message msg) {
        super.handleMessage(msg);
        int progress = msg.what;
        txtPage.setText(progress + "");
        progressStorage.setProgress(progress);
    }

    @OnClick(R.id.btnDataCheck)
    public void onViewClicked() {
    }
}

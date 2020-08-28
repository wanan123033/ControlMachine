package com.feipulai.common.utils.print;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.feipulai.common.R;
import com.feipulai.common.utils.DateUtil;
import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zzs on  2020/8/13
 * 深圳市菲普莱体育发展有限公司   秘密级别:绝密
 */
public class PrintA4Util {

    private Context mContext;
    private ImageView imgCode;
    private TextView txtTitle;
    private TextView txtHandName;
    private TextView txtStartTime;
    private TextView txtTilte1;
    private TextView txtTilte2;
    private TextView txtTilte3;
    private TextView txtTilte4;
    private TextView txtTilte5;
    private TextView txtTilte6;
    private TextView txtTilte7;
    private TextView txtTilte8;
    private RecyclerView rvData;
    private TextView txtBottom1;
    private TextView txtBottom2;
    private TextView txtBottom3;
    private TextView txtBottom4;
    private TextView txtPrintTime;
    private NestedScrollView viewContent;
    private List<PrintBean.PrintDataBean> printDataBeans = new ArrayList<>();
    private List<String> printImgPathList = new ArrayList<>();
    private PrintDataAdapter adapter;
    private View view;

    public List<String> getPrintImgList() {
        return printImgPathList;
    }


    public PrintA4Util(Context mContext) {
        this.mContext = mContext;
//        initView();
    }

    private void initView() {
        view = LayoutInflater.from(mContext).inflate(R.layout.view_a4_print, null);
        viewContent = view.findViewById(R.id.view_content);
        imgCode = view.findViewById(R.id.img_code);
        txtTitle = view.findViewById(R.id.txt_title);
        txtHandName = view.findViewById(R.id.txt_hand_name);
        txtStartTime = view.findViewById(R.id.txt_start_time);
        txtTilte1 = view.findViewById(R.id.txt_title1);
        txtTilte2 = view.findViewById(R.id.txt_title2);
        txtTilte3 = view.findViewById(R.id.txt_title3);
        txtTilte4 = view.findViewById(R.id.txt_title4);
        txtTilte5 = view.findViewById(R.id.txt_title5);
        txtTilte6 = view.findViewById(R.id.txt_title6);
        txtTilte7 = view.findViewById(R.id.txt_title7);
        txtTilte8 = view.findViewById(R.id.txt_title8);
        rvData = view.findViewById(R.id.rv_data);
        txtBottom1 = view.findViewById(R.id.txt_bottom1);
        txtBottom2 = view.findViewById(R.id.txt_bottom2);
        txtBottom3 = view.findViewById(R.id.txt_bottom3);
        txtBottom4 = view.findViewById(R.id.txt_bottom4);
        txtPrintTime = view.findViewById(R.id.txt_print_time);
        rvData.setLayoutManager(new LinearLayoutManager(mContext));
        adapter = new PrintDataAdapter(mContext, printDataBeans);
        rvData.setAdapter(adapter);
    }

    private void setData(PrintBean printBean) {
        txtTitle.setText(printBean.getTitle());
        imgCode.setImageBitmap(QRCodeUtil.createQRCodeBitmap(printBean.getCodeData(), 260));
        //表格头部
        if (printBean.getPrintTableHand() == null) {
            printBean.setPrintTableHand(mContext.getResources().getStringArray(R.array.print_table_hand));
        }
        if (printBean.getPrintTableHand()[0] == null) {
            txtTilte1.setText("");
            txtTilte1.setVisibility(View.GONE);
        } else {
            txtTilte1.setText(printBean.getPrintTableHand()[0]);
            txtTilte1.setVisibility(View.VISIBLE);
        }
        if (printBean.getPrintTableHand()[1] == null) {
            txtTilte2.setText("");
            txtTilte2.setVisibility(View.GONE);
        } else {
            txtTilte2.setText(printBean.getPrintTableHand()[1]);
            txtTilte2.setVisibility(View.VISIBLE);
        }
        if (printBean.getPrintTableHand()[2] == null) {
            txtTilte3.setText("");
            txtTilte3.setVisibility(View.GONE);
        } else {
            txtTilte3.setText(printBean.getPrintTableHand()[2]);
            txtTilte3.setVisibility(View.VISIBLE);
        }
        if (printBean.getPrintTableHand()[3] == null) {
            txtTilte4.setText("");
            txtTilte4.setVisibility(View.GONE);
        } else {
            txtTilte4.setText(printBean.getPrintTableHand()[3]);
            txtTilte4.setVisibility(View.VISIBLE);
        }
        if (printBean.getPrintTableHand()[4] == null) {
            txtTilte5.setText("");
            txtTilte5.setVisibility(View.GONE);
        } else {
            txtTilte5.setText(printBean.getPrintTableHand()[4]);
            txtTilte5.setVisibility(View.VISIBLE);
        }
        if (printBean.getPrintTableHand()[5] == null) {
            txtTilte6.setText("");
            txtTilte6.setVisibility(View.GONE);
        } else {
            txtTilte6.setText(printBean.getPrintTableHand()[5]);
            txtTilte6.setVisibility(View.VISIBLE);
        }
        if (printBean.getPrintTableHand()[6] == null) {
            txtTilte7.setText("");
            txtTilte7.setVisibility(View.GONE);
        } else {
            txtTilte7.setText(printBean.getPrintTableHand()[6]);
            txtTilte7.setVisibility(View.VISIBLE);
        }
        if (printBean.getPrintTableHand()[7] == null) {
            txtTilte8.setText("");
            txtTilte8.setVisibility(View.GONE);
        } else {
            txtTilte8.setText(printBean.getPrintTableHand()[7]);
            txtTilte8.setVisibility(View.VISIBLE);
        }
        //底部显示
        if (printBean.getPrintBottom() == null) {
            printBean.setPrintBottom(mContext.getResources().getStringArray(R.array.print_table_bottom));
        }
        if (printBean.getPrintBottom()[0] == null) {
            txtBottom1.setText("");
            txtBottom1.setVisibility(View.GONE);
        } else {
            txtBottom1.setText(printBean.getPrintBottom()[0] + ":");
            txtBottom1.setVisibility(View.VISIBLE);
        }
        if (printBean.getPrintBottom()[1] == null) {
            txtBottom2.setText("");
            txtBottom2.setVisibility(View.GONE);
        } else {
            txtBottom2.setText(printBean.getPrintBottom()[1] + ":");
            txtBottom2.setVisibility(View.VISIBLE);
        }
        if (printBean.getPrintBottom()[2] == null) {
            txtBottom3.setText("");
            txtBottom3.setVisibility(View.GONE);
        } else {
            txtBottom3.setText(printBean.getPrintBottom()[2] + ":");
            txtBottom3.setVisibility(View.VISIBLE);
        }
        if (printBean.getPrintBottom()[3] == null) {
            txtBottom4.setText("");
            txtBottom4.setVisibility(View.GONE);
        } else {
            txtBottom4.setText(printBean.getPrintBottom()[3] + ":");
            txtBottom4.setVisibility(View.VISIBLE);
        }

        //打印头
        if (TextUtils.isEmpty(printBean.getPrintHand())) {
            txtHandName.setText("");
            txtHandName.setVisibility(View.INVISIBLE);
        } else {
            txtHandName.setText(printBean.getPrintHand());
            txtHandName.setVisibility(View.VISIBLE);
        }
        if (TextUtils.isEmpty(printBean.getPrintHandRight())) {
            txtStartTime.setText("");
            txtStartTime.setVisibility(View.INVISIBLE);
        } else {
            txtStartTime.setText(printBean.getPrintHandRight());
            txtStartTime.setVisibility(View.VISIBLE);
        }

        txtPrintTime.setText("打印时间：" + DateUtil.getCurrentTime2("yyyy/MM/dd  HH:mm:ss"));


    }

    public void createPrintFile(PrintBean printBean, String filePath, String fileName) {
        if (printBean.getPrintDataBeans() != null) {
            printImgPathList.clear();

            int pageSum;
            if (printBean.getPrintDataBeans().size() % 20 == 0) {
                pageSum = printBean.getPrintDataBeans().size() / 20;
            } else {
                pageSum = (printBean.getPrintDataBeans().size() / 20) + 1;
            }
            for (int pageNo = 0; pageNo < pageSum; pageNo++) {
                initView();
                adapter.setLineShow(printBean.getPrintTableHand());
                setData(printBean);

                printDataBeans.clear();
                if (pageNo == pageSum - 1) {
                    printDataBeans.addAll(printBean.getPrintDataBeans().subList(pageNo * 20, printBean.getPrintDataBeans().size()));
                } else {
                    printDataBeans.addAll(printBean.getPrintDataBeans().subList(pageNo * 20, (pageNo + 1) * 20));
                }
                adapter.notifyDataSetChanged();
                DisplayMetrics metric = new DisplayMetrics();
                ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metric);
                int width = metric.widthPixels;     // 屏幕宽度（像素）
                int height = metric.heightPixels;   // 屏幕高度（像素）

                ViewImageUtils.layoutView(view, width, height);
                String imgPath = ViewImageUtils.viewSaveToImage(viewContent, filePath, fileName + "_" + pageNo);
                if (!TextUtils.isEmpty(imgPath)) {
                    printImgPathList.add(imgPath);
                }

            }
            try {
                PdfUtil.createPdf(filePath + fileName + ".pdf", printImgPathList);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
    }
}

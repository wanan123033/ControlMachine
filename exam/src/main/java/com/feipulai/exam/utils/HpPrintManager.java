package com.feipulai.exam.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.print.PrintAttributes;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.feipulai.exam.R;
import com.hp.mss.hpprint.model.ImagePrintItem;
import com.hp.mss.hpprint.model.PDFPrintItem;
import com.hp.mss.hpprint.model.PrintItem;
import com.hp.mss.hpprint.model.PrintJobData;
import com.hp.mss.hpprint.model.asset.ImageAsset;
import com.hp.mss.hpprint.model.asset.PDFAsset;
import com.hp.mss.hpprint.util.PrintUtil;

import java.io.IOException;

public class HpPrintManager {
    private Context context;
    private PrintItem.ScaleType scaleType = PrintItem.ScaleType.CENTER_TOP;
    private PrintAttributes.Margins margins = new PrintAttributes.Margins(0, 0, 0, 0);
    private String contentType = CONTENT_TYPE_PDF;
    private Uri userPickedUri;

    public static String MIME_TYPE_PDF = "application/pdf";
    public static String MIME_TYPE_IMAGE_PREFIX = "image/";
    public static String CONTENT_TYPE_PDF = "PDF";
    public static String CONTENT_TYPE_IMAGE = "Image";

    private PrintJobData printJobData;
    PrintAttributes.MediaSize mediaSize5x7;
    private HpPrintManager(Activity context){
        this.context = context;
        mediaSize5x7 = new PrintAttributes.MediaSize("na_5x7_5x7in", "5 x 7", 5000, 7000);
        PrintUtil.doNotEncryptDeviceId = true;
    }

    public void print(Uri uri) {
        userPickedUri = uri;
        createPrintJobData();
        PrintUtil.setPrintJobData(printJobData);
//        PrintUtil.sendPrintMetrics = showMetricsDialog;
        PrintUtil.print((Activity) context);
    }

    private void createPrintJobData() {
        createUserSelectedPDFJobData();

        //Giving the print job a name.
        printJobData.setJobName("Example");

        //Optionally include print attributes.
        PrintAttributes printAttributes = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                .build();
        printJobData.setPrintDialogOptions(printAttributes);

    }
    private String getMimeType(Uri uri) {
        Uri returnUri = uri;
        return context.getContentResolver().getType(returnUri);
    }
    private void createUserSelectedImageJobData() {
        Bitmap userPickedBitmap;

        try {
            userPickedBitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), userPickedUri);
            int width = userPickedBitmap.getWidth();
            int height = userPickedBitmap.getHeight();

            // if user picked bitmap is too big, just reduce the size, so it will not chock the print plugin
            if (width * height > 5000) {
                width = width / 2;
                height = height / 2;
                userPickedBitmap = Bitmap.createScaledBitmap(userPickedBitmap, width, height, true);
            }

            DisplayMetrics mDisplayMetric = context.getResources().getDisplayMetrics();
            float widthInches =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, width, mDisplayMetric);
            float heightInches =  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, height, mDisplayMetric);

            ImageAsset imageAsset = new ImageAsset(context,
                    userPickedBitmap,
                    ImageAsset.MeasurementUnits.INCHES,
                    widthInches, heightInches);

            PrintItem printItem4x6 = new ImagePrintItem(PrintAttributes.MediaSize.NA_INDEX_4X6,margins, scaleType, imageAsset);
            PrintItem printItem85x11 = new ImagePrintItem(PrintAttributes.MediaSize.NA_LETTER,margins, scaleType, imageAsset);
            PrintItem printItem5x7 = new ImagePrintItem(mediaSize5x7,margins, scaleType, imageAsset);

            printJobData = new PrintJobData(context, printItem4x6);
            printJobData.addPrintItem(printItem85x11);
            printJobData.addPrintItem(printItem5x7);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void createUserSelectedPDFJobData() {
//        try {
////            FileInputStream input = new FileInputStream(file);
//            InputStream input=context.getContentResolver().openInputStream(userPickedUri);
//        } catch (FileNotFoundException e) {
//            Log.e("---", "No File", e);
//        }
//        Bitmap userPickedBitmap;

//        try {
//        userPickedBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), userPickedUri);
//        int width = userPickedBitmap.getWidth();
//        int height = userPickedBitmap.getHeight();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        PDFAsset pdfAsset = new PDFAsset(userPickedUri, false);

        PrintItem printItem4x6 = new PDFPrintItem(PrintAttributes.MediaSize.NA_INDEX_4X6, margins, scaleType, pdfAsset);
        PrintItem printItem5x7 = new PDFPrintItem(mediaSize5x7, margins, scaleType, pdfAsset);
        PrintItem printItemLetter = new PDFPrintItem(PrintAttributes.MediaSize.NA_LETTER, margins, scaleType, pdfAsset);

        printJobData = new PrintJobData(context, printItem4x6);

        printJobData.addPrintItem(printItemLetter);
        printJobData.addPrintItem(printItem5x7);

    }
    private void createDefaultPrintJobData() {

        if(contentType.equals("Image")) {
            //Create image assets from the saved files.
            ImageAsset imageAsset4x5 = new ImageAsset(context, R.drawable.t4x5, ImageAsset.MeasurementUnits.INCHES, 4, 5);
            ImageAsset imageAsset4x6 = new ImageAsset(context, R.drawable.t4x6, ImageAsset.MeasurementUnits.INCHES, 4, 6);
            ImageAsset imageAsset5x7 = new ImageAsset(context, R.drawable.t5x7, ImageAsset.MeasurementUnits.INCHES, 5, 7);
            ImageAsset assetdirectory = new ImageAsset(context, "t8.5x11.png", ImageAsset.MeasurementUnits.INCHES, 8.5f, 11f);


            //Create printitems from the assets. These define what asset is to be used for each media size.
            PrintItem printItem4x6 = new ImagePrintItem(PrintAttributes.MediaSize.NA_INDEX_4X6,margins, scaleType, imageAsset4x6);
            PrintItem printItem85x11 = new ImagePrintItem(PrintAttributes.MediaSize.NA_LETTER,margins, scaleType, assetdirectory);
            PrintItem printItem5x7 = new ImagePrintItem(mediaSize5x7,margins, scaleType, imageAsset5x7);
            PrintItem printItem5x8 = new ImagePrintItem(PrintAttributes.MediaSize.NA_INDEX_5X8,margins, scaleType, imageAsset4x5);

            //Create the printJobData with the default print item
            PrintItem printItemDefault = new ImagePrintItem(margins, scaleType, imageAsset4x5);
            printJobData = new PrintJobData(context, printItemDefault);

            //Lastly, add all the printitems to the print job data.
            printJobData.addPrintItem(printItem4x6);
            printJobData.addPrintItem(printItem85x11);
            printJobData.addPrintItem(printItem5x7);
            printJobData.addPrintItem(printItem5x8);


        } else {
            PDFAsset pdf4x6 = new PDFAsset("4x6.pdf", true);
            PDFAsset pdf5x7 = new PDFAsset("5x7.pdf", true);
            PDFAsset pdfletter = new PDFAsset("8.5x11.pdf", true);

//            PDFAsset pdfletter = null;
//            try {
//                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getCanonicalPath();
//                File file = new File(path + "/HP_PRINT_ITEM_ORIGINAL_3.pdf");
//                String s = file.getCanonicalPath();
//                pdfletter = new PDFAsset(s);
//            } catch (IOException e) {
//                Log.e("MainActivity", "Unable to create path string.");
//            }

            PrintItem printItem4x6 = new PDFPrintItem(PrintAttributes.MediaSize.NA_INDEX_4X6,margins, scaleType, pdf4x6);
            PrintItem printItem5x7 = new PDFPrintItem(mediaSize5x7,margins, scaleType, pdf5x7);
            PrintItem printItemLetter = new PDFPrintItem(PrintAttributes.MediaSize.NA_LETTER,margins, scaleType, pdfletter);

            printJobData = new PrintJobData(context, printItem4x6);

            printJobData.addPrintItem(printItemLetter);
            printJobData.addPrintItem(printItem5x7);
        }

    }


    private static HpPrintManager printManager;
    public static synchronized HpPrintManager getInstance(Context context){
        if (printManager == null){
            printManager = new HpPrintManager((Activity) context);
        }
        return printManager;
    }
}

package cdfy.tesscv;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;

import com.googlecode.tesseract.android.TessBaseAPI;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.opencv.imgproc.Imgproc.CV_MEDIAN;

public class tesscv {
    private final static String TAG = "TessCV";
    private Bitmap              image;                      // The path of phone image
    private TessBaseAPI         API;               // Tesseract API reference
    private String              datapath;                   // The path to folder containing language data file
    private final static String language = "letsgodigital";  // The default language of tesseract
    private InputStream         input;


    public tesscv(Bitmap phone, InputStream instream) {
        image = phone;
        input = instream;

        datapath = Environment.getExternalStorageDirectory().toString() + "/MyLibApp/tesscv/tesseract";// initial tesseract-ocr
        checkFile(new File(datapath + "/tessdata")); // make sure training data has been copied

        API = new TessBaseAPI();
        API.init(datapath, language);

        API.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK);// set psm mode
        API.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789.");// set up a whitelist
    }

    private void saveTmpImage(String name, Mat image) {
        Mat img = image.clone();
        if (img.channels() == 3) {
            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2RGBA);
        }
        Bitmap bmp = null;
        try {
            bmp = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(img, bmp);
        } catch (CvException e) {
            Log.d("mat2bitmap", e.getMessage());
        }
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MyLibApp/tesscv");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("saveTmpImage", "failed to create directory");
                return;
            }
        }

        File dest = new File(mediaStorageDir.getPath() + File.separator + name + ".png");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(dest);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getOcrOfBitmap(ImageView imageView) {
        if (image == null) {
            return "";
        }
        Mat imgBgra = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(image, imgBgra);
        Imgproc.cvtColor(imgBgra, imgBgra, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imgBgra, imgBgra, new Size(3, 3), 0);
        Imgproc.threshold(imgBgra, imgBgra, 0, 255, Imgproc.THRESH_OTSU);

        //Imgproc.medianBlur(imgBgra,imgBgra, 5);
        //Imgproc.adaptiveThreshold(imgBgra, imgBgra, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2);



        //saveTmpImage("srcInputBitmap", img);

//        if (img.empty()) {
//            return "";
//        }
//        if (img.channels()==3) {
//            Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
//        }
        Utils.matToBitmap(imgBgra,image);
        imageView.setImageBitmap(image);
        return getResOfTesseractReg(imgBgra);
    }

    private String getResOfTesseractReg(Mat img) {
        String res;
        if (img.empty()) {
            return "";
        }
        byte[] bytes = new byte[(int)(img.total()*img.channels())];
        img.get(0, 0, bytes);
        API.setImage(bytes, img.cols(), img.rows(), 1, img.cols());
        res = API.getUTF8Text();
        return res;
    }

    private void checkFile(File dir) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists() && dir.mkdirs()){
            copyFiles();
        }
        //The directory exists, but there is no data file in it
        if(dir.exists()) {
            String datafilepath = dir.toString() + "/letsgodigital.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFiles();
            }
        }
    }

    private void copyFiles() {
        try {
            if (input == null) {
                //TODO
                String resInPath = "/tessdata/letsgodigital.traineddata";
                //Log.d(TAG, "copyFiles: resInPath " + resInPath);
                input = new FileInputStream(resInPath);
            }

            String resOutPath = datapath + "/tessdata/letsgodigital.traineddata";//location we want the file to be a
            OutputStream outstream = new FileOutputStream(resOutPath);//open byte streams for writing
            byte[] buffer = new byte[1024];//copy the file to the location specified by filepath
            int read;
            while ((read = input.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

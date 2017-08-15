package cdfy.dualmaut;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import cdfy.tesscv.tesscv;
import java.io.IOException;
import java.io.InputStream;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity {
    public static final String IMAGE_UNSPECIFIED = "image/*";
    public static final int PHOTOALBUM = 1;   // album
    Button photo_album = null;                // album
    ImageView imageView = null;               // Intercept the image
    TextView textView = null;                 // OCR Identify the results
    Bitmap m_phone;                           // Bitmap image
    String m_ocrOfBitmap;                     // Bitmap image OCR Identify the results
    InputStream m_instream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageID);
        photo_album = (Button) findViewById(R.id.photo_album);
        textView = (TextView) findViewById(R.id.OCRTextView);

        photo_album.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
                startActivityForResult(intent, PHOTOALBUM);
            }
        });
        AssetManager assetManager = getAssets();//get access to AssetManager
        try {
            m_instream = assetManager.open("tessdata/letsgodigital.traineddata");//open byte streams for reading/writing
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0 || data == null) {
            return;
        }
        if (requestCode == PHOTOALBUM) {
            Uri image = data.getData();
            try {
                m_phone = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (OpenCVLoader.initDebug()) {
            tesscv jmi = new tesscv(m_phone, m_instream);// do some opencv stuff
            m_ocrOfBitmap = jmi.getOcrOfBitmap(imageView);
        }
        //imageView.setImageBitmap(m_phone);
        textView.setText(m_ocrOfBitmap);
        super.onActivityResult(requestCode, resultCode, data);
    }

}

package com.example.ivshravan.imageprocessing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileInputStream;

/*
 * The following class/Activity takes picture of text, stores it in local storage
 * and processes the image to extract text.
 * */
public class MainActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 1;
    private static final String image_name = "scanner.png";  // Name of image to be stored
    private static TextView result;
    private static ImageView imageView;

    
    /* Scanner Button Handler. */
    public void bh_camera(View view) {

		 /* Get Camera. */
        File file = new File(Environment.getExternalStorageDirectory(), image_name);
        Uri outuri = Uri.fromFile(file);

        /* Open the Scanner */
        final Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri);

        /*
           Start the Camera and store the image with name "scanner.jpg"
           in root (Internal Storage) directory.
         */
        startActivityForResult(camera_intent, SELECT_PICTURE);
    }
    
    

    /*
     * Function the uses tess-two to scan the image and extract
     * text*/
    private void imageScanner() {

        String recognizedText = "Default-None";
        String path = "";
        Bitmap bitmap = null;
        FileInputStream fis;

        try {
            path = new File(Environment.getExternalStorageDirectory(), image_name).getAbsolutePath();
            fis = new FileInputStream(path);
            bitmap = BitmapFactory.decodeStream(fis);
        }
        catch (Exception e) {
            result.setText(e.getMessage());
            return;
        }


        /* Perform rotation of image in a way convenient for text extraction by tess-two. */
        try {
            ExifInterface exif = new ExifInterface(path);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            int rotate = 0;

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
            }

            if (rotate != 0) {

                // Getting width & height of the given image.
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();

                // Setting pre rotate
                Matrix mtx = new Matrix();
                mtx.preRotate(0);

                // Rotating Bitmap
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
            }
            // Convert to ARGB_8888, required by tess
           // bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        } catch (Exception e) {
            result.setText(e.getMessage());
            return;
        }

        imageView.setImageBitmap(bitmap);


        try {
            // Tesseract APIS.
            TessBaseAPI baseApi = new TessBaseAPI();
            baseApi.init(Environment.getExternalStorageDirectory().toString(), "eng");
            baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
            baseApi.setImage(bitmap);
            recognizedText = baseApi.getUTF8Text();
            baseApi.end();
        }
        catch (Exception e) {
            result.setText(e.getMessage());
            return;
        }

        /* Finally display the extracted text. */
        result.setText(recognizedText);
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        result.setText("Scanning Image...");
        imageScanner();
    }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result = (TextView) findViewById(R.id.resultTV);
        imageView=(ImageView)findViewById(R.id.imageView);
    }
}


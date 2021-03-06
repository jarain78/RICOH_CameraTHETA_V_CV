package com.example.opencvsample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theta360.pluginlibrary.activity.PluginActivity;
import com.theta360.pluginlibrary.callback.KeyCallback;
import com.theta360.pluginlibrary.receiver.KeyReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.theta4j.osc.CommandResponse;
import org.theta4j.osc.CommandState;
import org.theta4j.webapi.TakePicture;
import org.theta4j.webapi.Theta;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.ByteArrayOutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;

// Post Image to Analize
import java.io.FileInputStream;

import WebServerCommunication.WebServerCommunication;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    /**
     * if you have a real camera, uncomment the lines below
     * */
//public class MainActivity extends PluginActivity {
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        setKeyCallback(keyCallback);
//        Log.d("THETADEBUG", "set key callback");
//        if (isApConnected()) {
//        }
//    }
///////////////////////////////////////////
// End comment section
///////////////////////////////////////////

    // load native library
    static {
        System.loadLibrary("opencvsample");
    }

    // jarain78
    //-----------------------------------------------------------------------------------------

    Button analyzeImageButton;
    CheckBox sizecheckBox;
    CheckBox formatCheckBox;
    TextView object_detected;
    URL inputFileUrl;
    int conta_webp_image = 0;
    String url = "http://YOUR HOST/";
    String[] service = {"get_image_objects", "image"};
    int conta_pro = 0;

    //-----------------------------------------------------------------------------------------


    Button takePictureButton;
    ImageView thetaImageView;
    TextView statusTextView;
    // on the RICOH THETA V, there is no function button. People often use the
    // wifi button on the side of the camera to process images or change settings
    Button processButton;
    String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
    String basepath = extStorageDirectory + "/DCIM/100RICOH/";

    String picturePath;
    private ExecutorService imageExecutor = Executors.newSingleThreadExecutor();
    private ExecutorService thetaExecutor = Executors.newSingleThreadExecutor();

    String thetaImagePath = null;


    private final String TAG = "THETADEBUG";

    int imageNumber = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        thetaImageView = findViewById(R.id.thetaImageId);
        takePictureButton = findViewById(R.id.takePictueButtonId);

        // Jarain78
        analyzeImageButton = findViewById(R.id.analyzeImagaeButtonId);
        sizecheckBox = findViewById(R.id.sizecheckBoxId);
        formatCheckBox = findViewById(R.id.formatCheckBoxId);
        object_detected = findViewById(R.id.objectDetecteId);


        //-------

        processButton = findViewById(R.id.processButtonId);
        thetaImageView = findViewById(R.id.thetaImageId);
        thetaImageView.setImageResource(R.drawable.theta);

        checkPermission();

        File thetaMediaDir = new File(basepath);
        if (!thetaMediaDir.exists()) {
            thetaMediaDir.mkdirs();
        }

        // Jarain78
        //-----------------------------------------------------------------------------------------

        analyzeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            post_image_to_ws(thetaImagePath);
                            post_image_to_get_objects(thetaImagePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        sizecheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        getBitmap800_400(thetaImagePath);

                    }
                }).start();
            }
        });

        formatCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        Bitmap new_image = getBitmap400_200(thetaImagePath);
                        change_compress_format(new_image);
                    }
                }).start();
            }
        });

        //-----------------------------------------------------------------------------------------

        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picturePath = takeThetaPicture();
                Log.d(TAG, "received image path " + picturePath);

                /**
                 * Call your image processing or file transfer method here or
                 * trigger it with a button press.
                 * If you want to process your image when the picture is taken,
                 * uncomment the line below.
                 */
                // processImage(picturePath);


            }
        });

        processButton.setOnClickListener(new View.OnClickListener() {
            /**
             * This section is only if you want to trigger your image
             * processing or file transfer when a button is pressed
             * on the camera.  If you start the image process when the
             * picture is taken, you can delete the entire method.
             * @param v
             */
            @Override
            public void onClick(View v) {
                processImage(picturePath);
                Toast.makeText(MainActivity.this, "Processed image: " +
                        picturePath, Toast.LENGTH_LONG).show();
            }
        });

    }

    // native functions
    public native String version();

    public native byte[] rgba2bgra(int width, int height, byte[] src);

    // Jarain78
    public native byte[] processing(int width, int height, byte[] src);

    // Jarain78
    public native byte[] flipimage(int width, int height, byte[] src);


    // Post Image
    //WebServerCommunication postImageToImaggaAsync = new WebServerCommunication();


    private void post_image_to_ws(String thetaPicturePath) throws IOException {
        Response responses;

        File file = new File(picturePath);
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);


        Request request = new Request.Builder()
                .url(url + service[0])
                .post(fileReqBody)
                .build();

        OkHttpClient client = new OkHttpClient();

        try {
            responses = client.newCall(request).execute();
            System.out.println(responses.body());
            final Bitmap bitmap = BitmapFactory.decodeStream(responses.body().byteStream());

            //
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    thetaImageView.setImageBitmap(bitmap);
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void post_image_to_get_objects(String thetaPicturePath) throws IOException {
        Response responses;

        File file = new File(picturePath);
        RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);


        Request request = new Request.Builder()
                .url(url + service[1])
                .post(fileReqBody)
                .build();

        OkHttpClient client = new OkHttpClient();

        try {
            responses = client.newCall(request).execute();
            show_objects_names(responses.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void show_objects_names(String responses) {

        new Thread(new Runnable() {
            public void run() {
                try {

                    JSONArray Jarray = new JSONArray(responses);
                    String label_names = " ";

                    for (int i = 0; i < Jarray.length(); i++) {
                        JSONObject jsonobject = Jarray.getJSONObject(i);
                        label_names = jsonobject.getString("label_names");
                        System.out.println(label_names);
                        object_detected.setText(label_names);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    // Img Processing
    private void processImage(String thetaPicturePath) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // With out cv
        //-----------------------------------------------------------------------------------------
        /* You can change the format to WEBP. You also need to change
        Bitmap.CompressFormat below*/
        // File myExternalFile = new File(basepath + "PROCESSED_IMAGE.WEBP");
        //File myExternalFile = new File(basepath + "PROCESSED_IMAGE.PNG");
        //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        //Bitmap bitmap = getBitmap(thetaPicturePath);

        //-----------------------------------------------------------------------------------------

        // load the picture from the drawable resource
        // Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.park);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Log.d(TAG, thetaPicturePath);
        Bitmap img = BitmapFactory.decodeFile(thetaPicturePath, options);

        // call the process from the native library
        if (conta_pro == 0) {
            // get the byte array from the Bitmap instance
            ByteBuffer byteBuffer = ByteBuffer.allocate(img.getByteCount());
            img.copyPixelsToBuffer(byteBuffer);
            // set the output image on an ImageView
            Bitmap bmp = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
            byte[] dst = rgba2bgra(img.getWidth(), img.getHeight(), byteBuffer.array());
            object_detected.setText("RGB2BGRA");
            bmp.copyPixelsFromBuffer(ByteBuffer.wrap(dst));
            thetaImageView.setImageBitmap(bmp);
            conta_pro = conta_pro + 1;

        } else if (conta_pro == 1) {

            // get the byte array from the Bitmap instance
            ByteBuffer byteBuffer = ByteBuffer.allocate(img.getByteCount());
            img.copyPixelsToBuffer(byteBuffer);
            // set the output image on an ImageView
            Bitmap bmp = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
            byte[] dst = flipimage(img.getWidth(), img.getHeight(), byteBuffer.array());
            object_detected.setText("FLIP IMAGE");
            bmp.copyPixelsFromBuffer(ByteBuffer.wrap(dst));
            thetaImageView.setImageBitmap(bmp);
            conta_pro = conta_pro + 1;

        } else if (conta_pro == 2) {

            // get the byte array from the Bitmap instance
            ByteBuffer byteBuffer = ByteBuffer.allocate(img.getByteCount());
            img.copyPixelsToBuffer(byteBuffer);
            // set the output image on an ImageView
            Bitmap bmp = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
            byte[] dst = processing(img.getWidth(), img.getHeight(), byteBuffer.array());
            object_detected.setText("BLUR FILTER");
            bmp.copyPixelsFromBuffer(ByteBuffer.wrap(dst));
            thetaImageView.setImageBitmap(bmp);
            conta_pro = conta_pro + 1;

        } else {
            conta_pro = 0;
        }


    }

    // Jarain78
    //----------------------------------------------------------------------------------------------
    private Bitmap getBitmap400_200(String photoPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Log.d(TAG, photoPath);
        Bitmap imgTheta = BitmapFactory.decodeFile(photoPath, options);
        ByteBuffer byteBufferTheta = ByteBuffer.allocate(imgTheta.getByteCount());
        imgTheta.copyPixelsToBuffer(byteBufferTheta);
        Bitmap bmpTheta = Bitmap.createScaledBitmap(imgTheta, 400, 200, true);
        return bmpTheta;
    }

    // resize the image
    private void getBitmap800_400(String photoPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Log.d(TAG, photoPath);
        Bitmap imgTheta = BitmapFactory.decodeFile(photoPath, options);
        ByteBuffer byteBufferTheta = ByteBuffer.allocate(imgTheta.getByteCount());
        imgTheta.copyPixelsToBuffer(byteBufferTheta);
        Bitmap bmpTheta = Bitmap.createScaledBitmap(imgTheta, 800, 400, true);
        save_resized_image(bmpTheta);
        //thetaImageView.setImageBitmap(bmpTheta);
    }

    // save the new image with the new size as png
    private void save_resized_image(Bitmap bitmap) {
        File myExternalFile = new File(basepath + "image_800_400.PNG");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // bitmap.compress should be put on different thread
        imageExecutor.submit(() -> {
            // you can change the compress format to WEBP in the line below
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);

            try {
                Log.d(TAG, "New File Url: " + myExternalFile);
                FileOutputStream fos = new FileOutputStream(myExternalFile);
                fos.write(byteArrayOutputStream.toByteArray());
                fos.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    // change the compress format
    private void change_compress_format(Bitmap bitmap) {
        File myExternalFile = new File(basepath + "change_compress_format.WEBP");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // bitmap.compress should be put on different thread
        imageExecutor.submit(() -> {
            // you can change the compress format to WEBP in the line below
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, byteArrayOutputStream);

            try {
                Log.d(TAG, "New File Url: " + myExternalFile);

                FileOutputStream fos = new FileOutputStream(myExternalFile);
                fos.write(byteArrayOutputStream.toByteArray());
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }


    //----------------------------------------------------------------------------------------------


    public String takeThetaPicture() {

        InputStream in = null;
        OutputStream out = null;
        AssetManager assetManager = getResources().getAssets();

        String[] thetaImageFiles = null;

        try {
            thetaImageFiles = assetManager.list("100RICOH");
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            if (imageNumber >= thetaImageFiles.length) {
                imageNumber = 0;
                Log.d(TAG, "Set Image Number to Zero");
            }

            // copy file
            in = assetManager.open("100RICOH/" + thetaImageFiles[imageNumber]);
            out = new FileOutputStream(basepath + thetaImageFiles[imageNumber]);
            copyFile(in, out);

            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            Log.d(TAG, "copied file " + thetaImageFiles[imageNumber]);

            InputStream inputStream = assetManager.open("100RICOH/" + thetaImageFiles[imageNumber]);
            Drawable d = Drawable.createFromStream(inputStream, null);
            thetaImageView.setImageDrawable(d);
            inputStream.close();
            inputStream = null;
            thetaImagePath = basepath + thetaImageFiles[imageNumber];

            // increment image number last
            imageNumber = imageNumber + 1;


        } catch (IOException e) {
            e.printStackTrace();
        }


        return thetaImagePath;


    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    private KeyCallback keyCallback = new KeyCallback() {

        Theta theta = Theta.createForPlugin();

        @Override
        public void onKeyDown(int keyCode, KeyEvent keyEvent) {
            if (keyCode == KeyReceiver.KEYCODE_CAMERA) {
                thetaExecutor.submit(() -> {
                    CommandResponse<TakePicture.Result> response = null;

                    try {
                        response = theta.takePicture();


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    while (response.getState() != CommandState.DONE) {
                        try {
                            response = theta.commandStatus(response);
                            Thread.sleep(100);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Log.d(TAG, "fileUrl: " + response.getResult().getFileUrl());

                    inputFileUrl = response.getResult().getFileUrl();


                });
            }
        }

        @Override
        public void onKeyUp(int keyCode, KeyEvent keyEvent) {
            if (keyCode == KeyReceiver.KEYCODE_WLAN_ON_OFF) {
                processImage(getImagePath());

            }

        }

        @Override
        public void onKeyLongPress(int keyCode, KeyEvent keyEvent) {

        }
    };

    public String getImagePath() {
        String[] parts = inputFileUrl.toString().split("/");
        int length = parts.length;
        String filepath = Environment.getExternalStorageDirectory().getPath() +
                "/DCIM/100RICOH/" +
                parts[length - 1];
        Log.d(TAG, filepath);
        return filepath;
    }

    public void checkPermission() {
        statusTextView = findViewById(R.id.statusViewId);
        if ((ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED)) {
            statusTextView.setText("Ready");
            Toast.makeText(this, "storage permission good", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "WARNING: Need to enable storage permission",
                    Toast.LENGTH_LONG).show();
            statusTextView.setText("Check Permissions");
        }
    }

}
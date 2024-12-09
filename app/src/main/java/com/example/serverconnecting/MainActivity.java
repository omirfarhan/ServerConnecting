package com.example.serverconnecting;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.dhaval2404.imagepicker.ImagePicker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    Button button;
    ImageView edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView=findViewById(R.id.imageview);
        textView=findViewById(R.id.textview);
        button=findViewById(R.id.button);
        edit=findViewById(R.id.edit);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BitmapDrawable bd= (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap=bd.getBitmap();

                ByteArrayOutputStream arrayOutputStream=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,50,arrayOutputStream);

                byte[] imagebyte=arrayOutputStream.toByteArray();
                String image64= Base64.encodeToString(imagebyte,Base64.DEFAULT);

                postmethoddata(image64);
            }
        });


        ActivityResultLauncher<Intent>imagepicker=
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {
                            @Override
                            public void onActivityResult(ActivityResult result) {
                               if (result.getResultCode()==Activity.RESULT_OK){
                                   Intent intent=result.getData();
                                   Uri uri=intent.getData();
                                   try {
                                       Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                                       imageView.setImageBitmap(bitmap);
                                   } catch (IOException e) {
                                       throw new RuntimeException(e);
                                   }
                               }


                            }
                        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*
                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);

                 */

                ImagePicker.with(MainActivity.this)
                        .compress(1024)
                        .maxResultSize(1080, 1080)
                        .createIntent(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent intent) {
                                imagepicker.launch(intent);
                                return null;
                            }
                        });

            }
        });


    }

    //===Camara part======

    public boolean camerapermission(){
        boolean haspermission=false;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
            haspermission=true;

        }else {
            haspermission=false;
            String[] permission={Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(this,permission,201);
        }
        return haspermission;
    }


    ActivityResultLauncher<Intent> camaralauncher=
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>() {
                        @Override
                        public void onActivityResult(ActivityResult result) {

                            if (result.getResultCode()==Activity.RESULT_OK){
                                Toast.makeText(MainActivity.this, "image capturad", Toast.LENGTH_SHORT).show();
                                Intent intent=result.getData();
                                Bundle bundle=intent.getExtras();

                                Bitmap bitmap= (Bitmap) bundle.get("data");
                                imageView.setImageBitmap(bitmap);
                            }else {
                                Toast.makeText(MainActivity.this, "Image not captured", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


   //=====End part========================
    ActivityResultLauncher<Intent>activityResultLauncher
            =registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {

            if (result.getResultCode()==Activity.RESULT_OK){
                Toast.makeText(MainActivity.this, "Image selected", Toast.LENGTH_SHORT).show();
                Intent intent=result.getData();
                Uri uri=intent.getData();
                try {
                    Bitmap bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                    imageView.setImageBitmap(bitmap);

                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }else {
                Toast.makeText(MainActivity.this, "Image not selected", Toast.LENGTH_SHORT).show();
            }

        }
    });



    public void postmethoddata(String image) {

        String url = "http://192.168.1.25/files/practice.php";


        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        textView.setText(response);
                        Toast.makeText(MainActivity.this, "Server data received", Toast.LENGTH_SHORT).show();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textView.setText("Not Uploading image");
                        Toast.makeText(MainActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                params.put("key1", "value1");
                params.put("image", image);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


}
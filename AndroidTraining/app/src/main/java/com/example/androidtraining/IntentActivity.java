package com.example.androidtraining;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;

public class IntentActivity extends AppCompatActivity {

    final int CAMERA_RESULT = 100;
    Uri imageUri;    //Uri型：ファイルの情報　パス情報、ファイルの種類、ファイル名など


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intent);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText searchText = findViewById(R.id.searchText);
        Button searchButton = findViewById(R.id.searchButton);
        Button cameraButton = findViewById(R.id.cameraButton);
        FloatingActionButton confirmFab = findViewById(R.id.confirmFab);


        confirmFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntentActivity.this, ImageActivity.class);
                intent.putExtra("imageUri",imageUri.toString());
                startActivity(intent);
            }
        });

        //検索ボタン
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = searchText.getText().toString();
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, query);
                startActivity(intent);
            }
        });

        //カメラ起動ボタン
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //ファイル名
                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String fileName = "Training_" + timestamp + "_.jpg";
                //ファイルの情報
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                startActivityForResult(intent, CAMERA_RESULT);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_RESULT && resultCode == RESULT_OK){
//            Bitmap bitmap = data.getParcelableExtra("data");

            ImageView cameraImage = findViewById(R.id.cameraImage);
//            cameraImage.setImageBitmap(bitmap);
            cameraImage.setImageURI(imageUri);
        }
    }
}
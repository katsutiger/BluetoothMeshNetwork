package com.example.androidtraining;

import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PreferenceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_preference);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences pref = getPreferences(Context.MODE_PRIVATE);

        //画面部品をアクティビティ内に宣言する
        EditText thoughtsText = findViewById(R.id.thoughtsText);
        Button saveButton = findViewById(R.id.saveButton);
        Button cancelButton = findViewById(R.id.cancelButton);


        //画面の動き
        String selectedItem = getIntent().getStringExtra("selectedItem");
        String thoughtsSt = pref.getString("memo" + selectedItem,"");
        thoughtsText.setText(thoughtsSt);  //EditText に取得した文字列をセットする

        //保存ボタンのクリックイベントにプレファレンスへの書き込み処理を記述する
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = pref.edit();
                //前の画面で選んだもので保存領域を変える
                editor.putString("memo"+selectedItem, thoughtsText.getText().toString());
                editor.apply();
                Toast.makeText(PreferenceActivity.this, "保存しました", Toast.LENGTH_SHORT).show();
            }
        });

        //キャンセルボタンのクリックイベントに画面を閉じる処理を追加
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PreferenceActivity.this,"終了します", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }
}
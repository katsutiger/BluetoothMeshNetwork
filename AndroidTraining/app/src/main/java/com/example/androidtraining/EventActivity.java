package com.example.androidtraining;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });

        Intent intent = getIntent();
        int age = intent.getIntExtra("age",25);

        //画面上のパーツを変数化
        EditText nameText = findViewById(R.id.nameText);
        EditText passText = findViewById(R.id.passText);
        Button nameClear = findViewById(R.id.nameClear);
        Button passClear = findViewById(R.id.passClear);
        Button confirmButton = findViewById(R.id.confirmButton);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);


        //画面の動き

        Toast.makeText(this,"年齢：" + age,Toast.LENGTH_SHORT).show();

        nameClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameText.setText("");
            }
        });

        //パスワードクリックボタン
        passClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passText.setText("");
            }
        });


        //パスワードクリアボタンをタッチ
        passClear.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction()== MotionEvent.ACTION_DOWN) {
                    //押した時のイベント
                    setTitle(passText.getText());
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    //離れた時のイベント
                    setTitle(R.string.app_name);
                }
                return false;
            }
        });

        //確認ボタンクリック
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(EventActivity.this,"名前は"+ nameText.getText(),Toast.LENGTH_SHORT).show();
            }
        });

        //確認ボタンを長押し
        confirmButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //長押しでラジオボタンのどちらを選んでいるかを取得
                if (radioGroup.getCheckedRadioButtonId() == R.id.radioMale) {
                    Toast.makeText(EventActivity.this, R.string.radioMale, Toast.LENGTH_SHORT).show();
                }

                if (radioGroup.getCheckedRadioButtonId() == R.id.radioFemale) {
                    Toast.makeText(EventActivity.this, R.string.radioFemale, Toast.LENGTH_SHORT).show();
                }
                finish();
                return true;
            }
        });
    }
}

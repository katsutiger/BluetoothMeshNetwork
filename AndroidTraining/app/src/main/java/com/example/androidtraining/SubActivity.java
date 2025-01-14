package com.example.androidtraining;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SubActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_main);
        //setContentView(R.layout.activity_sub);
        setContentView(R.layout.activity_const);
        //setContentView(R.layout.activity_const2);

        setTitle("SubActivity");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //画面上のパーツを変数化
        TextView nameLable = findViewById(R.id.nameLable);
        EditText nameText = findViewById(R.id.nameText);
        Button sendButton = findViewById(R.id.chatButton);
        Button confirmButton = findViewById(R.id.confirmButton);
        Button clearButton = findViewById(R.id.clearButton);


        //画面の操作
        nameLable.setText("Name");

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nameText.setText("");
            }
        });

        sendButton.setOnClickListener(this);

        confirmButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        setTitle("Button Clicked");

        //EventActivity(別の画面)を変数化
        Intent intent = new Intent(this,EventActivity.class);


        //呼び元のボタンの判別
        if (view.getId() == R.id.confirmButton){
            Toast.makeText(this,"確認クリック",Toast.LENGTH_SHORT).show();
            intent.putExtra("age",19);
            startActivity(intent);//別画面を起動
        }
        if (view.getId() == R.id.chatButton){
            Toast.makeText(this,"送信クリック",Toast.LENGTH_SHORT).show();
        }
    }
}
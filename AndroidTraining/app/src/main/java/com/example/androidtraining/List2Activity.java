package com.example.androidtraining;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;

import java.util.ArrayList;

public class List2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        ArrayList<String> addList = new ArrayList<>();
//        addList.add("January");
//        addList.add("February");
//        addList.add("March");
//        addList.add("April");




        //        画面上の変数化
        ListView addList = findViewById(R.id.addList);
        EditText addText = findViewById(R.id.addText);
        Button addButton = findViewById(R.id.addButton);

        //別の画面を変数化
        Intent subIntent = new Intent(List2Activity.this,SubActivity.class);
        Intent eventIntent = new Intent(List2Activity.this,EventActivity.class);

        //ダークの入る可変長配列変数
        ArrayList<String> dataList = new ArrayList<>();
        //配列変数をアダプターに設定します
        ArrayAdapter<String> adapter = new ArrayAdapter(List2Activity.this, android.R.layout.simple_list_item_1,dataList);
        //アダプターを画面のリストに設定
        addList.setAdapter(adapter);

        //画面の操作
        //追加ボタン
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addList.setAdapter(adapter);
                dataList.add(addText.getText().toString());  //addTextに入力した文字をdataList配列変数に追加
                addText.setText("");  //addTextに書いた文字が消える
            }
        });
        //画面の動き
        //リストから選んだ
        addList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i % 2 == 0) { // 偶数行
                    startActivity(eventIntent);
                } else { // 奇数行
                    startActivity(subIntent);
                }
            }
        });
    }
}
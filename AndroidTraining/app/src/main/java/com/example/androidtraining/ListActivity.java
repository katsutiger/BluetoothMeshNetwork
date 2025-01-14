package com.example.androidtraining;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


//      listに出すデータを配列変数で宣言
//      String[] curryList = {"ドライカレー","カツカレー","チーズカレー","スープカレー"};
        ArrayList<String> curryList = new ArrayList<>();
        curryList.add("ドライカレー");
        curryList.add("カツカレー");
        curryList.add("チーズカレー");
        curryList.add("スープカレー");
//        画面上の変数化
         ListView listView = findViewById(R.id.curryList);
//        配列をアダプターに設定
         ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,curryList);
//        アダプターをリストに設定
         listView.setAdapter(adapter);

//        curryList.add("ビーフカレー");

        //画面の動き
        //リストから選んだ
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = curryList.get(i);
                Toast.makeText(ListActivity.this, "番目"+selectedItem, Toast.LENGTH_SHORT).show();

                Intent intent;
                intent = new Intent(ListActivity.this,PreferenceActivity.class);
                intent.putExtra("selectedItem",selectedItem);
                startActivity(intent);

 //               Toast.makeText(ListActivity.this, ((TextView)view).getText().toString(),Toast.LENGTH_SHORT).show();
//                Toast.makeText(ListActivity.this, i + "番目", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
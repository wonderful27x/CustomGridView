package com.example.customgridview;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WonderfulGridView wonderfulGridView1;
    private WonderfulGridView wonderfulGridView2;
    private WonderfulGridView wonderfulGridView3;
    private WonderfulGridView wonderfulGridView4;
    private TabContentGridView tabContentGridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wonderfulGridView1 = findViewById(R.id.customGridView1);
        wonderfulGridView2 = findViewById(R.id.customGridView2);
        wonderfulGridView3 = findViewById(R.id.customGridView3);
        wonderfulGridView4 = findViewById(R.id.customGridView4);
        tabContentGridView = findViewById(R.id.tabContentGridView);

        init();
    }

    private void init(){
        final List<String> children1 = new ArrayList<>();
        List<String> children2 = new ArrayList<>();
        List<String> children3 = new ArrayList<>();
        List<String> children4 = new ArrayList<>();
        List<String> tab = new ArrayList<>();
        List<String> content = new ArrayList<>();

        children1.add("守望者");
        children1.add("明日边缘");
        children1.add("变形金刚");
        children1.add("复仇者联盟");
        children1.add("阿凡达");
        children1.add("愤怒的小鸟");
        children1.add("天启");
        tab.add("1");
        tab.add("2");
        tab.add("3");
        tab.add("4");
        tab.add("5");
        tab.add("6");
        tab.add("7");

        children2.addAll(children1);
        children3.addAll(children1);
        children4.addAll(children1);
        content.addAll(children1);

        wonderfulGridView1.addChildrenView(children1);
        wonderfulGridView2.addChildrenView(children2);
        wonderfulGridView3.addChildrenView(children3);
        wonderfulGridView4.addChildrenView(children4);
        tabContentGridView.addChildrenView(content,tab);

        wonderfulGridView1.setItemClickListener(new SimpleCustomGridView.ItemClickListener() {
            @Override
            public void onItemClick(int position, String content) {
                Toast.makeText(MainActivity.this, position + "-" + content, Toast.LENGTH_SHORT).show();
            }
        });

        wonderfulGridView2.setSelectChangeListener(new WonderfulGridView.SelectChangeListener() {
            @Override
            public void onSelect(int position, String content) {
                Toast.makeText(MainActivity.this, "onSelect", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void unSelect(int position, String content) {
                Toast.makeText(MainActivity.this, "unSelect", Toast.LENGTH_SHORT).show();
            }
        });

        wonderfulGridView3.setSelectChangeListener(new WonderfulGridView.SelectChangeListener() {
            @Override
            public void onSelect(int position, String content) {
                Toast.makeText(MainActivity.this, "onSelect", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void unSelect(int position, String content) {
                Toast.makeText(MainActivity.this, "unSelect", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
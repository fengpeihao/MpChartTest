package com.honglu.mpcharttest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EntranceActivity extends BaseActivity {
    @Bind(R.id.btn)
    Button btn;
    @Bind(R.id.btn_k)
    Button btnK;
    @Bind(R.id.btn_fix)
    Button btnFix;
    @Bind(R.id.btn_k1)
    Button mBtnK1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
        ButterKnife.bind(this);
     /*   Intent intent = new Intent(MainActivity.this, KLineActivity.class);
        startActivity(intent);*/
    }


    @OnClick({R.id.btn, R.id.btn_k, R.id.btn_fix,R.id.btn_k1})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn:
                Intent intent = new Intent(EntranceActivity.this, MinutesActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_k:
                Intent intentK = new Intent(EntranceActivity.this, KLineActivity.class);
                startActivity(intentK);
                break;
            case R.id.btn_fix:
                Intent intentK2 = new Intent(EntranceActivity.this, StockActivity.class);
                startActivity(intentK2);
                break;
            case R.id.btn_k1:
                Intent intentK1 = new Intent(EntranceActivity.this, MainActivity.class);
                startActivity(intentK1);
                break;
        }
    }
}

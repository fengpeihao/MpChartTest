package com.honglu.mpcharttest;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import butterknife.ButterKnife;

public class BaseActivity extends Activity {
    public final  String TAG =this.getClass().getSimpleName();
    protected Activity activity;
    protected Toast mToast = null;
    public SharedPreferences sharedPreferences;
    /*@Inject
    Activity activity;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);

    }

    public void showToast(String content) {
        if (mToast == null) {
            mToast = Toast.makeText(this, content, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(content);
        }
        mToast.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(BaseActivity.this);
    }
}

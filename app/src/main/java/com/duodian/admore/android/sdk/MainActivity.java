package com.duodian.admore.android.sdk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.duodian.admore.android.sdk.adview.appwall.AdmoreAppWallView;
import com.duodian.admore.android.sdk.adview.drift.AdmoreDriftView;
import com.duodian.admore.android.sdk.callbacks.AdListener;
import com.duodian.admore.android.sdk.log.LoggerUtil;
import com.duodian.admore.android.sdk.utils.LogUtil;


public class MainActivity extends AppCompatActivity {
    private AdmoreAppWallView admoreAppWallView;
    private AdmoreAppWallView admoreAppWallView1;
    private AdmoreAppWallView admoreAppWallView2;
    private AdmoreDriftView admoreDriftView;
    private Button buttonAppWall;
    private Button buttonDrift;
    private Button buttonLog;
//    private TextView textView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.e("MainActivity", "onCreate");
        setContentView(R.layout.activity_main);
        this.admoreAppWallView = (AdmoreAppWallView) findViewById(R.id.admoreAppWallView);
        this.admoreAppWallView1 = (AdmoreAppWallView) findViewById(R.id.admoreAppWallView1);
        this.admoreAppWallView2 = (AdmoreAppWallView) findViewById(R.id.admoreAppWallView2);
        this.admoreDriftView = (AdmoreDriftView) findViewById(R.id.floatingAdView);
        this.admoreDriftView.setAdListener(new AdListener() {
            public void onReceiveAd() {
            }

            public void onReceiveAdFailed() {
            }

            public void onLoadFailed() {
            }

            public void onCloseClick() {
            }

            public void onAdClick() {
            }

            public void onAdExpose() {
            }
        });
        this.buttonDrift = (Button) findViewById(R.id.buttonDrift);
        this.buttonAppWall = (Button) findViewById(R.id.buttonAppWall);
        this.buttonDrift.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.admoreDriftView.loadAd("unit-556b1faf35832");
            }
        });
        this.buttonAppWall = (Button) findViewById(R.id.buttonAppWall);
        this.buttonAppWall.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.admoreAppWallView.loadAd("unit-556b1faf35832");
                MainActivity.this.admoreAppWallView1.loadAd("unit-556b1faf35832");
                MainActivity.this.admoreAppWallView2.loadAd("unit-556b1faf35832");
            }
        });
        this.admoreAppWallView.setAdListener(new AdListener() {
            public void onReceiveAd() {
            }

            public void onReceiveAdFailed() {
            }

            public void onLoadFailed() {
            }

            public void onCloseClick() {
            }

            public void onAdClick() {
            }

            public void onAdExpose() {
            }
        });
        LogUtil.e("MainActivity", "onCreate完成");
//        this.textView = (TextView) findViewById(R.id.textView);
        this.buttonLog = (Button) findViewById(R.id.buttonLog);
        this.buttonLog.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LoggerUtil.readLog(MainActivity.this.getApplicationContext());
            }
        });
    }

    protected void onResume() {
        super.onResume();
    }

}

package me.peiwo.peiwo.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import me.peiwo.peiwo.DfineAction;
import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.constans.Constans;
import me.peiwo.peiwo.net.ApiRequestWrapper;
import me.peiwo.peiwo.net.AsynHttpClient;
import me.peiwo.peiwo.net.MsgStructure;
import me.peiwo.peiwo.net.NetUtil;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.Md5Util;
import me.peiwo.peiwo.util.PWUtils;
import me.peiwo.peiwo.util.TitleUtil;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.TextUtils;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;

public class AboutUsActivity extends BaseActivity {

    private TextView buildVerName;

    private long beginTime = 0L;
    private int clickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us_activity);

        TitleUtil.setTitleBar(this, "关于陪我", new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        }, null);
        buildVerName = (TextView) findViewById(R.id.build_ver_name);
        TextView tv_protocol = (TextView) findViewById(R.id.tv_protocol);
        tv_protocol.setText(PWUtils.getClauselinks(this, "陪我app《用户协议》", 5));
        tv_protocol.setMovementMethod(LinkMovementMethod.getInstance());
        readCodes();
        TextView verName = (TextView) findViewById(R.id.ver_name);
        verName.setText(getString(R.string.ver_name, PWUtils.getVersionName(this)));


        // buildVerName.setText(getString(R.string.build_ver_name,PWUtils.getVersionName(this)));
        // buildVerName.setVisibility(View.GONE);


        findViewById(R.id.about_us_logo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (System.currentTimeMillis() - beginTime > 2000) {
                    clickCount = 0;
                }
                if (clickCount == 0) {
                    beginTime = System.currentTimeMillis();
                }
                clickCount++;
                if (clickCount == 5) {
                    clickCount = 0;
                    mHandler.removeMessages(0);
                    mHandler.sendEmptyMessageDelayed(0, 1000);
                }
            }
        });
        final TextView questionsView = (TextView) findViewById(R.id.tv_questions_answer);
        questionsView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(AboutUsActivity.this, GlobalWebViewActivity.class);
                intent.putExtra(GlobalWebViewActivity.TITLE, questionsView.getText().toString());
                intent.putExtra(GlobalWebViewActivity.URL, Constans.PEIWO_SCHOOL_URL);
                startActivity(intent);
            }
        });
        findViewById(R.id.tv_evaluation_app).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                startMarket();
            }
        });
        findViewById(R.id.tv_function_guide).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                System.out.println("AboutUsActivity.onCreate(...).new OnClickListener() {...}.onClick()!");
                Intent intent = new Intent(AboutUsActivity.this, WildcatGuideActivity.class);
                intent.putExtra("isFromSetting", true);
                startActivity(intent);
            }
        });
    }

    private void startMarket() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + getApplicationInfo().packageName));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showToast(this, "没有找到应用市场程序");
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    showToast(AboutUsActivity.this, "开始上传日志");
                    CustomLog.i("syf", "need upload");
                    PeiwoApp.getApplication().mExecutorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            upLoadToServer();
                        }
                    });
                    break;
                case 1:
                    showToast(AboutUsActivity.this, "日志上传成功");
                    break;
                case 2:
                    showToast(AboutUsActivity.this, "日志上传失败");
                    break;
            }
        }
    };


    public void upLoadToServer() {
        if (PeiwoApp.getApplication().getNetType() == NetUtil.NO_NETWORK) {
            return;
        }
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File filePath1 = new File(path);
        if (!filePath1.exists()) {
            if (!filePath1.mkdirs())
                return;
        }
        File filePath2 = new File(filePath1, "LogFolder");
        if (!filePath2.exists()) {
            if (!filePath2.mkdirs())
                return;
        }

        for (int i = 0; i < 3; i++) {
            File file = null;
            if (i == 0) {
                file = new File(filePath2, DfineAction.HTTP_TAG);
            } else if (i == 1) {
                file = new File(filePath2, DfineAction.TCP_TAG);
            } else if (i == 2) {
                file = new File(filePath2, DfineAction.WEBRTC_TAG);
            }
            if (!file.exists() || file.length() == 0)
                continue;

            InputStream is = null;
            InputStreamReader inputReader = null;
            BufferedReader reader = null;
            StringBuffer sb = null;

            try {
                is = new FileInputStream(file);
                inputReader = new InputStreamReader(is);
                reader = new BufferedReader(inputReader);
                sb = new StringBuffer();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair("content-length", sb.length() + ""));
                params.add(new BasicNameValuePair("validation", Md5Util.MD5(sb.toString())));
                params.add(new BasicNameValuePair("log", sb.toString()));
                final int index = i;
                ApiRequestWrapper.openAPIPOST(PeiwoApp.getApplication(),
                        params, AsynHttpClient.API_REPORT_CRASH_LOG,
                        new MsgStructure() {
                            @Override
                            public void onReceive(JSONObject data) {
                                if (index == 1) {
                                    mHandler.sendEmptyMessage(1);
                                }
                            }

                            @Override
                            public void onError(int error, Object ret) {
                                if (index == 1) {
                                    mHandler.sendEmptyMessage(2);
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputReader != null) {
                    try {
                        inputReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    /**
     * git统计版本使用，上线版本需要将其注掉
     */
    private void readCodes() {
        InputStream ins = null;
        InputStreamReader inReader = null;
        BufferedReader reader = null;
        try {
            ins = getAssets().open("txt/debug_config.txt");
            inReader = new InputStreamReader(ins, "utf-8");
            reader = new BufferedReader(inReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONObject o = new JSONObject(sb.toString());
            String build_version_hash = o.optString("build_version_hash");
            String build_version = o.optString("build_version");
            String build_version_manual = o.optString("build_version_manual");
            String version = PWUtils.getVersionName(PeiwoApp.getApplication());
            build_version_manual = version + "." + build_version_manual;
            if (!TextUtils.isEmpty(build_version_hash)) {
                buildVerName.setText(build_version_hash + "-" + build_version + "-" + build_version_manual);
            } else {
                buildVerName.setText(build_version + "-" + build_version_manual);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (inReader != null)
                    inReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (ins != null)
                    ins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

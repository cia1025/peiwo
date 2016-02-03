package me.peiwo.peiwo.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.qiniu.android.http.ResponseInfo;

import me.peiwo.peiwo.PeiwoApp;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.callback.UploadCallback;
import me.peiwo.peiwo.constans.UMEventIDS;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.CreateFeedFlowEvent;
import me.peiwo.peiwo.eventbus.event.UserInfoEvent;
import me.peiwo.peiwo.information.picture.ImagePublishAdapter;
import me.peiwo.peiwo.information.picture.ImageZoomActivity;
import me.peiwo.peiwo.model.FeedFlowModel;
import me.peiwo.peiwo.model.ImageItem;
import me.peiwo.peiwo.model.ImageModel;
import me.peiwo.peiwo.model.PWUserModel;
import me.peiwo.peiwo.net.*;
import me.peiwo.peiwo.util.*;
import me.peiwo.peiwo.util.LocationUtil.GetLocationCallback;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 创建信息流Activity
 *
 * @author Administrator
 */
public class InformationPubActivity extends BaseActivity implements OnClickListener {
    public static final int RESULT_CHOOSE = 103;
    private static final int TOPIC_PUB_SUCCESSFUL = 0X1002;
    private static final int TOPIC_PUB_FAIL = 0X1003;
    private static final int TOPIC_GET_LOCATION_SUCCESS = 0x1004;
    private static final int TOPIC_GET_LOCATION_ERROR = 0x1005;
    private TextView locationTV;
    private ImageView image_location_cancel;
    private static final int TAKE_PHOTO = 101;
    private static final int VIEW_IMAGE = 102;
    private GridView mGridView;
    private TextView topicTitle;
    private EditText editContent;
    private ImagePublishAdapter mAdapter;
    private int mUid;
    private int topicId;
    private String topicContent;
    private ArrayList<ImageItem> mDataList = new ArrayList<ImageItem>();
    private MyHandler mHandler;
    private ImageItem addImage;
    private View location_layout;
    private String mProvince = " ";
    private String mCity = " ";
    private int currentImgSize;
    private TextView btn_right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_pub);
        initData();
        initView();
        addImage = new ImageItem();
        mDataList.add(addImage);

        notifyDataChanged();
    }

    private void initView() {
        topicTitle = (TextView) findViewById(R.id.tv_title);
        topicTitle.setText(topicContent);
        btn_right = (TextView) findViewById(R.id.btn_right);
        btn_right.setTextColor(Color.parseColor("#8e8e8e"));
        editContent = (EditText) findViewById(R.id.tv_words);
        editContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    btn_right.setTextColor(Color.parseColor("#00b8d0"));
                } else {
                    if (mDataList.size() > 1) {
                        btn_right.setTextColor(Color.parseColor("#00b8d0"));
                    } else {
                        btn_right.setTextColor(Color.parseColor("#8e8e8e"));
                    }
                }
            }
        });
        locationTV = (TextView) findViewById(R.id.tv_location);
        image_location_cancel = (ImageView) findViewById(R.id.image_location_cancel);
        location_layout = findViewById(R.id.location_layout);
        PWUserModel model = UserManager.getPWUser(this);
        boolean isLocationOff = TextUtils.isEmpty(model.province.trim());
        if (!isLocationOff) {
            getLocation();
        }

        location_layout.setOnClickListener(this);
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        mAdapter = new ImagePublishAdapter(this, mDataList);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener((parent, view, position, id) -> {
            if (mDataList.get(position) == addImage) {
                Intent intent = new Intent(InformationPubActivity.this, AlbumCompatActivity.class);
                intent.putExtra(AlbumCompatActivity.CHOOSE_MODE, AlbumCompatActivity.CHOOSE_MODE_SECTION); // ImageUtil.getPathForUpload(mImageKey).getAbsolutePath()
                intent.putExtra(AlbumCompatActivity.K_ALBUM_RST_COUNT, 9 - currentImgSize);
                // intent.putExtra(AlbumCompatActivity.K_ALBUM_RST_COUNT, 9);
                startActivityForResult(intent, TAKE_PHOTO);
            } else {
                Intent intent = new Intent(InformationPubActivity.this, ImageZoomActivity.class);
                intent.putExtra(ImageZoomActivity.K_CURR_LOCATION, position);
                ArrayList<ImageItem> extra = new ArrayList<>();
                extra.addAll(mDataList);
                if (extra.indexOf(addImage) >= 0) {
                    extra.remove(addImage);
                }
                intent.putParcelableArrayListExtra(ImageZoomActivity.EXTRA_IMAGE_LIST, extra);
                startActivityForResult(intent, VIEW_IMAGE);
            }
        });
        setBarTitle();
    }

    private void initData() {
        topicId = getIntent().getIntExtra("topic_id", -1);
        topicContent = getIntent().getStringExtra("topic_content");
        mUid = UserManager.getUid(this);
        mHandler = new MyHandler(this);
    }

    private static class MyHandler extends Handler {
        WeakReference<InformationPubActivity> acivity_ref;

        public MyHandler(InformationPubActivity activity) {
            acivity_ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            InformationPubActivity theActivity = acivity_ref.get();
            if (theActivity == null || theActivity.isFinishing())
                return;
            int what = msg.what;
            switch (what) {
                case TOPIC_PUB_SUCCESSFUL:
                    SharedPreferencesUtil.putBooleanExtra(theActivity, AsynHttpClient.KEY_INFO_SHOW_DYNAMIC, true);
                    EventBus.getDefault().post(new UserInfoEvent());

                    UmengStatisticsAgent.onEvent(theActivity, UMEventIDS.UMERELEASESURE);
                    theActivity.dismissAnimLoading();
                    if (theActivity.topicId == -1) {
                        theActivity.showToast(theActivity, "创意已经提交成功，可在您的个人动态查看");
                    } else {
                        theActivity.showToast(theActivity, "信息发布成功！");
                    }
                    FeedFlowModel mModel = (FeedFlowModel) msg.obj;
                    EventBus.getDefault().post(new CreateFeedFlowEvent(mModel));
                    theActivity.finish();
                    break;
                case TOPIC_PUB_FAIL:
                    theActivity.dismissAnimLoading();
                    theActivity.showToast(theActivity, "网络连接失败");
                    break;
                case TOPIC_GET_LOCATION_ERROR: {
                    theActivity.showToast(theActivity, "获取坐标失败");
                    theActivity.locationTV.setText("显示坐标");
                    theActivity.location_layout.setClickable(true);
                }
                break;
                case TOPIC_GET_LOCATION_SUCCESS: {
                    String address = (String) msg.obj;
                    theActivity.locationTV.setText(address);
                    theActivity.image_location_cancel.setVisibility(View.VISIBLE);
                    theActivity.location_layout.setClickable(true);
                }
                break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }

    private void setBarTitle() {
        TitleUtil.setTitleBar(this, "", new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editContent.getText().length() > 0 || mDataList.size() > 1) {
                    exitDialog();
                    return;
                }
                finish();
            }
        }, "发布", new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PWUtils.isMultiClick())
                    return;
                int size = mDataList.size();
                if (mDataList.contains(addImage)) {
                    size = mDataList.size() - 1;
                }
                if (TextUtils.isEmpty(editContent.getText()) && size == 0) {
                    return;
                }
                submitCustomFeedFlow();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        notifyDataChanged();
        if (mDataList.size() > 1) {
            btn_right.setTextColor(Color.parseColor("#00b8d0"));
        } else {
            btn_right.setTextColor(Color.parseColor("#8e8e8e"));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
//            case TAKE_PICTURE:
//                if (mDataList.size() < 10 && resultCode == -1 && !TextUtils.isEmpty(path)) {
//                    ImageItem item = new ImageItem();
//                    item.sourcePath = path;
//                    mDataList.add(mDataList.size() - 1, item);
//
//                    //ImageFetcher imageFetcher = ImageFetcher.getInstance();
//                    //ArrayList<ImageItem> selectedList = imageFetcher.getSelectedImageList();
//                    //imageFetcher.addSeletedList(item);
//                    ArrayList<ImageItem> selectedList = data.getParcelableArrayListExtra(AlbumCompatActivity.K_ALBUM_RST);
//                    mDataList.addAll(selectedList);
//                    if (mDataList.size() == 10) {
//                        mDataList.remove(9);
//                    }
//                    notifyDataChanged();
//                }
//                break;
            case TAKE_PHOTO:
//                if (resultCode == RESULT_OK) {
//                    takePhoto();
//                } else if (resultCode == RESULT_CHOOSE) {
//                    ArrayList<ImageItem> selectedList = ImageFetcher.getInstance().getSelectedImageList();
//                    mDataList.clear();
//                    mDataList.addAll(selectedList);
//                    if (mDataList.size() < 9) {
//                        mDataList.add(addImage);
//                    }
//                    notifyDataChanged();
//                }
                if (mDataList.size() < 10 && resultCode == RESULT_OK) {
//                    ImageItem item = new ImageItem();
//                    item.sourcePath = path;
                    //mDataList.add(mDataList.size() - 1, item);

                    //ImageFetcher imageFetcher = ImageFetcher.getInstance();
                    //ArrayList<ImageItem> selectedList = imageFetcher.getSelectedImageList();
                    //imageFetcher.addSeletedList(item);
                    //mDataList.clear();
                    if (mDataList.contains(addImage)) {
                        mDataList.remove(addImage);
                    }
                    ArrayList<String> selectedList = data.getStringArrayListExtra(AlbumCompatActivity.K_ALBUM_RST);
                    currentImgSize += selectedList.size();
                    ArrayList<ImageItem> items = new ArrayList<>();
                    for (int i = 0; i < selectedList.size(); i++) {
                        String path = selectedList.get(i);
                        items.add(new ImageItem(path, path, 0, 0));
                    }
                    mDataList.addAll(items);
                    if (mDataList.size() < 9) {
                        mDataList.add(addImage);
                    }
                    notifyDataChanged();
                }
                break;
            case VIEW_IMAGE:
//                for (int i = 0; i < ImageFetcher.getInstance().getDeleteList().size(); i++) {
//                    for (int j = 0; j < mDataList.size(); j++) {
//                        if (mDataList.get(j).sourcePath.equals(ImageFetcher.getInstance().getDeleteList().get(i))) {
//                            mDataList.remove(j);
//                            break;
//                        }
//                    }
//                }
//                if (!mDataList.contains(addImage) && mDataList.size() < 9) {
//                    mDataList.add(addImage);
//                }
//                notifyDataChanged();
                mDataList.clear();
                ArrayList<ImageItem> selectedList = data.getParcelableArrayListExtra(ImageZoomActivity.EXTRA_IMAGE_LIST);
                mDataList.addAll(selectedList);
                if (mDataList.size() < 9) {
                    mDataList.add(addImage);
                }
                notifyDataChanged();
                break;
        }
    }

    @Override
    protected void onDestroy() {
//        ImageFetcher.getInstance().getSelectedImageList().clear();
//        ImageFetcher.getInstance().getImagesList().clear();
        super.onDestroy();
    }

    //private static String path = "";


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.location_layout:
                if ("显示坐标".equals(locationTV.getText())) {
                    locationTV.setText("正在获取坐标信息");
                    location_layout.setClickable(false);
                    getLocation();
                } else {
                    image_location_cancel.setVisibility(View.GONE);
                    locationTV.setText("显示坐标");
                }
                break;
            default:
                break;
        }
    }

    private void getLocation() {
        LocationUtil.getMyLocation(new GetLocationCallback() {
            @Override
            public void onError() {
                mHandler.sendEmptyMessage(TOPIC_GET_LOCATION_ERROR);
            }

            @Override
            public void onComplete(String adress, String city) {
                mProvince = adress;
                mCity = city;
                Message msg = mHandler.obtainMessage(TOPIC_GET_LOCATION_SUCCESS);
                msg.obj = adress + city;
                if (!TextUtils.isEmpty(adress) && adress.equals(city)) {
                    msg.obj = adress;
                }
                mHandler.sendMessage(msg);
            }
        });
    }

    private void notifyDataChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public void onBackPressed() {
        if (editContent.getText().length() > 0 || mDataList.size() > 1) {
            exitDialog();
            return;
        }
        super.onBackPressed();
    }

    private void exitDialog() {
        new AlertDialog.Builder(this).setTitle("提示").setMessage("放弃此次编辑？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        UmengStatisticsAgent.onEvent(InformationPubActivity.this,
                                UMEventIDS.UMERELEASEBACK);
                        currentImgSize = 0;
                        finish();
                    }
                }).setPositiveButton("继续编辑", null).create().show();
    }

    /**
     * 提交发布
     */
    private void submitCustomFeedFlow() {
        if (PeiwoApp.getApplication().getNetType() == NetUtil.NO_NETWORK) {
            showToast(this, "当前网络不可用，请检查网络连接");
            return;
        }
//        if (TextUtils.isEmpty(editContent.getText().toString().trim())) {
//            showToast(this, "内容不能为空");
//            return;
//        }

        showAnimLoading("", false, false, false);
        ArrayList<String> imageUploadPathList = new ArrayList<String>();
        if (mDataList.size() > 1) {
            ArrayList<ImageModel> imageList = new ArrayList<ImageModel>();
            //ImageModel imageModel = null;
            int size = mDataList.size();
            if (mDataList.contains(addImage)) {
                size = mDataList.size() - 1;
            }
            for (int i = 0; i < size; i++) {
                ImageItem imageItem = mDataList.get(i);
                imageItem.setUid(mUid);
                uploadImgBySCS(imageItem, imageUploadPathList, imageList, size);
            }
        } else {
            postData(new FeedFlowModel(), new ArrayList<>());
        }

    }


    private void postData(FeedFlowModel mModel, ArrayList<NameValuePair> params) {


        String content = editContent.getText().toString();
        String loction = locationTV.getText().toString();

        if (TextUtils.isEmpty(content) && mDataList.size() <= 1) {
            return;
        }
        mModel.setTopicContent(topicContent);
        mModel.setContent(content);
        mModel.setCreate_time(System.currentTimeMillis());
        mModel.setTopicId(topicId);
        mModel.setUserModel(UserManager.getPWUser(this));
        mModel.setIs_like(0);
        mModel.setLike_number(0);
        mModel.setMy(true);
        if (!"显示坐标".equals(loction) && !"正在获取坐标信息".equals(loction)) {
            params.add(new BasicNameValuePair(AsynHttpClient.KEY_TOPIC_LOCATION, loction));
            mModel.setLocation(loction);
        }
        if (topicId == -1) {
            params.add(new BasicNameValuePair(AsynHttpClient.KEY_TOPIC_CONTENT, topicContent));
            params.add(new BasicNameValuePair(AsynHttpClient.KEY_TOPIC_CUSTOM_CONTENT, content));
            ApiRequestWrapper.openAPIPOST(InformationPubActivity.this, params,
                    AsynHttpClient.API_TAG_TOPIC, new MsgStructure() {
                        @Override
                        public void onReceive(JSONObject data) {
                            if (data != null) {
                                String id = data.optString("pub_id");
                                mModel.setId(id);
                            }
                            Message msg = mHandler.obtainMessage(TOPIC_PUB_SUCCESSFUL);
                            msg.obj = mModel;
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void onError(int error, Object ret) {
                            if (error == 50005) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast(InformationPubActivity.this, "积分过低,无法发布");
                                        dismissAnimLoading();
                                    }
                                });
                            } else {
                                mHandler.sendEmptyMessage(TOPIC_PUB_FAIL);
                            }
                        }
                    });
        } else {
            params.add(new BasicNameValuePair(AsynHttpClient.KEY_TOPIC_CONTENT, content));
            params.add(new BasicNameValuePair(AsynHttpClient.KEY_TOPIC_ID, String.valueOf(topicId)));
            ApiRequestWrapper.openAPIPOST(InformationPubActivity.this, params,
                    AsynHttpClient.API_TOPIC_PUB, new MsgStructure() {
                        @Override
                        public void onReceive(JSONObject data) {
                            CustomLog.d("onReceive. data is : " + data);
                            if (data != null) {
                                String id = data.optString("pub_id");
                                mModel.setId(id);
                            }
                            Message msg = mHandler.obtainMessage(TOPIC_PUB_SUCCESSFUL);
                            msg.obj = mModel;
                            mHandler.sendMessage(msg);
                        }

                        @Override
                        public void onError(int error, final Object ret) {
                            CustomLog.d("onError. ret is : " + ret);
                            if (error == 50005) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast(InformationPubActivity.this, "积分过低,无法发布");
                                        dismissAnimLoading();
                                    }
                                });
                            } else {
                                mHandler.sendEmptyMessage(TOPIC_PUB_FAIL);
                            }
                        }
                    });
        }
        int locationOption = image_location_cancel.getVisibility() == View.VISIBLE ? 1 : 0;

        ArrayList<NameValuePair> paramList = new ArrayList<NameValuePair>();
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_USER_LOCATION_FILTER, String.valueOf(locationOption)));
        if (locationOption == 0) {
            mProvince = " ";
            mCity = " ";
        }
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_PROVINCE, mProvince));
        paramList.add(new BasicNameValuePair(AsynHttpClient.KEY_CITY, mCity));
        ApiRequestWrapper.openAPIPOST(this, paramList, AsynHttpClient.API_USERS_FILTER_SETTING, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                CustomLog.d("onReceive1. data is : " + data);
            }

            @Override
            public void onError(int error, Object ret) {
                CustomLog.d("onError1. ret is : " + ret);
            }
        });

        PWUserModel model = UserManager.getPWUser(this);
        model.province = mProvince;
        UserManager.saveUser(this, model);
    }

    /**
     * 上传图片到搜狐云
     *
     * @param imageItem
     */
    private int count;

    private void uploadImgBySCS(final ImageItem imageItem, List<String> imageUploadPathList, ArrayList<ImageModel> imageList, int size) {
//        mExecutorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                SCSUpload.uploadFile(imageItem.sourcePath, imageItem.imageKey, SCSUpload.FileType.IMAGE);
//            }
//        });
        CustomLog.d("uploadImgBySCS. image localpath is : " + imageItem.sourcePath);
        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(PWUploader.K_UPLOAD_TYPE, PWUploader.UPLOAD_TYPE_IMAGE));
        ApiRequestWrapper.openAPIGET(this, params, AsynHttpClient.API_QINIU_TOKEN, new MsgStructure() {
            @Override
            public void onReceive(JSONObject data) {
                PWUploader uploader = PWUploader.getInstance();
                uploader.add(imageItem.sourcePath, data.optString("key"), data.optString("token"), new UploadCallback() {
                    @Override
                    public void onComplete(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                        imageItem.imageKey = key;

                        imageUploadPathList.add(key);
                        ImageModel imageModel = new ImageModel();
                        if (TextUtils.isEmpty(imageItem.thumbnailPath)) {
                            imageModel.thumbnail_url = ImageDownloader.Scheme.FILE.wrap(imageItem.sourcePath);
                        } else {
                            imageModel.thumbnail_url = ImageDownloader.Scheme.FILE.wrap(imageItem.thumbnailPath);
                        }
                        imageModel.image_url = ImageDownloader.Scheme.FILE.wrap(imageItem.sourcePath);
                        imageList.add(imageModel);


                        if (count == size - 1) {
                            final FeedFlowModel mModel = new FeedFlowModel();
                            mModel.setImageList(imageList);

                            JSONArray array = new JSONArray(imageUploadPathList);
                            JSONObject jsObject = new JSONObject();
                            try {
                                jsObject.put("images", array);
                                if (imageUploadPathList.size() == 1) {
                                    int[] wandh = PWUtils.getImageUrl(mDataList.get(0).sourcePath);
                                    if (wandh != null && wandh.length >= 2) {
                                        jsObject.put("width", wandh[0]);
                                        jsObject.put("height", wandh[1]);

                                        mModel.imageWidth = wandh[0];
                                        mModel.imageHeight = wandh[1];
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            ArrayList<NameValuePair> post_params = new ArrayList<NameValuePair>();
                            post_params.add(new BasicNameValuePair(AsynHttpClient.KEY_TOPIC_EXTRA, jsObject.toString()));
                            mHandler.post(() -> postData(mModel, post_params));
                        }
                        count++;
                        currentImgSize = 0;
//                        Message message = mHandler.obtainMessage();
//                        message.what = WHAT_UPLOAD_IMG_SUCCESS;
//                        message.obj = imgModel;
//                        mHandler.sendMessage(message);
                    }

                    @Override
                    public void onFailure(String key, ResponseInfo responseInfo, JSONObject jsonObject) {
                        //mHandler.sendEmptyMessage(WHAT_UPLOAD_IMG_ERROR);
                        count = 0;
                        dismissAnimLoading();
                    }
                });
            }

            @Override
            public void onError(int error, Object ret) {
                //mHandler.sendEmptyMessage(WHAT_UPLOAD_IMG_ERROR);
                count = 0;
                dismissAnimLoading();
            }
        });
    }
}

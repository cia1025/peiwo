package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.eventbus.EventBus;
import me.peiwo.peiwo.eventbus.event.MsgImageEvent;
import me.peiwo.peiwo.fragment.MsgShowAlbumFolderFragment;
import me.peiwo.peiwo.fragment.MsgShowAlbumTiledFragment;
import me.peiwo.peiwo.model.AblumImageModel;
import me.peiwo.peiwo.util.ImageUtil;
import me.peiwo.peiwo.util.MsgImageKeeper;
import me.peiwo.peiwo.util.group.ChatImageWrapper;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MsgShowAlbumActvity extends BaseActivity {

    public static final String MSG_IMG_URLS = "msg_img_urls";
    public static String ALBUM_SHOW_MODE = "album_show_mode";
    public static int ALBUM_SHOW_TILED = 1;
    public static int ALBUM_SHOW_FLODER = 2;
    private int currentMode;
    public static int MAX_NUM = 5;
    private int PAGE_LIMIT = 2;
    private Subscription subscription;

    @Bind(R.id.msg_show_album_vp)
    ViewPager msg_show_album_vp;
    private String mImageKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_show_album_actvity);
        EventBus.getDefault().register(this);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (msg_show_album_vp.getCurrentItem() == 1) {
            currentMode = ALBUM_SHOW_TILED;
            loadAlbum();
        }
    }

    private void init() {
        setTitle("选择图片");
        Intent intent = getIntent();
        if (intent != null) {
            currentMode = intent.getIntExtra(ALBUM_SHOW_MODE, ALBUM_SHOW_TILED);
        }
        msg_show_album_vp.setOffscreenPageLimit(PAGE_LIMIT);
        msg_show_album_vp.setAdapter(new MsgShowAlbumAdapter(getSupportFragmentManager()));
        msg_show_album_vp.post(() -> loadAlbum());

    }


    @Override
    public void left_click(View v) {
        if (msg_show_album_vp.getCurrentItem() == 1) {
            currentMode = ALBUM_SHOW_FLODER;
            loadAlbum();
            msg_show_album_vp.setCurrentItem(0);
        } else {
            MsgImageKeeper.getInstance().clear();
            super.left_click(v);
        }
    }

    private void loadAlbum() {
        Observable<List<String>> albumUrlObservable = ChatImageWrapper.scanExternalImages(this, true);
        if (currentMode == ALBUM_SHOW_FLODER) {
            loadAlbumWithFolder(albumUrlObservable);
        } else if (currentMode == ALBUM_SHOW_TILED) {
            loadAlbumWithTiled(albumUrlObservable);
        }

    }

    private void loadAlbumWithTiled(Observable<List<String>> albumUrlObservable) {
        subscription = albumUrlObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<String>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<String> strings) {
                        String tag = makeFragmentName(msg_show_album_vp.getId(), 1);
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
                        if (fragment != null && fragment instanceof MsgShowAlbumTiledFragment) {
                            ((MsgShowAlbumTiledFragment) fragment).updateImgUrlList(strings);
                            msg_show_album_vp.setCurrentItem(1);
                        }
                    }
                });
    }


    private void loadAlbumWithFolder(Observable<List<String>> albumUrlObservable) {
        subscription = albumUrlObservable.subscribeOn(Schedulers.io()).map(this::transAlbumToModel).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<AblumImageModel>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<AblumImageModel> ablumImageModels) {
                        String tag = makeFragmentName(msg_show_album_vp.getId(), 0);
                        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(tag);
                        if (fragmentByTag != null && fragmentByTag instanceof MsgShowAlbumFolderFragment) {
                            ((MsgShowAlbumFolderFragment) fragmentByTag).updateAlbumFolderList(ablumImageModels);
                            msg_show_album_vp.setCurrentItem(0);
                        }


                    }
                });
    }

    private List<AblumImageModel> transAlbumToModel(List<String> urlsList) {

        List<AblumImageModel> ablumImageModelList = new ArrayList<>();
        Map<String, AblumImageModel> ablumImageModelMap = new HashMap<>();
        int size = urlsList.size();
        for (int j = 0; j < size; j++) {
            String imgUrlPath = urlsList.get(j);
            if (!TextUtils.isEmpty(imgUrlPath)) {
                String folderName = new File(imgUrlPath).getParentFile().getName();
                if (ablumImageModelMap.containsKey(folderName)) {
                    ablumImageModelMap.get(folderName).childs.add(imgUrlPath);
                } else {
                    AblumImageModel ablumImageModel = new AblumImageModel(imgUrlPath, folderName);
                    ablumImageModel.childs = new ArrayList<>();
                    ablumImageModel.childs.add(imgUrlPath);
                    ablumImageModelMap.put(folderName, ablumImageModel);
                }
            }
        }

        for (Map.Entry<String, AblumImageModel> entry : ablumImageModelMap.entrySet()) {
            AblumImageModel ablumImageModel = entry.getValue();
            ablumImageModelList.add(ablumImageModel);
        }

        return ablumImageModelList;
    }

    public static String makeFragmentName(int containerViewId, long id) {
        return "android:switcher:" + containerViewId + ":" + id;
    }

    public void takePicture() {
        mImageKey = String.valueOf(System.currentTimeMillis());
        ImageUtil.startImgPickerCamera(this, ImageUtil.PICK_FROM_CAMERA, ImageUtil.getPathForCameraCrop(mImageKey));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ImageUtil.PICK_FROM_CAMERA:
                    File src = ImageUtil.getPathForCameraCrop(mImageKey);
                    MsgImageKeeper.getInstance().add(src.getAbsolutePath());
                    sendImgMsg();
                    break;

                default:
                    break;
            }
        }
    }


    public void sendImgMsg() {
        Intent intent = new Intent();
        ArrayList<String> selectedUrlList = (ArrayList<String>) MsgImageKeeper.getInstance().getImgList();
        intent.putStringArrayListExtra(MSG_IMG_URLS, selectedUrlList);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void clickAlbumFolderItem(AblumImageModel albumFolder) {
        String tag = makeFragmentName(msg_show_album_vp.getId(), 1);
        Fragment fragmentByTag = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragmentByTag != null && fragmentByTag instanceof MsgShowAlbumTiledFragment) {
            ((MsgShowAlbumTiledFragment) fragmentByTag).updateImgUrlList(albumFolder.childs);
            msg_show_album_vp.setCurrentItem(1);
        }
    }

    private class MsgShowAlbumAdapter extends FragmentPagerAdapter {
        private int PAGE_NUM = 2;

        public MsgShowAlbumAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return MsgShowAlbumFolderFragment.getInstance();
            } else {
                return MsgShowAlbumTiledFragment.getInstance();
            }
        }

        @Override
        public int getCount() {
            return PAGE_NUM;
        }
    }

    @Override
    public void finish() {
        unsubscription();
        super.finish();
    }

    private void unsubscription() {
        if (subscription != null && !subscription.isUnsubscribed())
            subscription.unsubscribe();
        subscription = null;
    }

    public void onEventMainThread(MsgImageEvent event) {
        sendImgMsg();
    }

    @Override
    public void onBackPressed() {
        MsgImageKeeper.getInstance().clear();
        super.onBackPressed();
    }
}

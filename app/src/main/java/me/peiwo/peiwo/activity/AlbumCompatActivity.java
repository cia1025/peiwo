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
import me.peiwo.peiwo.adapter.AlbumFolderFragment;
import me.peiwo.peiwo.adapter.AlbumSectionFragment;
import me.peiwo.peiwo.model.AblumImageModel;
import me.peiwo.peiwo.util.ImageUtil;
import me.peiwo.peiwo.util.group.ChatImageWrapper;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AlbumCompatActivity extends BaseActivity {
    public static final String K_ALBUM_RST = "album";
    public static final String K_ALBUM_RST_COUNT = "album_count";
    @Bind(R.id.vp_album)
    ViewPager vp_album;
    private String mImageKey;
    //private List<AblumImageModel> allData = new ArrayList<>();

    public static final String CHOOSE_MODE = "choose";
    public static final int CHOOSE_MODE_FOLDER = 0;
    public static final int CHOOSE_MODE_SECTION = 1;

    private Subscription subscription;

    private int max_count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_compat);
        init();
    }

    private void init() {
        setTitle("选择图片");
        max_count = getIntent().getIntExtra(K_ALBUM_RST_COUNT, 1);
        vp_album.setOffscreenPageLimit(2);
        vp_album.setAdapter(new AlbumAdapter(getSupportFragmentManager()));
        vp_album.post(this::LoadExternalImages);
    }

    private void LoadExternalImages() {
        Intent intent = getIntent();
        if (intent.getIntExtra(CHOOSE_MODE, CHOOSE_MODE_FOLDER) == CHOOSE_MODE_FOLDER) {
            subscriberFolder();
        } else if (intent.getIntExtra(CHOOSE_MODE, CHOOSE_MODE_FOLDER) == CHOOSE_MODE_SECTION) {
            subscriberSection();
        }
    }

    private void subscriberSection() {
        Observable<List<String>> query = ChatImageWrapper.scanExternalImages(this, false);
        subscription = query.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<String>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<String> items) {
                String tag = makeFragmentName(vp_album.getId(), 1);
                Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
                if (f != null && f instanceof AlbumSectionFragment) {
                    ((AlbumSectionFragment) f).flushData(items);
                    vp_album.setCurrentItem(1);
                }
            }
        });
    }

    private void subscriberFolder() {
        Observable<List<String>> query = ChatImageWrapper.scanExternalImages(this, false);
        subscription = query.subscribeOn(Schedulers.io()).map(this::prepareData).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<AblumImageModel>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<AblumImageModel> ablumImageModels) {
                String tag = makeFragmentName(vp_album.getId(), 0);
                Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
                if (f != null && f instanceof AlbumFolderFragment) {
                    ((AlbumFolderFragment) f).flushData(ablumImageModels);
                }
            }
        });
    }

    private void unSubscribed() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        subscription = null;
    }

    @Override
    public void finish() {
        unSubscribed();
        super.finish();
    }

    private List<AblumImageModel> prepareData(List<String> data) {
        //Log.i("album", "prepareData == " + (Looper.myLooper() == Looper.getMainLooper()));
        //long start = System.currentTimeMillis();
        List<AblumImageModel> allData = new ArrayList<>();
        Map<String, AblumImageModel> groupData = new HashMap<>();
        for (int i = 0, z = data.size(); i < z; i++) {
            String sourcePath = data.get(i);
            if (!TextUtils.isEmpty(sourcePath)) {
                String folderName = new File(sourcePath).getParentFile().getName();
                if (groupData.containsKey(folderName)) {
                    groupData.get(folderName).childs.add(sourcePath);
                } else {
                    AblumImageModel ablumImageModel = new AblumImageModel(sourcePath, folderName);
                    ablumImageModel.childs = new ArrayList<>();
                    ablumImageModel.childs.add(sourcePath);
                    groupData.put(folderName, ablumImageModel);
                }
            }
        }
        for (Map.Entry<String, AblumImageModel> entry : groupData.entrySet()) {
            AblumImageModel model = entry.getValue();
            allData.add(model);
        }
        return allData;
        //Log.i("rongs", "temp==" + (System.currentTimeMillis() - start));
    }

    private String makeFragmentName(int viewId, int index) {
        return "android:switcher:" + viewId + ":" + index;
    }

    private class AlbumAdapter extends FragmentPagerAdapter {
        public AlbumAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return AlbumFolderFragment.newInstance();
            } else {
                return AlbumSectionFragment.newInstance(max_count);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public void setlectFolderItem(AblumImageModel model) {
        String tag = makeFragmentName(vp_album.getId(), 1);
        Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
        if (f != null && f instanceof AlbumSectionFragment) {
            ((AlbumSectionFragment) f).flushData(model.childs);
            vp_album.setCurrentItem(1);
        }
    }


    @Override
    public void left_click(View v) {
        if (vp_album.getCurrentItem() == 1) {
            String tag = makeFragmentName(vp_album.getId(), 1);
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
            if (fragment != null && fragment instanceof AlbumSectionFragment) {
                List<String> sectionList = ((AlbumSectionFragment) fragment).getSectionData();
                String tag_folder = makeFragmentName(vp_album.getId(), 0);
                Fragment f_folder = getSupportFragmentManager().findFragmentByTag(tag_folder);
                if (f_folder != null && f_folder instanceof AlbumFolderFragment) {
                    AlbumFolderFragment albumFolderFragment = ((AlbumFolderFragment) f_folder);
                    if (albumFolderFragment.needFlush()) {
                        albumFolderFragment.flushData(prepareData(sectionList));
                    }
                    vp_album.setCurrentItem(0);
                }
            }

        } else {
            super.left_click(v);
        }
    }

    public void resultAlbum(ArrayList<String> rst) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(K_ALBUM_RST, rst);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void pickInCamera() {
        mImageKey = String.valueOf(System.currentTimeMillis());
        if (!TextUtils.isEmpty(mImageKey))
            ImageUtil.startImgPickerCamera(this, ImageUtil.PICK_FROM_CAMERA, ImageUtil.getPathForCameraCrop(mImageKey));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ImageUtil.PICK_FROM_CAMERA:
                    File src = ImageUtil.getPathForCameraCrop(mImageKey);
                    //ImageSize size = ChatImageWrapper.computeImageSize(src.getPath());
                    //ImageItem imageItem = new ImageItem(src.getAbsolutePath(), src.getAbsolutePath(), 0, 0);
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(src.getAbsolutePath());
                    resultAlbum(arrayList);
                    break;

                default:
                    break;
            }
        }
    }
}

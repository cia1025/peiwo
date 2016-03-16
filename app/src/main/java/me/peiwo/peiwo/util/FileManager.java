package me.peiwo.peiwo.util;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import rx.Observable;
import rx.Subscriber;

public final class FileManager {

    /**
     * 缓存基本目录
     */

    public static final String APP_CACHE_DIR = "/data/data/me.peiwo.peiwo/peiwo";

    private static final String BASE_PATH = "peiwo";

    private static final String IMAGE = "image";
    public static final String VOICE_FOLDER = "voice";
    private static final String TEMP_FILE = "temp_file";
    private static final String LOG_FILE = "LogFolder";
    private static final String CHAT_IMAGE_COPY = "chat_image";

    public static File getChatImageCopyPath() {
        return getPath(CHAT_IMAGE_COPY);
    }

    public static File getImagePath() {
        return getPath(IMAGE);
    }

    public static File getTempFilePath() {
        return getPath(TEMP_FILE);
    }

    public static File getLogPath() {
        return getPath(LOG_FILE);
    }

    public static File getVoicePath() {
        return getPath(VOICE_FOLDER);
    }

    private static File getPath(String fileName) {
        File file = new File(getBasepath(), fileName);
        if (!file.exists())
            file.mkdirs();
        return file;
    }

    public static boolean isSDCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    private static File getBasepath() {
        File appCacheDir = null;
        if (isSDCardAvailable()) {
            appCacheDir = new File(Environment.getExternalStorageDirectory(), BASE_PATH);
        }
        if (appCacheDir == null) {

            appCacheDir = new File(APP_CACHE_DIR);
        }
        if (!appCacheDir.exists())
            appCacheDir.mkdirs();
        return appCacheDir;
    }


    /**
     * copy file
     *
     * @param src
     * @param dst
     * @throws IOException
     */
    public static Observable<Boolean> copyFile(File src, File dst) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                FileInputStream inStream = null;
                FileOutputStream outStream = null;
                try {
                    inStream = new FileInputStream(src);
                    outStream = new FileOutputStream(dst);
                    FileChannel inChannel = inStream.getChannel();
                    FileChannel outChannel = outStream.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    subscriber.onNext(true);
                    subscriber.onCompleted();
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                } finally {
                    try {
                        if (inStream != null)
                            inStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (outStream != null)
                            outStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


}

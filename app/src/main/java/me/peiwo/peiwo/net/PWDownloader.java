package me.peiwo.peiwo.net;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import me.peiwo.peiwo.callback.DownloadCallback;
import okio.*;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import java.io.File;
import java.io.IOException;

/**
 * Created by fuhaidong on 15/10/28.
 */
public class PWDownloader {
    private OkHttpClient mClient;


    private PWDownloader() {
        mClient = new OkHttpClient();
    }

    private static class SingletonHolder {
        public static final PWDownloader INSTANCE = new PWDownloader();
    }

    public static PWDownloader getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 下载文件类
     *
     * @param url      请求地址
     * @param target   目标文件
     * @param callback 主线程回调
     * @return 文件地址
     */
    public void add(String url, final File target, final DownloadCallback callback) {
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) throws IOException {
                BufferedSink sink = Okio.buffer(Okio.sink(target));
                sink.writeAll(response.body().source());
                sink.close();
                if (callback == null) return;
                Observable.just(target.getAbsolutePath()).observeOn(AndroidSchedulers.mainThread()).subscribe(callback::onComplete);
            }

            @Override
            public void onFailure(Request request, final IOException e) {
                if (callback == null) return;
                Observable.just(e).observeOn(AndroidSchedulers.mainThread()).subscribe(ex -> {
                    callback.onFailure(target.getAbsolutePath(), ex);
                });
            }
        });
    }

    /**
     * 带进度的下载
     *
     * @param url              remote url
     * @param target           文件
     * @param progressListener 进度回调
     */
    public void add(String url, final File target, ProgressListener progressListener) {
        Request request = new Request.Builder().url(url).build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) {
                try {
                    long limit = response.body().contentLength();
                    if (limit <= 0) {
                        if (progressListener != null)
                            progressListener.failure(new IOException("can not read content lengh"));
                        return;
                    }
                    BufferedSink sink = Okio.buffer(sink(Okio.sink(target), limit, progressListener));
                    sink.writeAll(response.body().source());
                    sink.close();
                } catch (IOException e) {
                    if (progressListener != null) progressListener.failure(e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Request request, final IOException e) {
                if (progressListener != null) progressListener.failure(e);
            }
        });
    }

    public interface ProgressListener {
        /**
         * 进度提醒
         *
         * @param totalBytesWritten 累计写入文件的字节数
         * @param limit             文件总共字节数
         * @param done              是否完成
         */
        void update(long totalBytesWritten, long limit, boolean done);

        void failure(Exception e);
    }

    private Sink sink(Sink sink, final long limit, final ProgressListener progressListener) {
        return new ForwardingSink(sink) {
            long totalBytesWritten = 0L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                totalBytesWritten += byteCount;
                if (progressListener != null)
                    progressListener.update(totalBytesWritten, limit, false);
            }

            @Override
            public void close() throws IOException {
                super.close();
                if (progressListener != null)
                    progressListener.update(totalBytesWritten, limit, true);
            }
        };
    }
}

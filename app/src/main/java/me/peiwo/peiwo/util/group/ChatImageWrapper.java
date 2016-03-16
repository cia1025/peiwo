package me.peiwo.peiwo.util.group;

import android.content.Context;
import android.database.Cursor;
import android.graphics.*;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import me.peiwo.peiwo.BuildConfig;
import me.peiwo.peiwo.model.ImageItem;
import me.peiwo.peiwo.util.CustomLog;
import me.peiwo.peiwo.util.FileManager;
import me.peiwo.peiwo.util.Md5Util;
import rx.Observable;
import rx.Subscriber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by fuhaidong on 15/12/14.
 */
public final class ChatImageWrapper {
    public static final int MAX_WIDTH = 200;
    public static final int MAX_HEIGHT = 200;

    public static ImageSize computeChatImageViewSize(int src_width, int src_height) {
        int rst_width;
        int rst_height;
        if (src_width > MAX_WIDTH) {
            rst_height = MAX_WIDTH * src_height / src_width;
            rst_width = MAX_WIDTH;
        } else if (src_height > MAX_HEIGHT) {
            rst_width = MAX_HEIGHT * src_width / src_height;
            rst_height = MAX_HEIGHT;
        } else {
            rst_width = src_width;
            rst_height = src_height;
        }
        return new ImageSize(rst_width, rst_height);
    }

//    public static ImageSize computeImageSize(String local_path) {
//        File f = new File(local_path);
//        if (f.exists() && f.length() > 0) {
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeFile(local_path, options);
//            //options.inJustDecodeBounds = false;
//            int orientation = ImageUtil.getExifOrientation(local_path);
//            if (orientation != ExifInterface.ORIENTATION_NORMAL && orientation != ExifInterface.ORIENTATION_UNDEFINED) {
//                return new ImageSize(options.outHeight, options.outWidth);
//            }
//            return new ImageSize(options.outWidth, options.outHeight);
//        } else {
//            return null;
//        }
//    }

    /**
     * 扫描磁盘图片
     *
     * @param context
     * @param section 是否扫描部分
     */
    public static Observable<List<String>> scanExternalImages(Context context, boolean section) {
        return Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> subscriber) {
                try {
                    List<String> data = new ArrayList<>();
                    String[] IMAGE_PROJECTION = {MediaStore.Images.Media.DATA, MediaStore.Images.Media.DATE_ADDED, MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.SIZE};
                    String order = IMAGE_PROJECTION[1];
                    if (section) {
                        order += " DESC LIMIT 50";
                    }
                    String selecton = IMAGE_PROJECTION[3] + " > 0 AND " + IMAGE_PROJECTION[2] + " = ? OR " + IMAGE_PROJECTION[2] + " = ? OR " + IMAGE_PROJECTION[2] + " = ? OR " + IMAGE_PROJECTION[2] + " = ? OR " + IMAGE_PROJECTION[2] + " = ? OR " + IMAGE_PROJECTION[2] + " = ?";
                    Cursor fullCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                            selecton, new String[]{"image/jpeg", "image/JPEG", "image/jpg", "image/JPG", "image/png", "image/PNG"}, order);
                    if (fullCursor != null) {
                        while (fullCursor.moveToNext()) {
                            String fullPath = fullCursor.getString(fullCursor.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                            data.add(fullPath);
                        }
                        fullCursor.close();
                    }
                    //Log.i("album", "scanExternalImages == " + (Looper.myLooper() == Looper.getMainLooper()));
                    subscriber.onNext(data);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public static ImageItem compressImage(String local_path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
        //you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(local_path, options);
        int actualWidth = options.outWidth;
        int actualHeight = options.outHeight;
        File local_file = new File(FileManager.getTempFilePath(), Md5Util.getMd5code(local_path));
        if (local_file.exists() && local_file.length() > 0) {
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
            }
            return new ImageItem(local_path, local_path, actualWidth, actualHeight);
        }
        /******/
        try {
            //max Height and width values of the compressed image is taken as 816x612
            float maxHeight = 1134.0f;
            float maxWidth = 640.0f;
            float imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;
            //width and height values are set maintaining the aspect ratio of the image
            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;
                }
            }
            //setting inSampleSize value allows to load a scaled down version of the original image
            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
            //inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;
            //this options allow android to claim the bitmap memory if it runs low on memory
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];
            //load the bitmap from its path
            if (bmp != null &&!bmp.isRecycled()) {
                bmp.recycle();
            }
            bmp = BitmapFactory.decodeFile(local_path, options);
            Bitmap scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;
            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
            Canvas canvas = new Canvas(scaledBitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
            if (!bmp.isRecycled()) {
                bmp.recycle();
            }
            //check the rotation of the image and display it properly
            ExifInterface exif = new ExifInterface(local_path);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }
            Bitmap final_bitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            if (!scaledBitmap.isRecycled()) {
                scaledBitmap.recycle();
            }
            String filename = local_file.getAbsolutePath();
            FileOutputStream out = new FileOutputStream(filename);

            //write the compressed bitmap at the destination specified by filename.
            final_bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

            int w = scaledBitmap.getWidth();
            int h = scaledBitmap.getHeight();
            if (!final_bitmap.isRecycled()) {
                final_bitmap.recycle();
            }
            return new ImageItem(filename, filename, w, h);
        } catch (OutOfMemoryError | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public static void deleteTempFiles() {
        File dir = FileManager.getTempFilePath();
        if (dir != null && dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                boolean b = f.delete();
                if (BuildConfig.DEBUG)
                    Log.i("del", "del is " + b);
            }
        }
    }

    public static Observable<ImageItem> createCompressObservable(List<String> imageItems) {
        return Observable.from(imageItems).map(local_path -> {
            ImageItem new_item = ChatImageWrapper.compressImage(local_path);
            //Log.i("compress", "loop == " + (Looper.myLooper() == Looper.getMainLooper()));
            if (new_item == null || new_item.width == 0 || new_item.height == 0 || TextUtils.isEmpty(new_item.thumbnailPath))
                return null;
            return new_item;
        });
    }

}

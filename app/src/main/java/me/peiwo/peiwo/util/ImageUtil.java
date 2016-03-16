package me.peiwo.peiwo.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import me.peiwo.peiwo.R;

public class ImageUtil {

    public static final String IMAGE_PATH = "image-path";

    //读取图片的旋转角度
    public static int getExifOrientation(String filepath) {
        try {
            ExifInterface exif = new ExifInterface(filepath);
            if (exif != null) {
                return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ExifInterface.ORIENTATION_NORMAL;
    }


    public static Bitmap getBitmap(String path, Activity context) {

        Uri uri = Uri.fromFile(new File(path));
        InputStream in = null;
        try {
            ContentResolver mContentResolver = context.getContentResolver();
            in = mContentResolver.openInputStream(uri);

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            int IMAGE_MAX_SIZE = 1024;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = mContentResolver.openInputStream(uri);
            Bitmap b = BitmapFactory.decodeStream(in, null, o2);
            in.close();

            return b;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void startCrop(Activity a, File src, File dest) {

//		Intent cropIntent = new Intent("com.android.camera.action.CROP");
//		cropIntent.setDataAndType(Uri.fromFile(src), "image/*");
//		cropIntent.putExtra("crop", "true");
//		cropIntent.putExtra("aspectX", 1);
//		cropIntent.putExtra("aspectY", 1);
//		cropIntent.putExtra("outputX", 640);
//		cropIntent.putExtra("outputY", 640);
//		cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(dest));
//		cropIntent.putExtra("outputFormat",
//				Bitmap.CompressFormat.JPEG.toString());
//		cropIntent.putExtra("return-data", true);
//		a.startActivityForResult(cropIntent, Util.PICK_FROM_GALLERY);

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(Uri.fromFile(src), "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("outputX", 640);
        intent.putExtra("outputY", 640);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(dest));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
//        {
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//        }
//        else
//        {
//            intent.setAction(Intent.ACTION_PICK);
//            intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        }

        a.startActivityForResult(intent, PICK_FROM_GALLERY);

    }

    public static void startAlbum(Activity act, int requestCode) {
        Intent it = new Intent(Intent.ACTION_PICK);
        it.setType("image/*");
        act.startActivityForResult(it, requestCode);
    }

    public static DisplayImageOptions getRoundedOptionsWithRadius(int radius) {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_default_avatar)
                .showImageForEmptyUri(R.drawable.ic_default_avatar)
                .showImageOnFail(R.drawable.ic_default_avatar).cacheInMemory(true)
                .cacheOnDisk(true).displayer(new RoundedBitmapDisplayer(radius))
                .build();
    }

    public static DisplayImageOptions getRoundedOptions() {
        return getRoundedOptionsWithRadius(10);
    }

    public static DisplayImageOptions getLocalmageOptions() {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_default_avatar)
                .showImageForEmptyUri(R.drawable.ic_default_avatar)
                .showImageOnFail(R.drawable.ic_default_avatar).cacheInMemory(false)
                .cacheOnDisk(false)
                .build();
    }

    public static byte[] bmpToByteArray(final Bitmap bmp) {
        byte[] result = null;
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 95, output);
            result = output.toByteArray();
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 写bitmap到磁盘
     *
     * @param bmp
     * @param filename
     * @return
     */
    public static String saveBitmap(Bitmap bmp, String filename) {
        return saveBitmap(bmp, filename, 90);
    }

    public static String saveBitmap(Bitmap bmp, String filename, int quality) {
        File dest = new File(FileManager.getTempFilePath(), filename);
        try {
            FileOutputStream out = new FileOutputStream(dest);
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            return null;
        } finally {
//            if (bmp != null && !bmp.isRecycled())
//                bmp.recycle();
        }
        return dest.getAbsolutePath();
    }

    public static File getPathForCameraCrop(String imgKey) {
        return new File(FileManager.getTempFilePath(), String.format(
                Locale.getDefault(), "%s", imgKey));
    }

    public static File getPathForUpload(String imgKey) {
        return new File(FileManager.getTempFilePath(), String.format(
                Locale.getDefault(), "%s%s", imgKey, imgKey));
    }

    // /**
    // * 水平方向模糊度
    // */
    // private static float hRadius = 10;
    // /**
    // * 竖直方向模糊度
    // */
    // private static float vRadius = 10;
    // /**
    // * 模糊迭代度
    // */
    // private static int iterations = 7;
    //
    // /**
    // * 高斯模糊
    // */
    // public static Bitmap boxBlurFilter(Bitmap bmp) {
    // int width = bmp.getWidth();
    // int height = bmp.getHeight();
    // int[] inPixels = new int[width * height];
    // int[] outPixels = new int[width * height];
    // Bitmap bitmap = Bitmap.createBitmap(width, height,
    // Bitmap.Config.ARGB_8888);
    // bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
    // for (int i = 0; i < iterations; i++) {
    // blur(inPixels, outPixels, width, height, hRadius);
    // blur(outPixels, inPixels, height, width, vRadius);
    // }
    // blurFractional(inPixels, outPixels, width, height, hRadius);
    // blurFractional(outPixels, inPixels, height, width, vRadius);
    // bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
    // // Drawable drawable = new BitmapDrawable(bitmap);
    // return bitmap;
    // }
    //
    // public static void blur(int[] in, int[] out, int width, int height,
    // float radius) {
    // int widthMinus1 = width - 1;
    // int r = (int) radius;
    // int tableSize = 2 * r + 1;
    // int divide[] = new int[256 * tableSize];
    //
    // for (int i = 0; i < 256 * tableSize; i++)
    // divide[i] = i / tableSize;
    //
    // int inIndex = 0;
    //
    // for (int y = 0; y < height; y++) {
    // int outIndex = y;
    // int ta = 0, tr = 0, tg = 0, tb = 0;
    //
    // for (int i = -r; i <= r; i++) {
    // int rgb = in[inIndex + clamp(i, 0, width - 1)];
    // ta += (rgb >> 24) & 0xff;
    // tr += (rgb >> 16) & 0xff;
    // tg += (rgb >> 8) & 0xff;
    // tb += rgb & 0xff;
    // }
    //
    // for (int x = 0; x < width; x++) {
    // out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16)
    // | (divide[tg] << 8) | divide[tb];
    //
    // int i1 = x + r + 1;
    // if (i1 > widthMinus1)
    // i1 = widthMinus1;
    // int i2 = x - r;
    // if (i2 < 0)
    // i2 = 0;
    // int rgb1 = in[inIndex + i1];
    // int rgb2 = in[inIndex + i2];
    //
    // ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
    // tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
    // tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
    // tb += (rgb1 & 0xff) - (rgb2 & 0xff);
    // outIndex += height;
    // }
    // inIndex += width;
    // }
    // }
    //
    // public static void blurFractional(int[] in, int[] out, int width,
    // int height, float radius) {
    // radius -= (int) radius;
    // float f = 1.0f / (1 + 2 * radius);
    // int inIndex = 0;
    //
    // for (int y = 0; y < height; y++) {
    // int outIndex = y;
    //
    // out[outIndex] = in[0];
    // outIndex += height;
    // for (int x = 1; x < width - 1; x++) {
    // int i = inIndex + x;
    // int rgb1 = in[i - 1];
    // int rgb2 = in[i];
    // int rgb3 = in[i + 1];
    //
    // int a1 = (rgb1 >> 24) & 0xff;
    // int r1 = (rgb1 >> 16) & 0xff;
    // int g1 = (rgb1 >> 8) & 0xff;
    // int b1 = rgb1 & 0xff;
    // int a2 = (rgb2 >> 24) & 0xff;
    // int r2 = (rgb2 >> 16) & 0xff;
    // int g2 = (rgb2 >> 8) & 0xff;
    // int b2 = rgb2 & 0xff;
    // int a3 = (rgb3 >> 24) & 0xff;
    // int r3 = (rgb3 >> 16) & 0xff;
    // int g3 = (rgb3 >> 8) & 0xff;
    // int b3 = rgb3 & 0xff;
    // a1 = a2 + (int) ((a1 + a3) * radius);
    // r1 = r2 + (int) ((r1 + r3) * radius);
    // g1 = g2 + (int) ((g1 + g3) * radius);
    // b1 = b2 + (int) ((b1 + b3) * radius);
    // a1 *= f;
    // r1 *= f;
    // g1 *= f;
    // b1 *= f;
    // out[outIndex] = (a1 << 24) | (r1 << 16) | (g1 << 8) | b1;
    // outIndex += height;
    // }
    // out[outIndex] = in[width - 1];
    // inIndex += width;
    // }
    // }
    //
    // public static int clamp(int x, int a, int b) {
    // return (x < a) ? a : (x > b) ? b : x;
    // }


    public static void startImgPickerCamera(Activity activity, int reqCodeCamera,
                                            File outPutFile) {
        Intent cameraIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent
                .putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outPutFile));
        cameraIntent.putExtra("outputFormat",
                Bitmap.CompressFormat.JPEG.toString());
        activity.startActivityForResult(cameraIntent, reqCodeCamera);
    }

    public static void startImgPickerGallery(Activity activity, int reqCode,
                                             File outPutFile) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
//        intent.putExtra("outputX", 640);
//        intent.putExtra("outputY", 640);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outPutFile));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        try {
            activity.startActivityForResult(intent, reqCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, "没有找到相关处理程序", Toast.LENGTH_LONG).show();
        }
    }


    public static final int PICK_FROM_CAMERA = 10;
    public static final int PICK_FROM_GALLERY = 11;
    public static final String STARTUP_SCREEN_PATH_NAME = "startup_screen.jpg";


    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
//        final int height = options.outHeight;
//        final int width = options.outWidth;
//        int inSampleSize = 1;
//
//        if (height > reqHeight || width > reqWidth) {
//
//            final int halfHeight = height / 2;
//            final int halfWidth = width / 2;
//
//            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
//            // height and width larger than the requested height and width.
//            while ((halfHeight / inSampleSize) > reqHeight
//                    && (halfWidth / inSampleSize) > reqWidth) {
//                inSampleSize *= 2;
//            }
//        }

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public static Bitmap rotateImage(Bitmap src, float degree) {
        // create new matrix
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

}

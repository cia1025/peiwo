package me.peiwo.peiwo.util.group;

import android.content.Context;
import android.content.res.TypedArray;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.model.EmotionModel;
import me.peiwo.peiwo.model.GIFModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fuhaidong on 15/12/15.
 */
public class ExpressionData {
    private ExpressionData(Context context) {
        prepareData(context);
    }

    private Map<String, EmotionModel> emotionMappingData = new HashMap<>();
    private Map<String, GIFModel> gifMappingData = new HashMap<>();

    //private List<EmotionModel> emotionModels = new ArrayList<>();
    //private List<GIFModel> gifModels = new ArrayList<>();

    private static ExpressionData INSTANCE;

    public static ExpressionData getInstance(Context context) {
        synchronized (ExpressionData.class) {
            if (INSTANCE == null) {
                INSTANCE = new ExpressionData(context);
            }
        }
        return INSTANCE;
    }

    public List<EmotionModel> getEmotionModels() {
        List<EmotionModel> data = new ArrayList<>();
        for (Map.Entry<String, EmotionModel> entry : emotionMappingData.entrySet()) {
            data.add(entry.getValue());
        }
        return data;
    }

    public Map<String, EmotionModel> getEmotionMappingData() {
        return this.emotionMappingData;
    }

    public Map<String, GIFModel> getGifMappingData() {
        return this.gifMappingData;
    }

    public List<GIFModel> getGifModels() {
        List<GIFModel> data = new ArrayList<>();
        for (Map.Entry<String, GIFModel> entry : gifMappingData.entrySet()) {
            data.add(entry.getValue());
        }
        return data;
    }


    private void prepareData(Context context) {
        prepareEmotionData(context);

        prepareGIFData(context);

    }

    private void prepareEmotionData(Context context) {
        TypedArray typedArrayResId = context.getResources().obtainTypedArray(R.array.emotion_res_id);
        int[] array_res_id = new int[typedArrayResId.length()];
        fetchResIdArray(typedArrayResId, array_res_id);
        typedArrayResId.recycle();


        String[] array_regular = context.getResources().getStringArray(R.array.emotion_regular);
        String regular = null;
        for (int i = 0, z = array_res_id.length; i < z; i++) {
            if (array_regular.length > i) {
                regular = array_regular[i];
            }
            emotionMappingData.put(regular, new EmotionModel(array_res_id[i], regular));
        }
    }

    private void prepareGIFData(Context context) {
        TypedArray typedArrayResId = context.getResources().obtainTypedArray(R.array.gif_res_id);
        int[] array_res_id = new int[typedArrayResId.length()];
        fetchResIdArray(typedArrayResId, array_res_id);
        typedArrayResId.recycle();

        TypedArray typedArrayMovieResId = context.getResources().obtainTypedArray(R.array.movie_res_id);
        int[] array_movie_res_id = new int[typedArrayMovieResId.length()];
        fetchResIdArray(typedArrayMovieResId, array_movie_res_id);
        typedArrayMovieResId.recycle();

        String[] array_regular = context.getResources().getStringArray(R.array.gif_regular);
        String regular = null;
        String[] array_title = context.getResources().getStringArray(R.array.gif_title);
        String title = null;
        int movie_res_id = 0;
        for (int i = 0, z = array_res_id.length; i < z; i++) {
            if (array_regular.length > i) {
                regular = array_regular[i];
            }
            if (array_title.length > i) {
                title = array_title[i];
            }
            if (array_movie_res_id.length > i) {
                movie_res_id = array_movie_res_id[i];
            }
            gifMappingData.put(regular, new GIFModel(array_res_id[i], regular, title, movie_res_id));
        }
    }

    private void fetchResIdArray(TypedArray typedArrayResId, int[] array_res_id) {
        for (int i = 0, z = array_res_id.length; i < z; i++) {
            array_res_id[i] = typedArrayResId.getResourceId(i, 0);
        }
    }
}

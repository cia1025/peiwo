package me.peiwo.peiwo.model;

import android.support.annotation.DrawableRes;

/**
 * Created by fuhaidong on 15/11/18.
 */
public class EmotionModel extends ExpressionBaseModel {


    public EmotionModel(@DrawableRes int res_id, String regular) {
        this.res_id = res_id;
        this.regular = regular;
    }
}

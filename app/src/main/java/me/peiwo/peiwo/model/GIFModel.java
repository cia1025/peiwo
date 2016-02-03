package me.peiwo.peiwo.model;

import android.support.annotation.DrawableRes;

/**
 * Created by fuhaidong on 15/11/19.
 */
public class GIFModel extends ExpressionBaseModel {
    public String gif_title;
    public int movie_res_id;

    public GIFModel(@DrawableRes int res_id, String regular, String gif_title, @DrawableRes int movie_res_id) {
        this.res_id = res_id;
        this.regular = regular;
        this.gif_title = gif_title;
        this.movie_res_id = movie_res_id;
    }
}

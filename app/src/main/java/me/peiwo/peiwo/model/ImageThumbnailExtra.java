package me.peiwo.peiwo.model;

/**
 * Created by fuhaidong on 15/12/14.
 */
public class ImageThumbnailExtra {
    public int image_id;
    public String thumbnail_path;
    public int thumbnail_width;
    public int thumbnail_height;

    public ImageThumbnailExtra(int image_id, String thumbnail_path, int thumbnail_width, int thumbnail_height) {
        this.image_id = image_id;
        this.thumbnail_path = thumbnail_path;
        this.thumbnail_width = thumbnail_width;
        this.thumbnail_height = thumbnail_height;
    }
}

package me.peiwo.peiwo.net;

/**
 * Created by wallace on 16/3/15.
 */
public class PWError extends Throwable {
    public int error_code;
    public Object object;

    public PWError(int error_code, Object object) {
        this.error_code = error_code;
        this.object = object;
    }
}

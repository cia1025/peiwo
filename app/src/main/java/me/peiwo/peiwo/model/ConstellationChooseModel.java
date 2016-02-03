package me.peiwo.peiwo.model;

/**
 * Created by fuhaidong on 15/9/28.
 */
public class ConstellationChooseModel {
    public int res_id;
    public String c_name;
    //给服务器传值用
    public String key;

    public ConstellationChooseModel(int res_id, String c_name, String key) {
        this.res_id = res_id;
        this.c_name = c_name;
        this.key = key;
    }
}

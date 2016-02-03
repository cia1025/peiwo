package me.peiwo.peiwo.callback;

import io.rong.imlib.model.Message;

/**
 * Created by fuhaidong on 15/12/16.
 */
public interface ReceiveRongMessageListener {
    void onReceiveRongMessage(Message message, int integer);
}

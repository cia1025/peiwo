package me.peiwo.peiwo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import butterknife.Bind;
import me.peiwo.peiwo.R;
import me.peiwo.peiwo.adapter.GroupchatPacketsAdapter;
import me.peiwo.peiwo.model.groupchat.PacketIconModel;

import java.util.ArrayList;

public class GroupchatMorePacketActivity extends BaseActivity {
    @Bind(R.id.v_recycler_packets)
    RecyclerView v_recycler_packets;

    public static final String K_GROUP_POSITION = "g_p";
    public static final String K_CHILD_POSITION = "c_p";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groupchat_more_packet);
        init();
    }

    private void init() {
        setTitle("选择图片");
        v_recycler_packets.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        v_recycler_packets.setLayoutManager(linearLayoutManager);
        Intent data = getIntent();
        ArrayList<PacketIconModel> models = data.getParcelableArrayListExtra(ChatRedbagActivity.K_MORE_PACKETS);
        if (models == null) {
            return;
        }
        v_recycler_packets.setAdapter(new GroupchatPacketsAdapter(this, models));
    }

    public void resultSelectedPicket(int group_position, int child_position) {
        Intent intent = new Intent();
        intent.putExtra(K_GROUP_POSITION, group_position);
        intent.putExtra(K_CHILD_POSITION, child_position);
        setResult(RESULT_OK, intent);
        finish();
    }

}

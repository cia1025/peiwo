package me.peiwo.peiwo.model.groupchat;

import java.util.List;

/**
 * Created by fuhaidong on 16/1/20.
 */
public class GroupCommandData {
    //    {
//        "target_ids": [
//        {
//            "uid": 1005
//        },
//        {
//            "uid": 1004
//        }
//        ],
//        "user": {
//        "uid": 1001
//    },
//        "expired_at": "324524352456"
//    }
    public List<TUser> target_ids;

    public class TUser {
        public int uid;
    }

    public class User {
        public int uid;
    }

    public long expired_at;
    public Group group;

    public class Group {
        public String group_name;
        public String avatar;
    }
}

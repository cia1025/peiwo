package me.peiwo.peiwo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 更新用户资料的实体,专门用来更新用户资料使用
 *
 * @author Fuhai
 */
public class ProfileForUpdateModel extends PPBaseModel {
    private static final long serialVersionUID = 1L;

    public ProfileForUpdateModel() {
    }

    // uid: 608653738,
    // tags: [ ],
    // birthday: "1993-07-16 00:00:00",
    // avatar_thumbnail:
    // "http://peiwo-photo.bjcnc.img.sohucs.com/s_thumbnail/608653738_34f2e603c77643d310497a961f17bd08",
    // profession: null,
    // state: 1,
    // avatar:
    // "http://peiwo-photo.bjcnc.img.sohucs.com/608653738_34f2e603c77643d310497a961f17bd08",
    // city: null,
    // emotion: 0,
    // slogan: "nfmffjjfj",
    // price: 0,
    // session_data: "b52a67889f112410cd9c0f605a155f00",
    // name: "dfh",
    // province: null,
    // money: 0,
    // images: [
    // {
    // image_url:
    // "http://peiwo-photo.bjcnc.img.sohucs.com/608653738_34f2e603c77643d310497a961f17bd08",
    // thumbnail_url:
    // "http://peiwo-photo.bjcnc.img.sohucs.com/s_thumbnail/608653738_34f2e603c77643d310497a961f17bd08",
    // name: "608653738_34f2e603c77643d310497a961f17bd08"
    // },
    // {
    // image_url:
    // "http://peiwo-photo.bjcnc.img.sohucs.com/608653738_368a8816ac6519a91c4c954d5faaf169",
    // thumbnail_url:
    // "http://peiwo-photo.bjcnc.img.sohucs.com/s_thumbnail/608653738_368a8816ac6519a91c4c954d5faaf169",
    // name: "608653738_368a8816ac6519a91c4c954d5faaf169"
    // }
    // ],
    // gender: 1
    /**
     * 只保留可以更改的字段
     */
    public int uid;
    public String tags;
    public String birthday;
    public String avatar_thumbnail;
    public String profession;
    //public int state;
    public String avatar;
    public String city;
    public int emotion;
    public String slogan;
    public String name;
    public String province;
    public int gender;
    
    public String food_tags;
    public String music_tags;
    public String movie_tags;
    public String book_tags;
    public String travel_tags;
    public String sport_tags;
    public String game_tags;
    
    
//    public List<ImageModel> images = new ArrayList<ImageModel>();
    public ArrayList<ImageModel> images = new ArrayList<ImageModel>();
    //public List<ImagesModel> imagesForDisplay = new ArrayList<ProfileForUpdateModel.ImagesModel>();

    public ProfileForUpdateModel(PWUserModel user) {
        this.uid = user.uid;
        tags = user.tags;
        birthday = user.birthday;
        avatar_thumbnail = user.avatar_thumbnail;
        profession = user.profession;
        //state = user.state;
        avatar = user.avatar;
        city = user.city;
        emotion = user.emotion;
        slogan = user.slogan;
        name = user.name;
        province = user.province;
        gender = user.gender;
        
        food_tags = user.food_tags;
        music_tags = user.music_tags;
        movie_tags = user.movie_tags;
        book_tags = user.book_tags;
        travel_tags = user.travel_tags;
        sport_tags = user.sport_tags;
        game_tags = user.game_tags;
        
        if (user.images != null) {
            this.images.addAll(user.images);
        }
    }

    public String getImages() {
        StringBuilder sb = new StringBuilder();
        for (ImageModel im : images) {
            sb.append(im.name).append(",");
        }
        int index = sb.lastIndexOf(",");
        if (index >= 0) {
        	return sb.substring(0, index);
        }
        return "";
    }
}

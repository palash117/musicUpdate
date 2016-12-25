package dto;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by psd on 18/12/16.
 */

public class MusicListResponseList {
    ArrayList<MusicListResponse> data;

    public ArrayList<MusicListResponse> getData() {
        return data;
    }

    public void setData(ArrayList<MusicListResponse> data) {
        this.data = data;
    }

    public MusicListResponseList(ArrayList<MusicListResponse> data) {
        super();
        this.data = data;
    }

    public MusicListResponseList() {
        super();
        data = new ArrayList<>();
    }

}

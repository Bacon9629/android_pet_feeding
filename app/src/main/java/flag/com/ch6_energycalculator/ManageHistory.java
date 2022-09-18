package flag.com.ch6_energycalculator;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import androidx.annotation.NonNull;

public class ManageHistory {

    DatabaseReference db = FirebaseDatabase.getInstance().getReference("history");
    private Context context;

    public ManageHistory(Context context){
        this.context = context;
    }

    public interface DownloadDate{
        void Success(ArrayList<String> dates);
        void Failed();
    }

    public interface DownloadDetail{
        void Success(ArrayList<HashMap<String, String>> details);
        void Failed();
    }

    public void download_date(ManageAnimal.AnimalData data, DownloadDate downloadDate){
        // do download something

        // 下載那隻動物的紀錄，只下載日期的部分

        db.child(data.getName()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                ArrayList<String> result = new ArrayList<>();
                for (DataSnapshot d1 : snapshot.getChildren()){
                    result.add(d1.getKey());
                }
                Collections.reverse(result);
                downloadDate.Success(result);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "下載歷史紀錄失敗：" + data.getName(), Toast.LENGTH_SHORT).show();
//            download_date(context, data, downloadDate);
            downloadDate.Failed();
        });

    }

    public void download_theDate_detail(ManageAnimal.AnimalData data, String date, DownloadDetail downloadDetail){
        // do download something

        // 下載那隻動物、的某一天的紀錄，只下載那一天的詳細狀況

        db.child(data.getName()).child(date.replace("/", ":")).get()
                .addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                    @Override
                    public void onSuccess(DataSnapshot snapshot) {
                        ArrayList<HashMap<String, String>> a = new ArrayList<>();
                        HashMap<String, String> b;

                        for (DataSnapshot d1 : snapshot.getChildren()){
                            if (Objects.equals(d1.getKey(), "-date"))
                                continue;

                            b = new HashMap<>();
                            b.put("time", d1.getKey());

                            String[] weight_temp = d1.getValue().toString().split(",");
                            b.put("animal_weight", weight_temp[0]);
                            b.put("food_weight", weight_temp[1]);

                            a.add(b);

                        }

                        downloadDetail.Success(a);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "下載歷史紀錄失敗：" + date, Toast.LENGTH_SHORT).show();
//            download_date(context, data, downloadDate);
                downloadDetail.Failed();
            }
        });

    }

    public void change_history_name(String oldName, String newName){
        // 若寵物改名，需要移植history資料島新名字用的函式

        // 若是沒改名則跳過
        if (oldName.equals("") || oldName.equals(newName)){
            return;
        }

        db.child(oldName).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                if (snapshot.getValue() == null){
                    return;
                }
                HashMap<String, Object> result = new HashMap<>();

                for (DataSnapshot d1 : snapshot.getChildren()){
                    result.put(d1.getKey(), d1.getValue());
                }

                db.child(oldName).removeValue();
                db.child(newName).setValue(result);
            }
        });

    }

    public void del_history(String del_name){
        db.child(del_name).removeValue();
    }

    public void upload_history(String name, double[] animal_food_weight){
        /*
        * double[] animal_food_weight : 體重, 食物重量
        * */
        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.HOUR, 8); // +8小時符合時區

        DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd,HH:mm", Locale.TAIWAN);
        String[] date_time = dateFormat.format(calendar.getTime()).split(",");

        HashMap<String, Object> map = new HashMap<>();
        map.put(date_time[1], animal_food_weight[0] + "," + animal_food_weight[1]);
        db.child(name).child(date_time[0]).updateChildren(map);
    }

}

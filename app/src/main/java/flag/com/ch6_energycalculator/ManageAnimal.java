package flag.com.ch6_energycalculator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import androidx.annotation.NonNull;

public class ManageAnimal {
    private static final ArrayList<AnimalData> animalDatas = new ArrayList<>();

    private DatabaseReference db;
    private byte error_count;
    private Context context;


    public ManageAnimal(Context context){
        this.context = context;
        db = FirebaseDatabase.getInstance().getReference("animal");
        error_count = 0;
    }

    public interface DownloadAnimalData{
        void Success();
    }

    public interface ListenerAnimalData{
        void Success();
    }

    public void download_animalData(DownloadAnimalData downloadAnimalData){
        db.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                animalDatas.clear();
                for (DataSnapshot d1 : snapshot.getChildren()) {
//                    Log.d("TAG", d1.getKey());
                    HashMap<String , Object > temp = new HashMap<>();
                    for (DataSnapshot d2 : d1.getChildren()){
//                        Log.d("TAG", d2.getKey() + " " + d2.getValue());
                        temp.put(d2.getKey(), d2.getValue());
                    }
                    temp.put("name", d1.getKey());
                    animalDatas.add(new AnimalData(temp));
                }
                downloadAnimalData.Success();
            }
        });
    }

    public void listener_animalData(ListenerAnimalData updateAnimalData){

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                animalDatas.clear();
                for (DataSnapshot d1 : snapshot.getChildren()) {
//                    Log.d("TAG", d1.getKey());
                    HashMap<String , Object > temp = new HashMap<>();
                    for (DataSnapshot d2 : d1.getChildren()){
//                        Log.d("TAG", d2.getKey() + " " + d2.getValue());
                        temp.put(d2.getKey(), d2.getValue());
                    }
                    temp.put("name", d1.getKey());
                    animalDatas.add(new AnimalData(temp));
                }
                updateAnimalData.Success();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "寵物資料取得失敗，次數 : " + (++error_count), Toast.LENGTH_SHORT).show();
                listener_animalData(updateAnimalData);
            }
        });

    }

    public void delData(String del_name){
        db.child(del_name).setValue(null)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "刪除成功：" + del_name,Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void updateToDatabase(String old_name, AnimalData newData, boolean delete_origin){
        // 可以新增資料，可順便刪掉以前資料，刪掉以前資料的方法是delete_origin == true
        if (delete_origin && !old_name.equals("")){
            // 刪除原本資料的部分
            db.child(old_name).removeValue();
        }

        db.child(newData.getName()).setValue(newData.get_animal_map())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context, "更新資料成功!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "更新資料失敗...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void test_addData(){
        animalDatas.add(new AnimalData("cat", "----", "2001/01/01", 1, "---", "---", true));
//        animalDatas.add(new AnimalData("dog", "name", "2001/06/29", 65, "懶散的dogg", "netee", true));
    }

//    public void downloadToPhone(){
//
//    }

    public static ArrayList<AnimalData> getAnimalDatas() {
        return animalDatas;
    }




    public static class AnimalData {

        private static final String birthday_split_char = "/"; // 生日文字的間隔符號

        private String species = "cat";
        private String name;
        private int[] birthday;
        private String birthday_str; // follow int[] birthday construct and change， change in function(processing_birthday)
        private int age = 20;
        private double weight = 65.1;
        private String state = "NONE";
        private String note = "NONE";
        private boolean ligated = false;


        public AnimalData(String species, String name, String birthday, double weight, String state, String note, boolean ligated) {

            this.species = species;
            this.name = name;
            this.weight = weight;
            this.state = state;
            this.note = note;

            this.birthday = processing_birthday(birthday);
            this.ligated = ligated;
            this.age = processing_age(this.birthday);

        }

        public AnimalData(HashMap<String, Object> map){
            use_Map_input_data(map);
        }

        public HashMap<String, Object> get_animal_map(){
            HashMap<String, Object> map = new HashMap<>();

            map.put("species", species);
//            map.put("name", name);
            map.put("weight", weight);
            map.put("state", state);
            map.put("note", note);
            map.put("birthday", birthday_str.replace("/", ":"));
            map.put("ligated", ligated);

            return map;
        }

        public void use_Map_input_data(HashMap<String, Object> map){
            this.species = (String) map.get("species");
            this.name = (String) map.get("name");
            this.weight =  Double.parseDouble(map.get("weight").toString());
            this.state = (String) map.get("state");
            this.note = (String) map.get("note");

            this.birthday = processing_birthday(((String) map.get("birthday")).replace(":", "/"));
            this.ligated = (boolean) map.get("ligated");
            this.age = processing_age(this.birthday);
        }

        private int processing_age(int[] birthday) {

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            try {
                Date d1 = df.parse(String.format("%04d-%02d-%02d 00:00:00", birthday[0], birthday[1], birthday[2]));
                Date d2 = new Date();
                long diff = d2.getTime() - d1.getTime();

                long age = diff / (1000 * 60 * 60 * 24) / 360;

                return (int) age;

            } catch (ParseException e) {
                e.printStackTrace();
                Log.d("TAG", "age parser failed");
            }

            return 999; // 如果執行到這，表示執行錯誤
        }


        @SuppressLint("DefaultLocale")
        private int[] processing_birthday(String birthday) {
            // 把生日的文字轉成int

            String[] origin = birthday.split(birthday_split_char);
            int[] result = new int[3];

            for (int i = 0; i < 3; i++) {
                result[i] = Integer.parseInt(origin[i]);
            }



            birthday_str = String.format("%04d/%02d/%02d", result[0], result[1], result[2]);

            return result;
        }

        public int[] getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = processing_birthday(birthday);
            this.age = processing_age(this.birthday);
        }

        public String getSpecies() {
            return species;
        }

        public void setSpecies(String species) {
            this.species = species;
        }

        public int getAge() {
            return age;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBirthday_str() {
            return birthday_str;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public boolean isLigated() {
            return ligated;
        }

        public void setLigated(boolean ligated) {
            this.ligated = ligated;
        }
    }
}
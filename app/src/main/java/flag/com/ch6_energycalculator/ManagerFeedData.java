package flag.com.ch6_energycalculator;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import androidx.annotation.NonNull;

public class ManagerFeedData {

    /*
    用來儲存各種動物的餵食量參數
    這裡沒做監聽，因為這種資料是一開始就不變的
     */

    private static final HashMap<String, FoodParams> species_dictionary = new HashMap<>(); // KEY是動物的種類，VALUE是那種動物的參數

    private DatabaseReference db;
    private Context context;
    private byte error_count = 0;

    public ManagerFeedData(Context context) {
        this.context = context;
        db = FirebaseDatabase.getInstance().getReference("species");
    }


    public interface DownloadFeedData{
        void Success();
    }

    public void download_FeedData(DownloadFeedData downloadFeedData){

        db.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                species_dictionary.clear();
                for (DataSnapshot d1 : snapshot.getChildren()){
//                    Log.d("TAG", d1.getKey());
                    HashMap<String, double[]> temp = new HashMap<>();

                    for (DataSnapshot d2 : d1.getChildren()){
                        if (d2.getKey().equals("data")){
                            continue;
                        }

                        String[] split_value = d2.getValue().toString().split(",");
                        double[] result = new double[]{Double.parseDouble(split_value[0]), Double.parseDouble(split_value[1])};
                        temp.put(d2.getKey(), result);

                    }
                    species_dictionary.put(d1.getKey(), new FoodParams(d1.getKey(), temp));
                }
                downloadFeedData.Success();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "食物計算機參數下載錯誤，次數 : " + (++error_count), Toast.LENGTH_SHORT).show();
                download_FeedData(downloadFeedData);
            }
        });


    }


    public static FoodParams getFoodParams(String species){
        return species_dictionary.get(species);
    }

    public static String[] getSpeciesList(){
        return species_dictionary.keySet().toArray(new String[0]);
    }

    public static void update(){
        // 去網路上抓取動物的資料
        species_dictionary.clear();
        HashMap<String, double[]> temp = new HashMap<>();
        temp.put("懶散的小貓貓貓", new double[]{0.1, 1.1});
        temp.put("勤奮的小貓貓", new double[]{1, 2.5});
        species_dictionary.put("cat", new FoodParams("cat", temp));
        HashMap<String, double[]> temp1 = new HashMap<>();
        temp1.put("懶散的小dogg", new double[]{0.1, 1.1});
        temp1.put("勤奮的小dogg", new double[]{1, 2.5});
        species_dictionary.put("dog", new FoodParams("dog", temp1));
    }

    public static int[] calculate(Context context, ManageAnimal.AnimalData data, Double food_per_kilo_kcal){
        // ligated是結紮過，此函式為食量計算機，回傳int{food_kcal, food_weight}

        double a = -1;
        try {
            a = data.isLigated()?species_dictionary.get(data.getSpecies()).params.get(data.getState())[0]:species_dictionary.get(data.getSpecies()).params.get(data.getState())[1];
        }catch (NullPointerException e){
            e.printStackTrace();
            Log.d("TAG", "FeedData.calculate failed: " + data.getSpecies() + " , " + data.getState() + " , " + data.isLigated());
            Toast.makeText(context, data.getSpecies() + " 無 " + data.getState() + " 狀況，請去修正動物列表內之狀況。", Toast.LENGTH_LONG).show();
            return new int[]{-1, -1};
        }

        double RER = 70*Math.pow(data.getWeight(), 0.75);
        double kcal  = RER * a;
        //double rer = Math.round(Math.pow(Double.parseDouble(w), 0.75) * 70);
        int g = (int) Math.round(kcal / food_per_kilo_kcal * 1000);

        return new int[]{(int)kcal, g};
    }

    public static class FoodParams {
        // 這裡用來放一種動物的參數，EX 貓的所有參數
        String species_name;
        private HashMap<String, double[]> params;
        FoodParams(String species_name, HashMap<String, double[]> params){
            this.species_name = species_name;
            this.params = params;
        }

        public double[] getParams(String state){
            return params.get(state);
        }

        public String[] getStateList(){
            return params.keySet().toArray(new String[0]);
        }
    }

}

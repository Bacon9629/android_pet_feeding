package flag.com.ch6_energycalculator.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import flag.com.ch6_energycalculator.ManageAnimal;
import flag.com.ch6_energycalculator.R;
import flag.com.ch6_energycalculator.ManageHistory;

class Adapter_History_date extends RecyclerView.Adapter<Adapter_History_date.myHolder> {

    private Context context;
    private ManageAnimal.AnimalData animalData;
    private ArrayList<String> date;

    public Adapter_History_date(Context context, ManageAnimal.AnimalData animalData, ArrayList<String> date){

        this.context = context;
        this.animalData = animalData;
        this.date = date;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_history_main, parent, false);
        return new myHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
        holder.init_for_parent();
        holder.time.setText(date.get(position).replace(":", "/"));

        holder.arrow.setBackgroundResource(R.drawable.ic_arrow_down);

        holder.touch_sensor.setOnClickListener(view -> {

            if (holder.detail_layout.getVisibility() == View.GONE) {
                // 這裡是點擊第一次，要做開啟詳細資料的動作

                holder.bar.setVisibility(View.VISIBLE);
                holder.arrow.setBackgroundResource(R.drawable.ic_baseline_arrow_drop_up_24);

                holder.detail_layout.setVisibility(View.VISIBLE);
                ManageHistory history = new ManageHistory(context);
                history.download_theDate_detail(animalData, date.get(position), new ManageHistory.DownloadDetail() {
                    @Override
                    public void Success(ArrayList<HashMap<String, String>> details) {
                        Adapter_History_detail detail_adapter = new Adapter_History_detail(context, details);
                        holder.detail_recycler.setLayoutManager(new LinearLayoutManager(context));
                        holder.detail_recycler.setAdapter(detail_adapter);
                        holder.bar.setVisibility(View.GONE);
                    }

                    @Override
                    public void Failed() {
                        Toast.makeText(context, "取得寵物這一天的資料錯誤", Toast.LENGTH_SHORT).show();
                        holder.bar.setVisibility(View.GONE);

                    }
                });
            }else{
                // 這裡是點擊第二次，要關閉詳細資料
                holder.init_for_parent();
                holder.arrow.setBackgroundResource(R.drawable.ic_arrow_down);

            }
        });

    }

    @Override
    public int getItemCount() {
        return date.size();
    }


    static class myHolder extends RecyclerView.ViewHolder{

        TextView time, animal_weight, food_weight, touch_sensor, split_line, arrow;
        LinearLayout weight_layout;
        ConstraintLayout detail_layout;
        RecyclerView detail_recycler;
        ProgressBar bar;

        public myHolder(@NonNull View v) {
            super(v);
            time = v.findViewById(R.id.recycler_alert_history_time);
            animal_weight = v.findViewById(R.id.recycler_alert_history_animal_weight);
            food_weight = v.findViewById(R.id.recycler_alert_history_food_weight);
            weight_layout = v.findViewById(R.id.recycler_alert_history_weight_layout);
            detail_layout = v.findViewById(R.id.recycler_alert_history_detail_layout);
            touch_sensor = v.findViewById(R.id.recycler_alert_history_touchsensor);
            detail_recycler = v.findViewById(R.id.recycler_alert_history_recycler);
            bar = v.findViewById(R.id.recycler_alert_history_progressbar);
            split_line = v.findViewById(R.id.recycler_alert_history_splite_line);
            arrow = v.findViewById(R.id.recycler_alert_history_arrow);
        }

        public void init_for_parent(){
            weight_layout.setVisibility(View.GONE);
            detail_layout.setVisibility(View.GONE);
            bar.setVisibility(View.VISIBLE);
            split_line.setVisibility(View.GONE);
            arrow.setVisibility(View.VISIBLE);
        }

        public void init_for_son(){
            weight_layout.setVisibility(View.VISIBLE);
            detail_layout.setVisibility(View.GONE);
            bar.setVisibility(View.VISIBLE);
            split_line.setVisibility(View.VISIBLE);
            arrow.setVisibility(View.GONE);
        }
    }

    static class Adapter_History_detail extends RecyclerView.Adapter<Adapter_History_date.myHolder>{
        Context context;
        ArrayList<HashMap<String, String>> datas;
        Adapter_History_detail(Context context, ArrayList<HashMap<String, String>> datas){

            HashMap<String, String> temp = new HashMap<>();
            temp.put("time", "時間");
            temp.put("animal_weight", "體重(kg)");
            temp.put("food_weight", "食物(g)");

            this.context = context;
            this.datas = datas;

            datas.add(0, temp);
        }

        @NonNull
        @Override
        public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(context).inflate(R.layout.recycler_history_main, parent, false);
            return new myHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull myHolder holder, int position) {
            holder.init_for_son();

            HashMap<String, String> data = datas.get(position);

            holder.time.setText(data.get("time"));
            holder.animal_weight.setText(data.get("animal_weight"));
            holder.food_weight.setText(data.get("food_weight"));

        }

        @Override
        public int getItemCount() {
            return datas.size();
        }
    }
}

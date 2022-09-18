package flag.com.ch6_energycalculator.ui.machine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import flag.com.ch6_energycalculator.R;

public class Adapter_Machine_data extends RecyclerView.Adapter<Adapter_Machine_data.MyHolder> {


    private Context context;
    private HashMap<String, Object> datas;
    private List<String> keys;


    public Adapter_Machine_data(Context context){
        this.context = context;
        datas = new HashMap<>();
        keys = new ArrayList<>();
    }

    public void update(HashMap<String, Object> datas){
        this.datas = datas;
//        keys = Arrays.asList(datas.keySet().toArray(new String[0]));
        arrange_datas();
        this.notifyDataSetChanged();
    }

    private void arrange_datas(){
        keys = new ArrayList<>();
        keys.add("machine_is_doing");
        keys.add("bowl_max_weight");
        keys.add("bowl_now_weight");
        keys.add("bowl_target_weight");

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_machine_datas, parent, false);
        return new MyHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String key = keys.get(position);
        String title = "";
        String content = "";
        boolean pass_flag = false;

        switch (key){
            case "bowl_max_weight": {
                title = "最大重量 : ";
                content = datas.get(key) + " 公克";
                break;
            }
            case "bowl_now_weight": {
                title = "目前重量 : ";
                content = datas.get(key) + " 公克";
                break;
            }
            case "bowl_target_weight":{
                title = "目標重量 : ";
                content = datas.get(key) + " 公克";
                break;
            }
            case "machine_is_doing":{
                if ((boolean) datas.get("machine_call_do")){
                    title = "呼叫機器投食中";
                }else
                    title = (boolean) datas.get(key)? "食物裝填中" : "機器待機中";

                content = "";
                break;
            }
            default:{
                pass_flag = true;
                break;
            }
        }

        if (pass_flag) {
            holder.layout.setVisibility(View.GONE);
        }else{
            holder.layout.setVisibility(View.VISIBLE);
            holder.title.setText(title);
            holder.content.setText(content);
        }
    }

    @Override
    public int getItemCount() {
        return keys.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder{
        TextView title, content;
        LinearLayout layout;
        public MyHolder(@NonNull View v) {
            super(v);
            title = v.findViewById(R.id.recycler_machine_datas_title);
            content = v.findViewById(R.id.recycler_machine_datas_content);
            layout = v.findViewById(R.id.recycler_machine_datas_layout);
        }
    }
}

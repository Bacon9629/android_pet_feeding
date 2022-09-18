package flag.com.ch6_energycalculator.ui.machine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import flag.com.ch6_energycalculator.ManageAnimal;
import flag.com.ch6_energycalculator.R;

public class Adapter_Machine_NameList extends RecyclerView.Adapter<Adapter_Machine_NameList.myHolder> {

    static private int now_chose_pos = -1;

    private Context context;
    private Runnable auto_fill_target_food_weight_content_runnable;
    private ToggleButton last_chose_bt = null;

    public Adapter_Machine_NameList(Context context, int now_chose_pos, Runnable auto_fill_target_food_weight_content_runnable){
        this.context = context;
        this.now_chose_pos = now_chose_pos;
        this.auto_fill_target_food_weight_content_runnable = auto_fill_target_food_weight_content_runnable;
        new ManageAnimal(context).download_animalData(this::notifyDataSetChanged);
    }

    static public int getNowChosePos(){
        // 若是回傳""，則尚未選取
        return now_chose_pos;
    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_machine_animal_name, parent, false);
        return new myHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {
//        ManageAnimal.AnimalData data = ManageAnimal.getAnimalDatas().get(position);
        String name = ManageAnimal.getAnimalDatas().get(position).getName();
        holder.bt.setText(name);
        holder.bt.setTextOn(name);
        holder.bt.setTextOff(name);

        holder.bt.setOnCheckedChangeListener(null);
        if (now_chose_pos == position){
            holder.bt.setChecked(true);
            last_chose_bt = holder.bt;
        }
        holder.bt.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked){
                if (last_chose_bt != null)
                    last_chose_bt.setChecked(false);

                now_chose_pos = holder.getAdapterPosition();
                last_chose_bt = (ToggleButton) buttonView;
                auto_fill_target_food_weight_content_runnable.run();

            }else{
                now_chose_pos = -1;
                last_chose_bt = null;
            }
        });

    }

    @Override
    public int getItemCount() {
        return ManageAnimal.getAnimalDatas().size();
    }

    static class myHolder extends RecyclerView.ViewHolder{
        ToggleButton bt;
        public myHolder(@NonNull View v) {
            super(v);

            bt = v.findViewById(R.id.recycler_machine_nameList_bt);

        }
    }
}

package flag.com.ch6_energycalculator.ui.home;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import flag.com.ch6_energycalculator.ManageAnimal;
import flag.com.ch6_energycalculator.ManagerFeedData;
import flag.com.ch6_energycalculator.R;
import flag.com.ch6_energycalculator.ManageHistory;
import flag.com.ch6_energycalculator.ui.machine.MachineFragment;

public class Adapter_Home_Main extends RecyclerView.Adapter<Adapter_Home_Main.myHolder> {

    private final Context context;
    private final ArrayList<ManageAnimal.AnimalData> datas;
    private ArrayList<Integer> insert_item_position;

    public Adapter_Home_Main(Context context){

        this.context = context;
        this.datas = ManageAnimal.getAnimalDatas();
        this.insert_item_position = new ArrayList<>();

    }

    @NonNull
    @Override
    public myHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.recycler_family_main, parent, false);
        return new Adapter_Home_Main.myHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull myHolder holder, int position) {

        ManageAnimal.AnimalData data;
        holder.callLayoutVisible(position == datas.size());
        if (position == datas.size()){
            // 在這裡實作"新增"的功能
            holder.callAdd.setOnClickListener(view -> {
                datas.add(new ManageAnimal.AnimalData("cat", "", "2000/01/01", 10, "", "", false));
                this.notifyItemInserted(position+1);
                this.notifyItemChanged(position);
                insert_item_position.add(position);
            });
            return;
        }

        data = datas.get(position);
        holder.callModifyViewVisible(false);


        holder.name.setText(data.getName());
        holder.birthday.setText(data.getBirthday_str());
        holder.age.setText(MessageFormat.format("{0}歲", data.getAge()));
        holder.weight.setText(MessageFormat.format("{0}kg", data.getWeight()));
        holder.state.setText(data.getState());
        holder.note.setText(data.getNote());
        holder.ligated.setText(data.isLigated() ? "已結紮":"未結紮");

        holder.callFeed.setOnClickListener(view -> {
            // 彈出視窗，內容是確認動物活動狀況和體重、輸入食物熱量、動態自動換算食物重量
            callFeed_showAlertDialog(data);

//            double food_weight = ManagerFeedData.calculate(context, data.getSpecies(), data.getWeight(), data.getState(), data.isLigated());

//            new ManageHistory(context).upload_history(data.getName(), new double[]{data.getWeight(), food_weight});

//            Log.d("TAG", "food weight: " + food_weight);

        });

        holder.callHistory.setOnClickListener(view -> {
            // 顯示餵食歷史
            show_history(data);
        });

        // 開始資料更改按鈕的監聽
        View.OnClickListener modifyBt_CallStartModify;

        // 更改資料完畢按鈕監聽
        View.OnClickListener modifyBt_CallEndModify = view -> {

            ArrayList<String> nameList = new ArrayList<>();
            String newName = holder.name_et.getEditableText().toString(); // 新命名的名字

            for (ManageAnimal.AnimalData d : datas){
                nameList.add(d.getName());
            }

            // 確認名子是否重複、適全部資料檢查，因此若名子沒被更改，這裡會顯示true，下一步要確認position是否跟陣列位置一樣
            boolean name_repeat_check1 = nameList.contains(newName);

            if (name_repeat_check1){
                if (nameList.indexOf(newName) != position){
                    Toast.makeText(context, "名子重複，請重新選擇其他名子！！", Toast.LENGTH_LONG).show();
                    return;
                }
                if (newName.equals("") || newName.equals(" ")){
                    Toast.makeText(context, "名子欄位空白，請重新選擇其他名子！！", Toast.LENGTH_LONG).show();
                    return;
                }
            }

            if (holder.state_spinner.getSelectedItem() == null){
                Toast.makeText(context, "請選擇活動狀況", Toast.LENGTH_SHORT).show();
                return;
            }

            holder.callModifyViewVisible(false);

            String old_name = data.getName();

            data.setSpecies(holder.species_spinner.getSelectedItem().toString());
            data.setName(holder.name_et.getEditableText().toString());
            data.setBirthday(holder.birthday_et.getEditableText().toString());
            data.setWeight(Double.parseDouble(holder.weight_et.getEditableText().toString()));
            data.setState(holder.state_spinner.getSelectedItem().toString());
            data.setNote(holder.note_et.getEditableText().toString());
            data.setLigated(holder.ligated_checkbox.isChecked());

            // 移動history資料
            new ManageHistory(context).change_history_name(old_name, data.getName());

            // 若是名子重複，就是沒改名，=> delete_origin = false。if old_name is null，third param type false;
            new ManageAnimal(context).updateToDatabase(old_name, data, !name_repeat_check1);

            // 由資料庫控制datas，因此這裡不需更新datas


            // 在這裡必須把監聽設回偵測開始修改資料，因此執行下列動作
            this.notifyItemChanged(position);
        };


        // 開始資料更改按鈕的監聽
        modifyBt_CallStartModify = view -> {

            view.setOnClickListener(modifyBt_CallEndModify);
            holder.callModifyViewVisible(true);

//            holder.name_et.setEnabled(false);
            holder.name_et.setText(data.getName());
            holder.birthday_et.setText(holder.birthday.getText());
            holder.birthday_et.setInputType(InputType.TYPE_NULL);
            holder.weight_et.setText(String.format("%s", data.getWeight()));
            holder.note_et.setText(data.getNote());
            holder.ligated_checkbox.setChecked(data.isLigated());

            //點擊跳出日期選擇器
            holder.birthday_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (!b) return;
                    DatePickerDialog dialog = new DatePickerDialog(context);
                    dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                            holder.birthday_et.setText(String.format("%4d/%02d/%02d", i, i1+1, i2));
                        }
                    });
                    dialog.show();
                }
            });
            holder.birthday_et.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(View view) {
                    Calendar calendar = Calendar.getInstance();
                    DatePickerDialog dialog = new DatePickerDialog(context);
                    dialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                            holder.birthday_et.setText(String.format("%4d/%02d/%02d", i, i1+1, i2));
                        }
                    });
                    dialog.show();
                }
            });


//          spinner
            MySpinnerAdapter spinnerAdapter = new MySpinnerAdapter(context, android.R.layout.simple_spinner_item, ManagerFeedData.getSpeciesList());
            holder.species_spinner.setAdapter(spinnerAdapter);

            ManagerFeedData.FoodParams feedData = ManagerFeedData.getFoodParams(data.getSpecies());  // 此species的活動狀況列表與對應之餵食資料參數
            holder.state_spinner.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, feedData.getStateList()));

            try {
                holder.state_spinner.setSelection(Arrays.asList(feedData.getStateList()).indexOf(data.getState()));
                holder.species_spinner.setSelection(Arrays.asList(ManagerFeedData.getSpeciesList()).indexOf(data.getSpecies()));
            }catch (Exception e){
                e.printStackTrace();
//                Log.d("TAG", "no this state : " + data.getState());
//                Log.d("TAG", "no this species : " + data.getSpecies());
                holder.state_spinner.setSelection(0);
                holder.species_spinner.setSelection(0);
            }

            holder.species_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    // 更改成新的species，必須重新去ManagerFeedData得到新選取的species的state list
                    String newSpecies = ManagerFeedData.getSpeciesList()[i];
                    holder.state_spinner
                            .setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item,
                                    ManagerFeedData.getFoodParams(newSpecies)
                                            .getStateList()));
                    try {
                        // 這裡必須要重新去ManagerFeedData取得新的spinner list，因為species被改過了，舊的不能用
                        holder.state_spinner.setSelection(Arrays.asList(ManagerFeedData.getFoodParams(newSpecies).getStateList()).indexOf(data.getState()));
                    }catch (Exception e){
                        e.printStackTrace();
                        Log.d("TAG", "no this state : " + data.getState());
                        holder.state_spinner.setSelection(0);
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        };

        // 刪除資料
        holder.callDel.setOnLongClickListener(view -> {
            // 若是資料新增後沒有被更新，名子就會是空的，那就只需要刪除本地端
            // 刪除線上端會自動更新本地端，但若沒線上端可刪，就不會更新
            if (data.getName().equals("")){
                datas.remove(data);
                this.notifyDataSetChanged();
            }else {
                new ManageAnimal(context).delData(data.getName());
                new ManageHistory(context).del_history(data.getName());
            }
            return false;
        });

        holder.callModify.setOnClickListener(modifyBt_CallStartModify);


        // 若是資料新增後沒有被更新，就會呼叫這一段
        if (data.getName().equals("")){
            holder.callModify.callOnClick();
        }

    }

    private void callFeed_showAlertDialog(ManageAnimal.AnimalData data) {
        // 彈出視窗，內容是確認動物活動狀況和體重、輸入食物熱量、動態自動換算食物重量

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View v = LayoutInflater.from(context).inflate(R.layout.alert_give_food, null, false);
        SharedPreferences preferences = context.getSharedPreferences(data.getName(), 0);
        int[] food_weight = {0};

        TextView content = v.findViewById(R.id.alert_give_food_content);
        TextView food_result = v.findViewById(R.id.alert_give_food_food_weight);
        EditText edit = v.findViewById(R.id.alert_give_food_edittext);
        Button check = v.findViewById(R.id.alert_give_food_feed_bt);
        Button cancel = v.findViewById(R.id.alert_give_food_cancel_bt);

        String content_str = data.getName() + " 是個 " + data.getState() + "\n" +
                            (data.isLigated() ? "已結紮" : "未結紮") + "\n" +
                            "體重 " + data.getWeight() + " kg\n\n" +
                            "(若以上資料有誤、請返回更改)";
        content.setText(content_str);



        // 輸入熱量後自動更新公克數
        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try{
                    int[] kcal_weight = ManagerFeedData.calculate(context, data, Double.parseDouble(charSequence.toString()));
                    StringBuilder temp = new StringBuilder();
                    temp.append("應該要吃：\n").append(kcal_weight[0]).append(" kcal\n").append(kcal_weight[1]).append(" g");
                    food_weight[0] = kcal_weight[1];
                    food_result.setText(temp);
                }catch (Exception ignored){}
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        });

        edit.setText(preferences.getString("memory_kcal", ""));

        AlertDialog dialog = builder.setView(v).show();

        // 開始投食
        check.setOnClickListener(view -> {
            preferences.edit().putString("memory_kcal", edit.getEditableText().toString()).apply();
            preferences.edit().putInt("memory_weight", food_weight[0]).apply();
            MachineFragment.auto_feed_animal_pos = ManageAnimal.getAnimalDatas().indexOf(data);
            dialog.cancel();

            call_change_to_machine_page();

        });

        // 取消
        cancel.setOnClickListener(view -> {
            dialog.cancel();
        });

    }

    private void call_change_to_machine_page() {
        Intent intent = new Intent();
        intent.setAction("change_to_machine_page");
        context.sendBroadcast(intent);
    }

    private void show_history(ManageAnimal.AnimalData data){

        View v = LayoutInflater.from(context).inflate(R.layout.alert_history, null, false);
        TextView name = v.findViewById(R.id.alert_history_name_tv);
        RecyclerView recycler = v.findViewById(R.id.alert_history_recycler_date);
        Button cancel_bt = v.findViewById(R.id.alert_history_canel_bt);
        ProgressBar bar = v.findViewById(R.id.alert_history_progress);

        bar.setVisibility(View.VISIBLE);

        name.setText(data.getName());

        ManageHistory manageHistory = new ManageHistory(context);
        manageHistory.download_date(data, new ManageHistory.DownloadDate() {
            @Override
            public void Success(ArrayList<String> dates) {
                Adapter_History_date adapter_history_date = new Adapter_History_date(context, data, dates);
                recycler.setLayoutManager(new LinearLayoutManager(context));
                recycler.setAdapter(adapter_history_date);
                bar.setVisibility(View.GONE);
            }

            @Override
            public void Failed() {

                Toast.makeText(context, "取得日期資料錯誤", Toast.LENGTH_SHORT).show();
                bar.setVisibility(View.GONE);

            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(v);
        AlertDialog dialog = builder.show();
        cancel_bt.setOnClickListener(view -> dialog.cancel());


    }


    @Override
    public int getItemCount() {
        return datas.size() + 1;
//        return 3;
    }

    static class myHolder extends RecyclerView.ViewHolder{

        Button callFeed, callModify, callHistory;
        TextView edge, name, birthday, age, weight, state, note, ligated;
        ImageView photo;
        EditText name_et, birthday_et, weight_et, note_et;
        Spinner state_spinner, species_spinner;
        ConstraintLayout dataLayout;
        Button callAdd, callDel;
        CheckBox ligated_checkbox;

        Button[] bt_list;
        View[] when_modify_GONE_view;
        View[] modify_list;

        public myHolder(@NonNull View v) {
            super(v);

            callFeed = v.findViewById(R.id.recycler_family_main_feed_bt);
            callModify = v.findViewById(R.id.recycler_family_main_modifydata_bt);
            callHistory = v.findViewById(R.id.recycler_family_main_seeHistory_bt);

            edge = v.findViewById(R.id.recycler_family_main_edge);
            name = v.findViewById(R.id.recycler_family_main_name_tv);
            birthday = v.findViewById(R.id.recycler_family_main_bithday_tv);
            age = v.findViewById(R.id.recycler_family_main_age_tv);
            weight = v.findViewById(R.id.recycler_family_main_weight_tv);
            state = v.findViewById(R.id.recycler_family_main_stage_tv);
            note = v.findViewById(R.id.recycler_family_main_note_tv);
            ligated = v.findViewById(R.id.recycler_family_main_ligated_tv);

            photo = v.findViewById(R.id.recycler_family_main_photo);

            name_et = v.findViewById(R.id.recycler_family_main_name_et);
            birthday_et = v.findViewById(R.id.recycler_family_main_birthday_et);
            weight_et = v.findViewById(R.id.recycler_family_main_weight_et);
            note_et = v.findViewById(R.id.recycler_family_main_note_et);
            ligated_checkbox = v.findViewById(R.id.recycler_family_main_ligated_checkbox);
            state_spinner = v.findViewById(R.id.recycler_family_main_stage_spinner);
            species_spinner = v.findViewById(R.id.recycler_family_main_species_spinner);

            dataLayout = v.findViewById(R.id.recycler_family_main_dataLayout_constrain);

            callAdd = v.findViewById(R.id.recycler_family_main_add_bt);
            callDel = v.findViewById(R.id.recycler_family_main_del_bt);

            bt_list = new Button[]{callAdd, callHistory, callModify, callFeed};
            when_modify_GONE_view = new TextView[]{name, birthday, age, weight, state, note, ligated, callFeed, callHistory};
            modify_list = new View[]{name_et, birthday_et, weight_et, state_spinner, note_et, ligated_checkbox, species_spinner, callDel};

            // this list is not content name_et
//            modify_list = new View[]{birthday_et, weight_et, state_spinner, note_et, ligated_checkbox, species_spinner, callDel};

        }

        public void callLayoutVisible(boolean last_one){
            // 初始化物件
            callDel.setVisibility(View.GONE);
            if (last_one){
                // 這裡要放新增項目的layout
                callAdd.setVisibility(View.VISIBLE);
                dataLayout.setVisibility(View.GONE);
            }else{
                // 這裡放顯示資料的layout
                callAdd.setVisibility(View.GONE);
                dataLayout.setVisibility(View.VISIBLE);
            }
            for(Button b : bt_list){
                b.setOnClickListener(null);
            }
        }

        public void callModifyViewVisible(boolean startModify){
            // 設定更改資料與顯示資料的切換顯示函式
            if (startModify){
                for(View v : when_modify_GONE_view) v.setVisibility(View.GONE);
                for(View v : modify_list) v.setVisibility(View.VISIBLE);
                callModify.setText("更新完成");
//                callModify.setTextColor(0xFF5722);
            }else{
                birthday_et.setOnFocusChangeListener(null);
                birthday_et.setOnClickListener(null);
                for(View v : when_modify_GONE_view) v.setVisibility(View.VISIBLE);
                for(View v : modify_list) v.setVisibility(View.GONE);
                callModify.setText("修改資料");
//                callModify.setTextColor(0x6C973A);

            }

        }

    }

    static class MySpinnerAdapter extends ArrayAdapter<String>{


        public MySpinnerAdapter(@NonNull Context context, int resource, String[] textViewResource) {
            super(context, resource, textViewResource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View view, @NonNull ViewGroup parent) {

            if (view != null) {
                view.setPadding(0, 10, 0, 4);
            }

            return super.getView(position, view, parent);
        }


    }

}

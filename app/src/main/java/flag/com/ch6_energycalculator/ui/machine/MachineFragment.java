package flag.com.ch6_energycalculator.ui.machine;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import flag.com.ch6_energycalculator.FeedService;
import flag.com.ch6_energycalculator.ManageAnimal;
import flag.com.ch6_energycalculator.ManageHistory;
import flag.com.ch6_energycalculator.ManageMachine;
import flag.com.ch6_energycalculator.R;

public class MachineFragment extends Fragment {

    static public int auto_feed_animal_pos = -1; // 從home選擇寵物進行餵食，非-1則進行自動餵食

    private Context context;
    private Timer refresh_timer_edit_content_timer;

    private EditText[] timer_time_edit;
    private Button[] timer_control_bt;
    private ImageButton food_weight_mask;
    private EditText target_feed_weight_edit;

    private ProgressDialog progressDialog;
    private ManageMachine manageMachine;
    
    private int timer_stop_flag = 0; // 0:繼續、1:暫停、2:停止

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_machine, container, false);
        context = getActivity();

        progressDialog = new ProgressDialog(context);
        progressDialog.show();

        manageMachine = new ManageMachine();
        
        ImageView pointer = v.findViewById(R.id.recycler_home_barPointer);
        TextView bar = v.findViewById(R.id.recycler_home_food_weight_bar);
        ConstraintLayout.LayoutParams pointer_params = (ConstraintLayout.LayoutParams) pointer.getLayoutParams();
        TextView weight_percent_tv = v.findViewById(R.id.recycler_machine_food_weight_percent_tv);
        Button feed_button = v.findViewById(R.id.machine_feed_button);
        target_feed_weight_edit = v.findViewById(R.id.recycler_machine_food_weight_edit);

        food_weight_mask = v.findViewById(R.id.machine_feed_button_maskView);
        food_weight_mask.setVisibility(View.GONE);

        timer_time_edit = new EditText[]{
                v.findViewById(R.id.recycler_machine_timer_min_edit),
                v.findViewById(R.id.recycler_machine_timer_sec_edit)};
        timer_control_bt = new Button[]{
                v.findViewById(R.id.recycler_machine_timer_play_pause_bt),
                v.findViewById(R.id.recycler_machine_timer_stop_bt)
        };

        Button[] give_food_weight_button = new Button[]{
                v.findViewById(R.id.recycler_machine_food_weight_addAll_bt),
                v.findViewById(R.id.recycler_machine_food_weight_addHalf_bt),
                v.findViewById(R.id.recycler_machine_food_weight_addLittle_bt)
        };
        give_food_weight_button[0].setTag("all");
        give_food_weight_button[1].setTag("half");
        give_food_weight_button[2].setTag("little");


        // name list recycler
        RecyclerView name_recycler = v.findViewById(R.id.recycler_machine_name_recyclerView);
        Adapter_Machine_NameList adapter_name = null; // 第二個參數若不為-1，則自動執行餵食
        adapter_name = new Adapter_Machine_NameList(context, auto_feed_animal_pos, () -> refresh_target_feed_weight_edit_content(Adapter_Machine_NameList.getNowChosePos()));
        name_recycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        name_recycler.setAdapter(adapter_name);


        // data recycler
        RecyclerView data_recycler = v.findViewById(R.id.machine_data_recycler);
        Adapter_Machine_data adapter_machine_data = new Adapter_Machine_data(context);
//        data_recycler.setLayoutManager(new GridLayoutManager(context, 2));
        data_recycler.setLayoutManager(new LinearLayoutManager(context));
        data_recycler.setAdapter(adapter_machine_data);


        // machine data 監聽
        manageMachine.getMachineDataListener("id", new ManageMachine.OnGetMachineDataListener() {
            @Override
            public void Success(HashMap<String, Object> datas) {

                if (progressDialog.isShowing()) progressDialog.dismiss();

                int bar_width = bar.getMeasuredWidth();
                float percent = (Float.parseFloat(datas.get("bowl_now_weight").toString()) / Float.parseFloat(datas.get("bowl_max_weight").toString()));
                int padding = bar_width - (int) (bar_width * percent);
//                Log.d("TAG", padding+"  "+bar_width);
                pointer_params.setMarginEnd(padding);
                pointer.setLayoutParams(pointer_params);
                weight_percent_tv.setText(MessageFormat.format("{0}%", (int)(percent*100)));

                adapter_machine_data.update(datas);
            }
        });


        // 按下投食按鈕
        feed_button.setOnClickListener(v1 -> {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            
            // 判斷timer是否存在，並把結果存在have_timer_flag裡面
            int timer_min = 0;
            int timer_sec = 0;
            boolean have_timer_flag = !(timer_time_edit[0].getEditableText().toString().equals("") && timer_time_edit[1].getEditableText().toString().equals(""));
            if (have_timer_flag){
                if (!timer_time_edit[0].getEditableText().toString().equals(""))
                    timer_min = Integer.parseInt(timer_time_edit[0].getEditableText().toString());
                if (!timer_time_edit[1].getEditableText().toString().equals(""))
                    timer_sec = Integer.parseInt(timer_time_edit[1].getEditableText().toString());
            }
            
            // 判斷資料是否齊全
            int position = Adapter_Machine_NameList.getNowChosePos();
            if (position == -1){
                Toast.makeText(context, "未選擇寵物", Toast.LENGTH_SHORT).show();
                return;
            }else if (target_feed_weight_edit.getEditableText().toString().equals("")){
                Toast.makeText(context, "未輸入飼料目標重量", Toast.LENGTH_SHORT).show();
                target_feed_weight_edit.requestFocus();
                // 強制顯示鍵盤
                imm.showSoftInput(target_feed_weight_edit,InputMethodManager.SHOW_FORCED);
                return;
            }
            
            // 強制隱藏鍵盤
            imm.hideSoftInputFromWindow(target_feed_weight_edit.getWindowToken(), 0);

            // 用彈出視窗來確認細節
            AlertDialog.Builder detailCheck_alertBuilder = new AlertDialog.Builder(context);
            ManageAnimal.AnimalData animalData = ManageAnimal.getAnimalDatas().get(position);

            
            double[] animal_food_weight = new double[]{animalData.getWeight(), Double.parseDouble(target_feed_weight_edit.getEditableText().toString())};

            detailCheck_alertBuilder.setTitle("給 " + animalData.getName() + " :");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("目前體重　： ").append(animal_food_weight[0]).append(" kg\n");
            stringBuilder.append("目標食物量： ").append(animal_food_weight[1]).append(" g\n\n");

            if (have_timer_flag){
                stringBuilder.append("已設定倒數計時： ").append(timer_min).append("分").append(timer_sec).append("秒\n");
            }else{
                stringBuilder.append("未設定倒數計時\n");
            }

            detailCheck_alertBuilder.setMessage(stringBuilder.toString());
            
            // 彈出視窗的確認與取消按鈕
            detailCheck_alertBuilder.setNegativeButton("取消", null);

            int finalTimer_min = timer_min;
            int finalTimer_sec = timer_sec;
            detailCheck_alertBuilder.setPositiveButton("確認", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    // 初始化停止計時的flag
                    timer_stop_flag = 0;
                    
                    if (have_timer_flag){
//                        delay_call_feed(finalTimer_min, finalTimer_sec, timer_time_edit,
//                                () -> call_feed(Integer.parseInt(target_feed_weight_edit.getEditableText().toString()), animal_food_weight, animalData));

                        start_feed_service(Integer.parseInt(target_feed_weight_edit.getEditableText().toString()),
                                animal_food_weight, animalData);
                    }

                    else
                        call_feed(Integer.parseInt(target_feed_weight_edit.getEditableText().toString()), animal_food_weight, animalData);

                    // 取消關注edittext
                    target_feed_weight_edit.clearFocus();
                }
            });

            detailCheck_alertBuilder.show();
            
        });


        // 按下timer暫停按鈕
        timer_control_bt[0].setOnClickListener(v12 -> {

            if (timer_stop_flag == 0){
                // pause
                FeedService.timer_stop_flag = 2;
                timer_stop_flag = 2;
                timer_control_bt[0].setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
            }else{
                // play
                FeedService.timer_stop_flag = 0;
                timer_stop_flag = 0;
                timer_control_bt[0].setBackgroundResource(R.drawable.ic_baseline_pause_24);
            }

        });

        // 按下timer停止鍵
        timer_control_bt[1].setOnClickListener(v13 -> {
            timer_stop_flag = 1;
            FeedService.timer_stop_flag = 1;
        });


        // 按下投放飼料的快捷鍵：全滿、半碗、一點點
        View.OnClickListener give_food_listener = (v1 -> {
            int total_capacity = Integer.parseInt(ManageMachine.getDatas().get("bowl_max_weight").toString());
            switch (v1.getTag().toString()){
                case "all":
                    target_feed_weight_edit.setText((total_capacity) + "");
                    break;
                case "half":
                    target_feed_weight_edit.setText((int)(total_capacity * 0.5) + "");
                    break;
                case "little":
                    target_feed_weight_edit.setText((int)(total_capacity * 0.3) + "");
                    break;
            }
        });
        for(Button button : give_food_weight_button){
            button.setOnClickListener(give_food_listener);
        }

        // 自動啟動餵食動作，因為auto_feed_animal_pos != -1，表示已經從寵物資料那邊啟動餵食
        if (auto_feed_animal_pos != -1){
            refresh_target_feed_weight_edit_content(auto_feed_animal_pos);
            feed_button.callOnClick();
        }

        refresh_timer_editText_content();

        return v;
    }


    private void start_feed_service(int food_target_weight, double[] animal_food_weight, ManageAnimal.AnimalData animalData){
        int timer_min = 0;
        int timer_sec = 0;
        if (!timer_time_edit[0].getEditableText().toString().equals(""))
            timer_min = Integer.parseInt(timer_time_edit[0].getEditableText().toString());
        if (!timer_time_edit[1].getEditableText().toString().equals(""))
            timer_sec = Integer.parseInt(timer_time_edit[1].getEditableText().toString());

        if (FeedService.animalData != null){
            Toast.makeText(context, "目前已經有寵物在餵食中", Toast.LENGTH_SHORT).show();
            return;
        }

        FeedService.set_parameter(timer_min, timer_sec, 0, food_target_weight, animal_food_weight, animalData);

        Intent intent = new Intent(context, FeedService.class);
        context.startService(intent);

    }

    private void refresh_target_feed_weight_edit_content(ManageAnimal.AnimalData data){
        target_feed_weight_edit.setText(context.getSharedPreferences(data.getName(), 0).getInt("memory_weight", 0) + "");
    }

    private void refresh_target_feed_weight_edit_content(int animal_position){
        refresh_target_feed_weight_edit_content(ManageAnimal.getAnimalDatas().get(animal_position));
    }


    private void call_feed(int food_target_weight, double[] animal_food_weight, ManageAnimal.AnimalData animalData){
        // 開始上傳
        ManageHistory history = new ManageHistory(context);
        history.upload_history(animalData.getName(), animal_food_weight);

        manageMachine.update_target_weight("id", food_target_weight);
        Toast.makeText(context, "成功呼叫，等待機器開始動作！", Toast.LENGTH_SHORT).show();

        context.getSharedPreferences(animalData.getName(), 0).edit().putInt("memory_weight", food_target_weight).apply();


    }

    private void refresh_timer_editText_content(){
        // for 背景執行的

        Handler ui_handler = new Handler();
        Runnable edit_view_runnable = new Runnable() {
            @Override
            public void run() {
                if (FeedService.animalData == null){
                    // 目前沒有執行timer，因此跳過執行
                    food_weight_mask.setVisibility(View.GONE);
                }else{
                    food_weight_mask.setVisibility(View.VISIBLE);
                    if (FeedService.timer_stop_flag == 2){
                        timer_control_bt[0].setBackgroundResource(R.drawable.ic_baseline_pause_24);
                    }else if(FeedService.timer_stop_flag == 0){
                        timer_control_bt[0].setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
                    }

                    String min = FeedService.min+"";
                    String sec = FeedService.sec+"";

                    timer_time_edit[0].setText(min);
                    timer_time_edit[1].setText(sec);
                }
            }
        };

        refresh_timer_edit_content_timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ui_handler.post(edit_view_runnable);
            }
        };
        refresh_timer_edit_content_timer.schedule(timerTask, 0, 1000);

    }

    private void delay_call_feed(int delay_min, int delay_sec, EditText[] timer_edit, Runnable call_feed_runnable){
        // 這個函式是給無法背景執行用的
        food_weight_mask.setVisibility(View.VISIBLE);
        Timer timer = new Timer();
        Handler handler = new Handler();
        Runnable onCancel_runnable = () -> {
            Toast.makeText(context, "倒數計時已停止", Toast.LENGTH_SHORT).show();
            food_weight_mask.setVisibility(View.GONE);
            timer.cancel();
        };

        TimerTask timerTask = new TimerTask() {
            int delay_total_sec = delay_min * 60 + delay_sec;
            @Override
            public void run() {
                switch (timer_stop_flag){
                    case 0:
                        // 繼續執行
                        break;

                    case 1:
                        // 結束timer
                        handler.post(onCancel_runnable);
//                        onCancel_runnable.run();
                        return;

                    case 2:
                        // 暫停timer
                        delay_total_sec+=1;
                        break;
                }

                delay_total_sec-=1;

                int after_delay_min = delay_total_sec / 60;
                int after_delay_sec = delay_total_sec - after_delay_min * 60;

                timer_edit[0].setText(MessageFormat.format("{0}", after_delay_min));
                timer_edit[1].setText(MessageFormat.format("{0}", after_delay_sec));

                if (delay_total_sec < 1){
                    call_feed_runnable.run();
                    food_weight_mask.setVisibility(View.GONE);
                    timer.cancel();
                }
            }
        };

        timer.schedule(timerTask, 1000, 1000);

//        Handler handler = new Handler();
//        Runnable refresh_timer;
//        food_weight_mask.setVisibility(View.VISIBLE);
//
//        refresh_timer = new Runnable() {
//            int delay_total_sec = delay_min * 60 + delay_sec;
//            @Override
//            public void run() {
//                switch (timer_stop_flag){
//                    case 0:
//                        // 繼續執行
//                        break;
//
//                    case 1:
//                        // 結束timer
//                        Toast.makeText(context, "倒數計時已停止", Toast.LENGTH_SHORT).show();
//                        food_weight_mask.setVisibility(View.GONE);
//                        handler.removeCallbacksAndMessages(null);
//                        return;
//
//                    case 2:
//                        // 暫停timer
//                        delay_total_sec+=1;
////                        handler.removeCallbacks(this);
//                        break;
//                }
//
//                delay_total_sec-=1;
//
//                int after_delay_min = delay_total_sec / 60;
//                int after_delay_sec = delay_total_sec - after_delay_min * 60;
//
//                timer_edit[0].setText(MessageFormat.format("{0}", after_delay_min));
//                timer_edit[1].setText(MessageFormat.format("{0}", after_delay_sec));
//
//                if (delay_total_sec >= 1){
//                    handler.removeCallbacksAndMessages(null);
//                    handler.postDelayed(this, 1000);
//                }
//                else {
//                    call_feed_runnable.run();
//                    food_weight_mask.setVisibility(View.GONE);
//                    handler.removeCallbacksAndMessages(null);
//                }
//            }
//        };
//
//        handler.removeCallbacks(refresh_timer);
//        handler.postDelayed(refresh_timer, 1000);
        
    }




    @Override
    public void onStop() {
        super.onStop();
        timer_stop_flag = 1; // 結束timer
        auto_feed_animal_pos = -1; // 從home選擇寵物進行餵食，非-1則進行自動餵食
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (refresh_timer_edit_content_timer != null){
            refresh_timer_edit_content_timer.cancel();
        }
    }
}
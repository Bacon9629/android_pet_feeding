package flag.com.ch6_energycalculator;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class FeedService extends Service {


    public static int min = 0;
    public static int sec = 0;
    public static int timer_stop_flag = 0; // 0:繼續執行 1:結束timer 2:暫停timer
    public static int food_target_weight;
    public static double[] animal_food_weight;
    public static ManageAnimal.AnimalData animalData = null;

    public static void set_parameter(int min, int sec, int timer_stop_flag, int food_target_weight, double[] animal_food_weight, ManageAnimal.AnimalData animalData){
        set_timer(min, sec);
        set_timer_stop_flag(timer_stop_flag);
        FeedService.food_target_weight = food_target_weight;
        FeedService.animal_food_weight = animal_food_weight;
        FeedService.animalData = animalData;
    }

    public static void set_timer(int min, int sec){
        FeedService.min = min;
        FeedService.sec = sec;
    }

    public static void set_timer_stop_flag(int timer_stop_flag){
        FeedService.timer_stop_flag = timer_stop_flag;
    }

    private String CHANNEL_ID = "ID";
    private Context context;
    private Handler done_handler;
    private boolean notification_flag = false;

    private void call_feed(int food_target_weight, double[] animal_food_weight, ManageAnimal.AnimalData animalData){
        // 開始上傳
        ManageHistory history = new ManageHistory(this.getApplicationContext());
        history.upload_history(animalData.getName(), animal_food_weight);

        ManageMachine manageMachine = new ManageMachine();
        manageMachine.update_target_weight("id", food_target_weight);
        Toast.makeText(context, "成功呼叫，等待機器開始動作！", Toast.LENGTH_SHORT).show();

        context.getSharedPreferences(animalData.getName(), 0).edit().putInt("memory_weight", food_target_weight).apply();


    }

    private void construct_notification(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "DemoCode", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder
                = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_cat4)
                .setContentTitle("投食功能")
                .setContentText("您設置的投放已經投放完畢喽~~")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        NotificationManagerCompat notificationManagerCompat
                = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1,builder.build());
    }

    private void set_fill_done_timer(){
        // 開啟偵測是否投食完畢

        Timer timer = new Timer();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
//                Toast.makeText(context, "OKOKOK", Toast.LENGTH_SHORT).show();
                timer.cancel();

                construct_notification();

            }
        };
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                ManageMachine manageMachine = new ManageMachine();
                manageMachine.getMachineDataListener("id", new ManageMachine.OnGetMachineDataListener() {
                    @Override
                    public void Success(HashMap<String, Object> datas) {

                        boolean machine_call_do = (boolean) datas.get("machine_call_do");
                        boolean machine_is_doing = (boolean) datas.get("machine_is_doing");

                        if (!machine_is_doing && !machine_call_do && notification_flag){
                            done_handler.post(runnable);
                            timer.cancel();
                            notification_flag = false;
                        }
                    }
                });
            }
        };

        timer.schedule(timerTask, 5000, 5000);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this.getApplicationContext();
        done_handler = new Handler();
        notification_flag = true;

//        Log.d("TAG", "feed service active");

        if (animalData == null){
            Toast.makeText(context, "animalData 是空的，無法執行計時器", Toast.LENGTH_SHORT).show();
            return super.onStartCommand(intent, flags, startId);
        }

        Timer timer = new Timer();
        Handler handler = new Handler();
        Runnable on_feed_runnable = () -> {
            Toast.makeText(context, "開始餵食", Toast.LENGTH_SHORT).show();
            call_feed(food_target_weight, animal_food_weight, animalData);
            animalData = null;
            timer.cancel();
        };
        Runnable on_cancel_runnable = ()->{
            Toast.makeText(context, "餵食計時器已取消", Toast.LENGTH_SHORT).show();
            animalData = null;
            timer.cancel();
        };
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                int total_time = get_total_time();
                Log.d("TAG", "from service：now time = "+total_time);
                switch (timer_stop_flag){
                    case 0:{
                        //繼續執行
                        break;
                    }
                    case 1:{
                        //結束timer
                        handler.post(on_cancel_runnable);
                        break;
                    }
                    case 2:{
                        // 暫停timer
                        sec += 1;
                        break;
                    }
                }
                if (total_time < 1){
                    // do feed
                    handler.post(on_feed_runnable);
                    set_fill_done_timer();
                }else{
                    if(sec > 0){
                        sec -= 1;
                    }else{
                        sec = 59;
                        min -= 1;
                    }
                }
            }
        };

        timer.schedule(timerTask, 1000, 1000);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int get_total_time(){
        return min*60 + sec;
    }

}

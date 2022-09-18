package flag.com.ch6_energycalculator.ui.home;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import flag.com.ch6_energycalculator.ManageAnimal;
import flag.com.ch6_energycalculator.ManagerFeedData;
import flag.com.ch6_energycalculator.R;

public class HomeFragment extends Fragment {

    private final byte Download_Mission_Amount = 2;  // 初始化任務數量、需要達成這個數字的任務才會把progress dialog關閉
    private byte init_download_flag = 0; // 每下載一樣東西就會+1，若是加到目標數量，就會把progress dialog關閉

    private Context context;

    private ProgressDialog dialog;
    private Adapter_Home_Main adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_family, container, false);

        context = getActivity();

        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        dialog.setMessage("等待資料連線中");
        dialog.show();

        RecyclerView recyclerView = root.findViewById(R.id.fragment_home_recyclerView);
        adapter = new Adapter_Home_Main(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        // 設置animal資料的初始化+監聽(他們是做在一起的)
//        ManageAnimal manageAnimal = new ManageAnimal();
        new ManageAnimal(context).listener_animalData(()->{
            adapter.notifyDataSetChanged();
            check_close_progressDialog();
        });


        // 設置FeedData資料的初始化、沒有監聽，因為這種資料是不變的
//        ManagerFeedData feedData = new ManagerFeedData(context);
        new ManagerFeedData(context).download_FeedData(this::check_close_progressDialog);




//        viewModel.getAnimal().observe(getViewLifecycleOwner(), new Observer<ManageAnimal.AnimalData>() {
//            @Override
//            public void onChanged(ManageAnimal.AnimalData data) {
//                adapter.notifyDataSetChanged();
//            }
//        });


        return root;
    }

    private void check_close_progressDialog(){

        // 這個函式用來控制開啟程式時的progress dialog顯示與否，if 裡面的數字代表下載的任務數量

        init_download_flag += 1;
        if (init_download_flag == Download_Mission_Amount) dialog.dismiss();
    }

}
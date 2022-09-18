package flag.com.ch6_energycalculator;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ManageMachine {

    static private HashMap<String, Object> datas;

    private DatabaseReference db;

    static public HashMap<String, Object> getDatas(){
        return datas.isEmpty() ? new HashMap<String, Object>() : datas;
    }

    public ManageMachine(){

        db = FirebaseDatabase.getInstance().getReference("machine");
    }

    public void update_target_weight(String machine_id, int target_weight){
        HashMap<String, Object> map = new HashMap<>();
        map.put("bowl_target_weight", target_weight);
        map.put("machine_call_do", true);
        db.child(machine_id).updateChildren(map);

    }

    public interface OnGetMachineDataListener{
        void Success(HashMap<String, Object> datas);
    }

    public void getMachineDataListener(String id, OnGetMachineDataListener onGetMachineData){
        db.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                datas = (HashMap<String, Object>)snapshot.getValue();
                onGetMachineData.Success(datas);
//                Log.d("TAG", datas.keySet().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}

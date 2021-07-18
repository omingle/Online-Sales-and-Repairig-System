package com.sdlproject.salesandrepair.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sdlproject.salesandrepair.R;
import com.sdlproject.salesandrepair.activities.RepairDetailsUserActivity;
import com.sdlproject.salesandrepair.models.ModelRepairUser;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterRepairUser extends RecyclerView.Adapter<AdapterRepairUser.HolderRepairUser> {

    private Context context;
    public ArrayList<ModelRepairUser> repairUserArrayList;

    public AdapterRepairUser(Context context, ArrayList<ModelRepairUser> repairUserArrayList) {
        this.context = context;
        this.repairUserArrayList = repairUserArrayList;
    }

    @NonNull
    @Override
    public AdapterRepairUser.HolderRepairUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_repair_user, parent, false);
        return new AdapterRepairUser.HolderRepairUser(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterRepairUser.HolderRepairUser holder, int position) {
        // get data at position
        ModelRepairUser modelRepairUser = repairUserArrayList.get(position);
        final String repairId = modelRepairUser.getRepairId();
        String productId = modelRepairUser.getProductId();
        String repairDescription = modelRepairUser.getRepairDescription();
        String repairingCharges = modelRepairUser.getRepairingCharges();
        String inWarranty = modelRepairUser.getInWarranty();
        final String customerEmail = modelRepairUser.getCustomerEmail();
        String repairStatus = modelRepairUser.getRepairStatus();
        String timestamp = modelRepairUser.getTimestamp();
        final String repairTo = modelRepairUser.getRepairTo();

        //get shop info
        loadShopInfo(modelRepairUser, holder);

        //set data
        holder.repairIdTv.setText("Repair ID: "+repairId);
        holder.statusTv.setText(repairStatus);

        if (repairingCharges.equals("0")) {
            holder.repairingChargesTv.setText("No Repairing Charges");
        }
        else {
            holder.repairingChargesTv.setText("Repairing Charges: ₹" + repairingCharges);
        }

        //change order status text color
        if(repairStatus.equals("In Progress")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorBlue));
        }
        else if(repairStatus.equals("Completed")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }
        else if(repairStatus.equals("Cancelled")) {
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));
        }

        //convert timestamp to proper format
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String formatedDate = DateFormat.format("dd/MM/yyyy", calendar).toString(); //eg 22/11/2020

        holder.repairDateTv.setText(formatedDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open repair details
                Intent intent = new Intent(context, RepairDetailsUserActivity.class);
                intent.putExtra("repairId", repairId);    //to load repair info
                intent.putExtra("repairTo", repairTo);
                context.startActivity(intent);
            }
        });
    }

    private void loadShopInfo(ModelRepairUser modelRepairUser, final HolderRepairUser holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelRepairUser.getRepairTo())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String shopName = ""+dataSnapshot.child("shopName").getValue();
                        holder.shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return repairUserArrayList.size();
    }

    // view holder class for row_repair_user.xml
    class HolderRepairUser extends RecyclerView.ViewHolder {

        // ui views of row_repair_user.xml
        private TextView repairIdTv, repairDateTv, shopNameTv, repairingChargesTv, statusTv;

        public HolderRepairUser(@NonNull View itemView) {
            super(itemView);

            //init ui views
            repairIdTv = itemView.findViewById(R.id.repairIdTv);
            repairDateTv = itemView.findViewById(R.id.repairDateTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            repairingChargesTv = itemView.findViewById(R.id.repairingChargesTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}

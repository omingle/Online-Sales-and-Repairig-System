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

import com.sdlproject.salesandrepair.R;
import com.sdlproject.salesandrepair.activities.OrderDetailsSellerActivity;
import com.sdlproject.salesandrepair.activities.RepairDetailsSellerActivity;
import com.sdlproject.salesandrepair.models.ModelRepairSeller;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterRepairSeller extends RecyclerView.Adapter<AdapterRepairSeller.HolderRepairSeller> {

    private Context context;
    public ArrayList<ModelRepairSeller> repairSellerArrayList;

    public AdapterRepairSeller(Context context, ArrayList<ModelRepairSeller> repairSellerArrayList) {
        this.context = context;
        this.repairSellerArrayList = repairSellerArrayList;
    }

    @NonNull
    @Override
    public HolderRepairSeller onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_repair_seller, parent, false);
        return new HolderRepairSeller(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderRepairSeller holder, int position) {
        // get data at position
        ModelRepairSeller modelRepairSeller = repairSellerArrayList.get(position);
        final String repairId = modelRepairSeller.getRepairId();
        String productId = modelRepairSeller.getProductId();
        String repairDescription = modelRepairSeller.getRepairDescription();
        String repairingCharges = modelRepairSeller.getRepairingCharges();
        String inWarranty = modelRepairSeller.getInWarranty();
        final String customerEmail = modelRepairSeller.getCustomerEmail();
        String repairStatus = modelRepairSeller.getRepairStatus();
        String timestamp = modelRepairSeller.getTimestamp();
        String repairTo = modelRepairSeller.getRepairTo();

        //set data
        holder.repairIdTv.setText("Repair ID: "+repairId);
        holder.emailTv.setText(customerEmail);
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
                Intent intent = new Intent(context, RepairDetailsSellerActivity.class);
                intent.putExtra("repairId", repairId);    //to load repair info
                intent.putExtra("custEmail", customerEmail);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return repairSellerArrayList.size();
    }

    // view holder class for row_repair_seller.xml
    class HolderRepairSeller extends RecyclerView.ViewHolder {

        // ui views of row_repair_seller.xml
        private TextView repairIdTv, repairDateTv, emailTv, repairingChargesTv, statusTv;

        public HolderRepairSeller(@NonNull View itemView) {
            super(itemView);

            //init ui views
            repairIdTv = itemView.findViewById(R.id.repairIdTv);
            repairDateTv = itemView.findViewById(R.id.repairDateTv);
            emailTv = itemView.findViewById(R.id.emailTv);
            repairingChargesTv = itemView.findViewById(R.id.repairingChargesTv);
            statusTv = itemView.findViewById(R.id.statusTv);
        }
    }
}

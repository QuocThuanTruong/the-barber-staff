package com.qtt.barberstaffapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.qtt.barberstaffapp.Common.Common;
import com.qtt.barberstaffapp.DoneServiceActivity;
import com.qtt.barberstaffapp.Interface.IRecyclerItemSelectedListener;
import com.qtt.barberstaffapp.Model.BookingInformation;
import com.qtt.barberstaffapp.Model.TimeSlot;
import com.qtt.barberstaffapp.R;

import java.util.ArrayList;
import java.util.List;

public class MyTimeSlotAdapter extends RecyclerView.Adapter<MyTimeSlotAdapter.TimeSlotViewHolder> {

    private Context context;
    private List<BookingInformation> timeSlotList; //stored slot is booked
    private List<CardView> cardViewList;

    public MyTimeSlotAdapter(Context context) {
        this.context = context;
        timeSlotList = new ArrayList<>();
        cardViewList = new ArrayList<>();
    }

    public MyTimeSlotAdapter(Context context, List<BookingInformation> timeSlotList) {
        this.context = context;
        this.timeSlotList = timeSlotList;
        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_time_slot, parent, false);
        return new TimeSlotViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final TimeSlotViewHolder holder, int position) {
        holder.tvTimeSlot.setText(new StringBuilder(Common.convertTimeSlotToString(position)).toString());

        if (timeSlotList.size() == 0) { //all are available
            holder.tvTimeSlotDesc.setText("Available");
            holder.tvTimeSlotDesc.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.tvTimeSlot.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.cardViewTimeSlot.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));

            holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                @Override
                public void onItemSelectedListener(View view, int position) {
                    //do nothing
                }
            });

        } else { //at least 1 slot full
            //get time slot from server
            for (final BookingInformation timeSlot : timeSlotList) {
                int slot = timeSlot.getTimeSlot();
                if (slot == position) {//is booked
                    if (!timeSlot.isDone()) {
                        holder.cardViewTimeSlot.setTag(Common.KEY_DISABLE);
                        holder.tvTimeSlotDesc.setText("Full");
                        holder.tvTimeSlotDesc.setTextColor(context.getResources().getColor(android.R.color.white));
                        holder.tvTimeSlot.setTextColor(context.getResources().getColor(android.R.color.white));
                        holder.cardViewTimeSlot.setCardBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));

                        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelectedListener(View view, int position) {

                                //get booking info
                                FirebaseFirestore.getInstance()
                                        .collection("AllSalon")
                                        .document(Common.stateName)
                                        .collection("Branch")
                                        .document(Common.selectedSalon.getId())
                                        .collection("Barbers")
                                        .document(Common.currentBarber.getBarberId())
                                        .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                                        .document(String.valueOf(timeSlot.getTimeSlot()))
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                Log.d("CCCC", "onComplete: " + task.getResult());
                                                if (task.isSuccessful()) {
                                                    if (task.getResult().exists()) {
                                                        Common.currentBookingInfo = task.getResult().toObject(BookingInformation.class);
                                                        Common.currentBookingInfo.setBookingId(task.getResult().getId());
                                                        context.startActivity(new Intent(context, DoneServiceActivity.class));
                                                    }
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        });
                    } else {
                        holder.cardViewTimeSlot.setTag(Common.KEY_DISABLE);
                        holder.tvTimeSlotDesc.setText("Done");
                        holder.tvTimeSlotDesc.setTextColor(context.getResources().getColor(android.R.color.white));
                        holder.tvTimeSlot.setTextColor(context.getResources().getColor(android.R.color.white));
                        holder.cardViewTimeSlot.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_blue_bright));
                        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                            @Override
                            public void onItemSelectedListener(View view, int position) {

                            }
                        });
                    }
                } else {
                    if (holder.getiRecyclerItemSelectedListener() == null)
                    holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
                        @Override
                        public void onItemSelectedListener(View view, int position) {

                        }
                    });
                }
            }
        }

        if (!cardViewList.contains(holder.cardViewTimeSlot))
            cardViewList.add(holder.cardViewTimeSlot);

    }


    @Override
    public int getItemCount() {
        return Common.TIME_SLOT_TOTAL;
    }

    public class TimeSlotViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTimeSlot, tvTimeSlotDesc;
        CardView cardViewTimeSlot;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public IRecyclerItemSelectedListener getiRecyclerItemSelectedListener() {
            return iRecyclerItemSelectedListener;
        }

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public TimeSlotViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTimeSlot = itemView.findViewById(R.id.tv_time_slot);
            tvTimeSlotDesc = itemView.findViewById(R.id.tv_time_slot_desc);
            cardViewTimeSlot = itemView.findViewById(R.id.card_view_time_slot);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectedListener(v, getAdapterPosition());
        }
    }
}

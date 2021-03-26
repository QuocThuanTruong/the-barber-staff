package com.qtt.barberstaffapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.qtt.barberstaffapp.Common.Common;
import com.qtt.barberstaffapp.Interface.IRecyclerItemSelectedListener;
import com.qtt.barberstaffapp.Model.BarberServices;
import com.qtt.barberstaffapp.R;
import com.qtt.barberstaffapp.databinding.LayoutServiceItemBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MyServiceAdapter extends RecyclerView.Adapter<MyServiceAdapter.MyViewHolder> {
    private Context context;
    private List<BarberServices> servicesList;
    List<String> selectedServicesList;
    List<CardView> cardViewList;
    LayoutServiceItemBinding binding;

    public MyServiceAdapter(Context context, List<BarberServices> servicesList) {
        this.context = context;
        this.servicesList = servicesList;
        cardViewList = new ArrayList<>();
    }

    public MyServiceAdapter(Context context, List<BarberServices> servicesList, List<String> selectedServicesList) {
        this.context = context;
        this.servicesList = servicesList;
        this.selectedServicesList = selectedServicesList;
        cardViewList = new ArrayList<>();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;
        LayoutServiceItemBinding binding;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LayoutServiceItemBinding.bind(itemView);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectedListener(v, getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = LayoutServiceItemBinding.inflate(LayoutInflater.from(context), parent, false);
        View itemView = binding.getRoot();
        return new MyViewHolder((itemView));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.binding.tvServiceName.setText(servicesList.get(position).getName());
        holder.binding.tvServicePrice.setText(new StringBuilder("$").append(servicesList.get(position).getPrice()).toString());
        Picasso.get().load(servicesList.get(position).getAvatar()).error(R.drawable.img_not_found).into(holder.binding.imgService);

        if (!cardViewList.contains(holder.binding.cardViewService))
            cardViewList.add(holder.binding.cardViewService);

        for (String service: selectedServicesList) {
            if (servicesList.get(position).getUid().equals(service)) {
                cardViewList.get(position).setCardBackgroundColor(context.getResources().getColor(R.color.colorCardSelected));
            }
        }


        holder.setiRecyclerItemSelectedListener((view, position1) -> {
            for (CardView cardView : cardViewList)
                cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));

            cardViewList.get(position).setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_orange_dark));

            //call event bus calc total price
            Common.selectedService = servicesList.get(position1);
        });
    }

    @Override
    public int getItemCount() {
        return servicesList.size();
    }

}

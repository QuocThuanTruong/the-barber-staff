package com.qtt.barberstaffapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qtt.barberstaffapp.Model.BarberServices;
import com.qtt.barberstaffapp.Model.CartItem;
import com.qtt.barberstaffapp.databinding.LayoutInvoiceItemBinding;

import java.util.List;

public class MyInvoiceServiceAdapter extends RecyclerView.Adapter<MyInvoiceServiceAdapter.MyViewHolder> {
    Context context;
    List<BarberServices> cartItemList;
    LayoutInvoiceItemBinding binding;

    public MyInvoiceServiceAdapter(Context context, List<BarberServices> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
    }

    public class MyViewHolder  extends RecyclerView.ViewHolder{
        LayoutInvoiceItemBinding binding;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = LayoutInvoiceItemBinding.bind(itemView);
        }
    }

    @NonNull
    @Override
    public MyInvoiceServiceAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = LayoutInvoiceItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new MyInvoiceServiceAdapter.MyViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull MyInvoiceServiceAdapter.MyViewHolder holder, int position) {
        binding.tvItemName.setText(cartItemList.get(position).getName());
        binding.tvItemQuantity.setText("");
        binding.tvItemPrice.setText(new StringBuilder("$").append(cartItemList.get(position).getPrice()).toString());
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

}

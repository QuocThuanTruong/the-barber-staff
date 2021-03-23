package com.qtt.barberstaffapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qtt.barberstaffapp.Model.CartItem;
import com.qtt.barberstaffapp.databinding.LayoutInvoiceItemBinding;

import java.util.List;

public class MyInvoiceItemAdapter extends RecyclerView.Adapter<MyInvoiceItemAdapter.MyViewHolder> {
    Context context;
    List<CartItem> cartItemList;
    LayoutInvoiceItemBinding binding;

    public MyInvoiceItemAdapter(Context context, List<CartItem> cartItemList) {
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
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = LayoutInvoiceItemBinding.inflate(LayoutInflater.from(context), parent, false);
        return new MyViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        binding.tvItemName.setText(cartItemList.get(position).getProductName());
        binding.tvItemQuantity.setText("" + cartItemList.get(position).getProductQuantity());
        binding.tvItemPrice.setText(new StringBuilder("$").append(cartItemList.get(position).getProductPrice()).toString());
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

}

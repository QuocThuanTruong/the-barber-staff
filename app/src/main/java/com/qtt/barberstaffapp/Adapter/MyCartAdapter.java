package com.qtt.barberstaffapp.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qtt.barberstaffapp.Common.Common;
import com.qtt.barberstaffapp.Model.CartItem;
import com.qtt.barberstaffapp.R;
import com.qtt.barberstaffapp.databinding.LayoutCartItemBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.MyCartHolder> {

    List<CartItem> cartItemList;
    Context context;
    LayoutCartItemBinding binding;

    public MyCartAdapter(List<CartItem> cartItemList, Context context) {
        this.cartItemList = cartItemList;
        this.context = context;
    }


    @NonNull
    @Override
    public MyCartHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = LayoutCartItemBinding.inflate(LayoutInflater.from(context), parent, false);
        View itemView = binding.getRoot();
        return new MyCartHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyCartHolder holder, int position) {

        Picasso.get().load(cartItemList.get(position).getProductImage()).error(R.drawable.img_not_found).into(holder.binding.imgCart);
        holder.binding.tvCartName.setText(cartItemList.get(position).getProductName());
        holder.binding.tvCartPrice.setText(new StringBuilder("$").append(cartItemList.get(position).getProductPrice()).toString());
        holder.binding.tvQuantity.setText(cartItemList.get(position).getProductQuantity() + "");

        holder.setListener((view, position1, isDecrease) -> {

            if (isDecrease) {
                if (cartItemList.get(position1).getProductQuantity() == 1) {
                    CartItem cartItem = cartItemList.get(position1);
                    cartItemList.remove(cartItem);

                    if (Common.currentBookingInfo.getCartItemList() != null) {
                        Common.currentBookingInfo.getCartItemList().remove(cartItem);
                    }

                    notifyDataSetChanged();

                }
                else if (cartItemList.get(position1).getProductQuantity() > 1) {
                    cartItemList.get(position1)
                            .setProductQuantity(cartItemList.get(position1).getProductQuantity() - 1);

                    holder.binding.tvQuantity.setText(cartItemList.get(position1).getProductQuantity() +"");
                }

            } else {
                if (cartItemList.get(position1).getProductQuantity() < 99) {
                    cartItemList.get(position1)
                            .setProductQuantity(cartItemList.get(position1).getProductQuantity() + 1);

                    holder.binding.tvQuantity.setText(cartItemList.get(position1).getProductQuantity() +"");

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }


    public class MyCartHolder extends RecyclerView.ViewHolder{
        LayoutCartItemBinding binding;
        IImageViewListener listener;

        public void setListener(IImageViewListener listener) {
            this.listener = listener;
        }

        public MyCartHolder(@NonNull View itemView) {
            super(itemView);

            binding = LayoutCartItemBinding.bind(itemView);

            binding.imgDecrease.setOnClickListener(v -> listener.onImageViewClickListener(v, getAdapterPosition(), true));

            binding.imgIncrease.setOnClickListener(v -> listener.onImageViewClickListener(v, getAdapterPosition(), false));
        }
    }

    private interface IImageViewListener {
        void onImageViewClickListener(View view, int position, boolean isDecrease);
    }
}


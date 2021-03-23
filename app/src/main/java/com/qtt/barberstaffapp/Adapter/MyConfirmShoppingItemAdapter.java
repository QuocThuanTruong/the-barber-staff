package com.qtt.barberstaffapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qtt.barberstaffapp.Model.CartItem;
import com.qtt.barberstaffapp.Model.ShoppingItem;
import com.qtt.barberstaffapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MyConfirmShoppingItemAdapter extends RecyclerView.Adapter<MyConfirmShoppingItemAdapter.MyViewHolder> {

    Context context;
    List<CartItem> shoppingItemList;

    public MyConfirmShoppingItemAdapter(Context context, List<CartItem> shoppingItemList) {
        this.context = context;
        this.shoppingItemList = shoppingItemList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_confirm_shopping, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Picasso.get()
                .load(shoppingItemList.get(position).getProductName())
                .error(R.drawable.img_not_found)
                .into(holder.imgItem);

        holder.tvItemName.setText(new StringBuilder(shoppingItemList.get(position).getProductName())
                .append(" x").append(shoppingItemList.get(position).getProductQuantity()).toString());


    }

    @Override
    public int getItemCount() {
        return shoppingItemList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.img_item)
        ImageView imgItem;
        @BindView(R.id.tv_item_name)
        TextView tvItemName;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

        }
    }
}

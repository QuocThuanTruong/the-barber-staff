package com.qtt.barberstaffapp.Adapter;

import android.content.Context;
import android.database.DatabaseUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qtt.barberstaffapp.Common.Common;
import com.qtt.barberstaffapp.Interface.IOnShoppingItemSelected;
import com.qtt.barberstaffapp.Interface.IRecyclerItemSelectedListener;
import com.qtt.barberstaffapp.Model.ShoppingItem;
import com.qtt.barberstaffapp.R;
import com.qtt.barberstaffapp.databinding.LayoutShoppingItemBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MyShoppingItemAdapter extends RecyclerView.Adapter<MyShoppingItemAdapter.MyShoppingItemViewHolder> {

    private Context context;
    private List<ShoppingItem> shoppingItemList;
    IOnShoppingItemSelected iOnShoppingItemSelected;
    LayoutShoppingItemBinding binding;

    public MyShoppingItemAdapter(Context context, List<ShoppingItem> shoppingItemList, IOnShoppingItemSelected iOnShoppingItemSelected) {
        this.context = context;
        this.shoppingItemList = shoppingItemList;
        this.iOnShoppingItemSelected = iOnShoppingItemSelected;
    }

    @NonNull
    @Override
    public MyShoppingItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = LayoutShoppingItemBinding.inflate(LayoutInflater.from(context), parent, false);
        View itemView = binding.getRoot();
        return new MyShoppingItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyShoppingItemViewHolder holder, int position) {
        Picasso.get().load(shoppingItemList.get(position).getImage()).error(R.drawable.img_not_found).into(holder.binding.imgShoppingItem);
        holder.binding.tvShoppingItemName.setText(Common.formatShoppingItemName(shoppingItemList.get(position).getName()));
        holder.binding.tvShoppingItemPrice.setText(new StringBuilder("$").append(shoppingItemList.get(position).getPrice()).toString());

        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int position) {
                iOnShoppingItemSelected.onShoppingItemSelected(shoppingItemList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return shoppingItemList.size();
    }

    public class MyShoppingItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        LayoutShoppingItemBinding binding;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public MyShoppingItemViewHolder(@NonNull View itemView) {
            super(itemView);

            binding = LayoutShoppingItemBinding.bind(itemView);

            binding.tvAddToCart.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectedListener(v, getAdapterPosition());
        }
    }
}

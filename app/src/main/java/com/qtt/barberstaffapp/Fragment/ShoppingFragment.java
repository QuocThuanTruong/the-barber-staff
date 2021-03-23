package com.qtt.barberstaffapp.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.qtt.barberstaffapp.Adapter.MyShoppingItemAdapter;
import com.qtt.barberstaffapp.Common.SpacesItemDecoration;
import com.qtt.barberstaffapp.Interface.IOnShoppingItemSelected;
import com.qtt.barberstaffapp.Interface.IShoppingItemsLoadListener;
import com.qtt.barberstaffapp.Model.ShoppingItem;
import com.qtt.barberstaffapp.R;
import com.qtt.barberstaffapp.databinding.FragmentShoppingBinding;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShoppingFragment extends BottomSheetDialogFragment implements IShoppingItemsLoadListener, IOnShoppingItemSelected {
    IOnShoppingItemSelected callBackToActivity;
    CollectionReference shoppingItemRef;
    IShoppingItemsLoadListener iShoppingItemsLoadListener;
    FragmentShoppingBinding binding;

    private static ShoppingFragment instance;

    public static ShoppingFragment getInstance(IOnShoppingItemSelected iOnShoppingItemSelected) {
        if (instance == null) {
            instance = new ShoppingFragment(iOnShoppingItemSelected);
        }
        return instance;
    }

    void chipWaxClick() {
        setChipSelected(binding.chipWax);
        loadShoppingItems(binding.chipWax.getText().toString());
    }


    void chipSprayClick() {
        setChipSelected(binding.chipSpray);
        loadShoppingItems(binding.chipSpray.getText().toString());
    }


    void chipHairCareClick() {
        setChipSelected(binding.chipHairCare);
    }

    void chipBodyCareClick() {
        setChipSelected(binding.chipBodyCare);
    }

    void chipColorClick() { setChipSelected(binding.chipColor);}

    private void setChipSelected(Chip chipWax) {
        for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
            Chip chipItem = (Chip) binding.chipGroup.getChildAt(i);
            if (chipItem.getId() != chipWax.getId()) {
                chipItem.setChipBackgroundColorResource(R.color.colorBackground);

                chipItem.setTextColor(getResources().getColor(R.color.colorPrimary));
            } else {
                chipItem.setChipBackgroundColorResource(R.color.colorPrimary);
                chipItem.setTextColor(getResources().getColor(android.R.color.white));
            }
        }
    }

    private void loadShoppingItems(String item) {
        shoppingItemRef = FirebaseFirestore.getInstance()
                .collection("Shopping")
                .document(item)
                .collection("Items");

        shoppingItemRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ShoppingItem> shoppingItems = new ArrayList<>();

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            ShoppingItem shoppingItem = queryDocumentSnapshot.toObject(ShoppingItem.class);
                            shoppingItem.setId(queryDocumentSnapshot.getId());
                            shoppingItems.add(shoppingItem);
                        }

                        iShoppingItemsLoadListener.onShoppingItemsLoadSuccess(shoppingItems);
                    }
                }).addOnFailureListener(e -> iShoppingItemsLoadListener.onShoppingItemsLoadFailed(e.getMessage()));
    }


    public ShoppingFragment(IOnShoppingItemSelected callBackToActivity) {
        this.callBackToActivity = callBackToActivity;
    }

    public ShoppingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentShoppingBinding.inflate(inflater, container, false);
        View itemView = binding.getRoot();

        ButterKnife.bind(this, itemView);

        iShoppingItemsLoadListener = this;

        //Default load
        loadShoppingItems(binding.chipWax.getText().toString());

        initView();

        return itemView;
    }

    private void initView() {
        binding.recyclerItems.setHasFixedSize(true);
        binding.recyclerItems.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerItems.addItemDecoration(new SpacesItemDecoration(8));

        binding.chipBodyCare.setOnClickListener(v -> chipBodyCareClick());
        binding.chipColor.setOnClickListener(v -> chipColorClick());
        binding.chipHairCare.setOnClickListener(v -> chipHairCareClick());
        binding.chipWax.setOnClickListener(v -> chipWaxClick());
        binding.chipSpray.setOnClickListener(v -> chipSprayClick());
    }

    @Override
    public void onShoppingItemsLoadSuccess(List<ShoppingItem> shoppingItemList) {
        MyShoppingItemAdapter myShoppingItemAdapter = new MyShoppingItemAdapter(getContext(), shoppingItemList, this);
        binding.recyclerItems.setAdapter(myShoppingItemAdapter);
    }

    @Override
    public void onShoppingItemsLoadFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {
        callBackToActivity.onShoppingItemSelected(shoppingItem);
    }
}

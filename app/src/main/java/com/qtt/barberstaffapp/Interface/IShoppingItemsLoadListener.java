package com.qtt.barberstaffapp.Interface;

import com.qtt.barberstaffapp.Model.ShoppingItem;

import java.util.List;

public interface IShoppingItemsLoadListener {
    void onShoppingItemsLoadSuccess(List<ShoppingItem> shoppingItemList);
    void onShoppingItemsLoadFailed(String message);
}

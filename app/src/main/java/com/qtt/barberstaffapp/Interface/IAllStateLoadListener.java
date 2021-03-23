package com.qtt.barberstaffapp.Interface;

import com.qtt.barberstaffapp.Model.City;

import java.util.List;

public interface IAllStateLoadListener {
    void onAllStateLoadSuccess(List<City> cityList);
    void onAllStateLoadFailed(String message);
}

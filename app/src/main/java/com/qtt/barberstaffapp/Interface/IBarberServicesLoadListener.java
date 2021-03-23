package com.qtt.barberstaffapp.Interface;

import com.qtt.barberstaffapp.Model.BarberServices;

import java.util.List;

public interface IBarberServicesLoadListener {
    void onBarberServicesLoadSuccess(List<BarberServices> barberServicesList);

    void onBarberServicesLoadFailed(String message);
}

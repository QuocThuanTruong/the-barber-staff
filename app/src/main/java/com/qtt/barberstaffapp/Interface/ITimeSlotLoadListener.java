package com.qtt.barberstaffapp.Interface;

import com.qtt.barberstaffapp.Model.BookingInformation;
import com.qtt.barberstaffapp.Model.TimeSlot;

import java.util.List;

public interface ITimeSlotLoadListener {
    void onTimeSlotLoadSuccess(List<BookingInformation> timeSlotList);
    void onTimeSlotLoadFailed(String message);
    void onTimeSlotLoadEmpty();
}

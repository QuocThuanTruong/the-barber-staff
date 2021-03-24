package com.qtt.barberstaffapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.qtt.barberstaffapp.Adapter.MyTimeSlotAdapter;
import com.qtt.barberstaffapp.Common.Common;
import com.qtt.barberstaffapp.Common.SpacesItemDecoration;
import com.qtt.barberstaffapp.Interface.INotificationCountListener;
import com.qtt.barberstaffapp.Interface.ITimeSlotLoadListener;
import com.qtt.barberstaffapp.Model.BookingInformation;
import com.qtt.barberstaffapp.Model.TimeSlot;
import com.qtt.barberstaffapp.databinding.ActivityStaffHomeBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class StaffHomeActivity extends AppCompatActivity implements ITimeSlotLoadListener, INotificationCountListener {
    ActivityStaffHomeBinding binding;
    TextView tvBarberName;

    TextView tvNotificationBadge;

    CollectionReference notificationCol;
    CollectionReference currentBookDateCol;

    EventListener<QuerySnapshot> notificationEvent;
    EventListener<QuerySnapshot> bookingEvent;

    ListenerRegistration notificationListener;
    ListenerRegistration bookingRealTimeListener;

    ActionBarDrawerToggle actionBarDrawerToggle;

    ITimeSlotLoadListener iTimeSlotLoadListener;
    INotificationCountListener iNotificationCountListener;

    DocumentReference barberDoc;
    android.app.AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));
        }

        init();
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        if (item.getItemId() == R.id.action_new_notification) {
            startActivity(new Intent(StaffHomeActivity.this, NotificationActivity.class));
            tvNotificationBadge.setText("");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, binding.staffHomeActivity, R.string.open, R.string.close);
        binding.staffHomeActivity.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       binding.navigationView.setNavigationItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.menu_exit) {
                logOut();
            } else if (menuItem.getItemId() == R.id.menu_update_profile) {
                startActivity(new Intent(StaffHomeActivity.this, UpdateProfileActivity.class));
            }
            return true;
        });

        View header = binding.navigationView.getHeaderView(0);
        tvBarberName = header.findViewById(R.id.tv_barber_name);
        tvBarberName.setText(new StringBuilder().append("Hi! ").append(Common.currentBarber.getName()).toString());

        //
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(), Common.simpleDateFormat.format(date.getTime()));

        binding.recyclerTimeSlot.setHasFixedSize(true);
        binding.recyclerTimeSlot.setLayoutManager(new GridLayoutManager(this, 3));
        binding.recyclerTimeSlot.addItemDecoration(new SpacesItemDecoration(8));

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 4); //current date + 2 day

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendar_view)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)
                .mode(HorizontalCalendar.Mode.DAYS)
                .build();
        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (date.getTimeInMillis() != Common.bookingDate.getTimeInMillis()) {
                    Common.bookingDate = date;
                    loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(), Common.simpleDateFormat.format(date.getTime()));
                }
            }
        });


    }

    private void logOut() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure to log out?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Paper.init(StaffHomeActivity.this);
                        Paper.book().delete(Common.BARBER_KEY);
                        Paper.book().delete(Common.LOGED_KEY);
                        Paper.book().delete(Common.SALON_KEY);
                        Paper.book().delete(Common.STATE_KEY);

                        Intent intent = new Intent(StaffHomeActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void loadAvailableTimeSlotOfBarber(String barberId, final String date) {
        dialog.show();

        //Check info of barber
        barberDoc.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot barberSnapShot = task.getResult();
                            if (barberSnapShot.exists()) {
                                CollectionReference dateRef = FirebaseFirestore.getInstance()
                                        .collection("AllSalon")
                                        .document(Common.stateName)
                                        .collection("Branch")
                                        .document(Common.selectedSalon.getId())
                                        .collection("Barbers")
                                        .document(Common.currentBarber.getBarberId())
                                        .collection(date);

                                dateRef.get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    QuerySnapshot querySnapshot = task.getResult();

                                                    if (querySnapshot.isEmpty()) {
                                                        iTimeSlotLoadListener.onTimeSlotLoadEmpty(); //load default time slot list
                                                    } else {
                                                        List<BookingInformation> timeSlotList = new ArrayList<>();
                                                        for (QueryDocumentSnapshot timeSlotSnapShot : querySnapshot) {
                                                            BookingInformation slot = timeSlotSnapShot.toObject(BookingInformation.class);
                                                            timeSlotList.add(slot);
                                                        }

                                                        iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlotList);
                                                    }
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage());
                                    }
                                });
                            }
                        }
                    }
                });
    }

    private void init() {
        iNotificationCountListener = this;
        iTimeSlotLoadListener = this;
        initNotificationRealTimeUpdate();
        initBookingRealTimeUpdate();
    }

    private void initBookingRealTimeUpdate() {
        // /AllSalon/Florida/Branch/0n7ikrtgQXW4EXhuJ0qy/Barbers/Nsa4hBFukd8UZYMiRe5y
        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.stateName)
                .collection("Branch")
                .document(Common.selectedSalon.getId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId());

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);

        bookingEvent = new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                loadAvailableTimeSlotOfBarber(Common.currentBarber.getBarberId(), Common.simpleDateFormat.format(calendar.getTime()));
            }
        };

        currentBookDateCol = barberDoc.collection(Common.simpleDateFormat.format(calendar.getTime()));

        bookingRealTimeListener = currentBookDateCol.addSnapshotListener(bookingEvent);
    }

    private void initNotificationRealTimeUpdate() {
        notificationCol = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.stateName)
                .collection("Branch")
                .document(Common.selectedSalon.getId())
                .collection("Barbers")
                .document(Common.currentBarber.getBarberId())
                .collection("Notifications");

        notificationEvent = (queryDocumentSnapshot, e) -> loadNotification();

        notificationListener = notificationCol.whereEqualTo("read", false)
                .addSnapshotListener(notificationEvent);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to exit?")
                .setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //TO DO
                        Toast.makeText(StaffHomeActivity.this, "exit", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void onTimeSlotLoadSuccess(List<BookingInformation> timeSlotList) {
        MyTimeSlotAdapter myTimeSlotAdapter = new MyTimeSlotAdapter(this, timeSlotList);
        binding.recyclerTimeSlot.setAdapter(myTimeSlotAdapter);

        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        dialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter myTimeSlotAdapter = new MyTimeSlotAdapter(this);
        binding.recyclerTimeSlot.setAdapter(myTimeSlotAdapter);

        dialog.dismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.staff_home_menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_new_notification);

        tvNotificationBadge = menuItem.getActionView().findViewById(R.id.tv_notification_badge);

        loadNotification();
        menuItem.getActionView().setOnClickListener(v -> onOptionsItemSelected(menuItem));
        return super.onCreateOptionsMenu(menu);
    }

    private void loadNotification() {
        notificationCol.whereEqualTo("read", false)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        iNotificationCountListener.onNotificationCountSuccess(task.getResult().size());
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(StaffHomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onNotificationCountSuccess(int count) {
        if (count != 0) {
            if (tvNotificationBadge != null) {
                tvNotificationBadge.setVisibility(View.VISIBLE);
            }

            if (count <= 9)
                tvNotificationBadge.setText(String.valueOf(count));
            else
                tvNotificationBadge.setText("9+");
        } else {
            tvNotificationBadge.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        loadNotification();
        initBookingRealTimeUpdate();
        initNotificationRealTimeUpdate();
        super.onResume();
    }

    @Override
    protected void onStop() {
        if (notificationListener != null) {
            notificationListener.remove();
        }
        if (bookingRealTimeListener != null) {
            bookingRealTimeListener.remove();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (notificationListener != null) {
            notificationListener.remove();
        }
        if (bookingRealTimeListener != null) {
            bookingRealTimeListener.remove();
        }
        super.onDestroy();
    }
}

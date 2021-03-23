package com.qtt.barberstaffapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.qtt.barberstaffapp.Adapter.MyStateAdapter;
import com.qtt.barberstaffapp.Common.Common;
import com.qtt.barberstaffapp.Common.SpacesItemDecoration;
import com.qtt.barberstaffapp.Interface.IAllStateLoadListener;
import com.qtt.barberstaffapp.Model.Barber;
import com.qtt.barberstaffapp.Model.City;
import com.qtt.barberstaffapp.Model.Salon;
import com.qtt.barberstaffapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements IAllStateLoadListener {

    CollectionReference allSalonCol;
    IAllStateLoadListener iAllStateLoadListener;
    ActivityMainBinding binding;

    MyStateAdapter adapter;
    AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorBackground));
        }

        Dexter.withContext(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                FirebaseInstanceId.getInstance()
                        .getInstanceId()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Common.updateToken(MainActivity.this, task.getResult().getToken());

                                Log.d("TOKEN_CLIENT_APP", task.getResult().getToken());

                            }
                        }).addOnFailureListener(e -> Log.d("TOKEN_CLIENT_APP", e.getMessage()));

                Paper.init(MainActivity.this);
                String user = Paper.book().read(Common.LOGED_KEY);
                if (TextUtils.isEmpty(user)) {
                    initView();
                    init();
                    loadAllStateFromFireStore();
                } else {
                    Gson gson = new Gson();
                    Common.stateName = Paper.book().read(Common.STATE_KEY);
                    Common.selectedSalon = gson.fromJson(Paper.book().read(Common.SALON_KEY, ""),
                            new TypeToken<Salon>(){}.getType());
                    Common.currentBarber = gson.fromJson(Paper.book().read(Common.BARBER_KEY, ""),
                            new TypeToken<Barber>(){}.getType());

                    Intent intent = new Intent(MainActivity.this, StaffHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                    token.continuePermissionRequest();
            }
        }).check();

    }

    private void loadAllStateFromFireStore() {
        loadingDialog.show();

        allSalonCol.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<City> cities = new ArrayList<>();
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                cities.add(documentSnapshot.toObject(City.class));
                            }

                            iAllStateLoadListener.onAllStateLoadSuccess(cities);
                            if (loadingDialog.isShowing())
                                loadingDialog.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iAllStateLoadListener.onAllStateLoadFailed(e.getMessage());
                if (loadingDialog.isShowing())
                    loadingDialog.dismiss();
            }
        });
    }

    private void init() {
        allSalonCol = FirebaseFirestore.getInstance().collection("AllSalon");
        iAllStateLoadListener = this;
        loadingDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
    }

    private void initView() {
        binding.recyclerState.setHasFixedSize(true);
        binding.recyclerState.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerState.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onAllStateLoadSuccess(List<City> cityList) {
        adapter = new MyStateAdapter(this, cityList);
        binding.recyclerState.setAdapter(adapter);
    }

    @Override
    public void onAllStateLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

package com.qtt.barberstaffapp.Fragment;


import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firestore.v1.DocumentTransform;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qtt.barberstaffapp.Adapter.MyConfirmShoppingItemAdapter;
import com.qtt.barberstaffapp.Adapter.MyInvoiceItemAdapter;
import com.qtt.barberstaffapp.Adapter.MyInvoiceServiceAdapter;
import com.qtt.barberstaffapp.Common.Common;
import com.qtt.barberstaffapp.EventBus.DismissDoneServiceEvent;
import com.qtt.barberstaffapp.Model.BarberServices;
import com.qtt.barberstaffapp.Model.CartItem;
import com.qtt.barberstaffapp.Model.FCMResponse;
import com.qtt.barberstaffapp.Model.FCMSendData;
import com.qtt.barberstaffapp.Model.Invoice;
import com.qtt.barberstaffapp.Model.LookBook;
import com.qtt.barberstaffapp.Model.MyNotification;
import com.qtt.barberstaffapp.Model.MyToken;
import com.qtt.barberstaffapp.R;
import com.qtt.barberstaffapp.Retrofit.IFCMService;
import com.qtt.barberstaffapp.Retrofit.RetrofitClient;
import com.qtt.barberstaffapp.databinding.FragmentTotalPriceBinding;

import org.greenrobot.eventbus.EventBus;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 */
public class TotalPriceFragment extends BottomSheetDialogFragment {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    FragmentTotalPriceBinding binding;

    HashSet<BarberServices> servicesAdded = new HashSet<>();

    IFCMService ifcmService;
    AlertDialog dialog;
    String imgUrl;

    private  static TotalPriceFragment instance;

    public static TotalPriceFragment getInstance() {
        if (instance == null)
            instance = new TotalPriceFragment();
        return instance;
    }

    private TotalPriceFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentTotalPriceBinding.inflate(getLayoutInflater());
        View itemView =  binding.getRoot();
        init();

        initView();

        getBundle(getArguments());

        setInformation();

        return itemView;
    }

    private void setInformation() {
        binding.tvSalonName.setText(Common.selectedSalon.getName());
        binding.tvBarberName.setText(Common.currentBarber.getName());
        binding.tvTime.setText(Common.convertTimeSlotToString(Common.currentBookingInfo.getTimeSlot()));
        binding.tvCustomerName.setText(Common.currentBookingInfo.getCustomerName());
        binding.tvCustomerPhone.setText(Common.currentBookingInfo.getCustomerPhone());

        if (Common.currentBookingInfo.getCartItemList() != null) {
            if (Common.currentBookingInfo.getCartItemList().size() > 0) {
                MyInvoiceItemAdapter adapter = new MyInvoiceItemAdapter(getContext(), Common.currentBookingInfo.getCartItemList());
                binding.recyclerShopping.setAdapter(adapter);
            }



        }

        List<BarberServices> barberServicesList = new ArrayList<>();
        barberServicesList.add(Common.selectedService);

        MyInvoiceServiceAdapter mAdapter = new MyInvoiceServiceAdapter(getContext(), barberServicesList);
        binding.recyclerServices.setHasFixedSize(true);
        binding.recyclerServices.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerServices.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        binding.recyclerServices.setAdapter(mAdapter);

        calculatePrice();
    }

    private double calculatePrice() {
        double price = 0;


        price += Common.selectedService.getPrice();

        if (Common.currentBookingInfo.getCartItemList() != null) {
            for (CartItem shoppingItem : Common.currentBookingInfo.getCartItemList())
                price += (shoppingItem.getProductPrice() * shoppingItem.getProductQuantity());
        }

        binding.tvTotalPrice.setText(new StringBuilder("$").append(price * 100 / 100).toString());

        return price;
    }

    private void getBundle(Bundle arguments) {
//        this.servicesAdded = new Gson()
//                .fromJson(arguments.getString(Common.SERVICES_ADDED),
//                        new TypeToken<HashSet<BarberServices>>(){}.getType());
        this.servicesAdded.add(Common.selectedService);

        imgUrl = arguments.getString(Common.IMG_URL, null);
    }

    private void initView() {
        binding.recyclerShopping.setHasFixedSize(true);
        binding.recyclerShopping.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerShopping.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        binding.btnConfirm.setOnClickListener(v -> {
            //update infor set done = true
            dialog.show();
            ///AllSalon/Florida/Branch/0n7ikrtgQXW4EXhuJ0qy/Barbers/Nsa4hBFukd8UZYMiRe5y/29_09_2019/10
            final DocumentReference bookingSet = FirebaseFirestore.getInstance()
                    .collection("AllSalon")
                    .document(Common.stateName)
                    .collection("Branch")
                    .document(Common.selectedSalon.getId())
                    .collection("Barbers")
                    .document(Common.currentBarber.getBarberId())
                    .collection(Common.simpleDateFormat.format(Common.bookingDate.getTime()))
                    .document(String.valueOf(Common.currentBookingInfo.getTimeSlot()));

            //update
            bookingSet.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                Map<String, Object> dataUpdated = new HashMap<>();
                                dataUpdated.put("done", true);
                                bookingSet.update(dataUpdated)
                                        .addOnFailureListener(e -> {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                dialog.dismiss();
                                                createInvoice();

                                                if (imgUrl != null) {
                                                    LookBook lookBook = new LookBook();
                                                    lookBook.setUrl(imgUrl);


                                                    ///AllSalon/Florida/Branch/0n7ikrtgQXW4EXhuJ0qy/Barbers/Nsa4hBFukd8UZYMiRe5y
                                                    FirebaseFirestore.getInstance()
                                                            .collection("AllSalon")
                                                            .document(Common.currentBookingInfo.getCityBooking())
                                                            .collection("Branch")
                                                            .document(Common.currentBookingInfo.getSalonId())
                                                            .collection("Barbers")
                                                            .document(Common.currentBookingInfo.getBarberId())
                                                            .collection("LookBook")
                                                            .add(lookBook)
                                                            .addOnCompleteListener(task2 -> {

                                                            })
                                                            .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());

                                                    ///User/+84389294631
                                                    FirebaseFirestore.getInstance()
                                                            .collection("User")
                                                            .document(Common.currentBookingInfo.getCustomerPhone())
                                                            .collection("LookBook")
                                                            .add(lookBook)
                                                            .addOnCompleteListener(task2 -> {

                                                            })
                                                            .addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                                                }

                                            }
                                        });
                            }
                        }
                    }).addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void createInvoice() {
        CollectionReference invoiceRef = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.stateName)
                .collection("Branch")
                .document(Common.selectedSalon.getId())
                .collection("Invoices");

        Invoice invoice = new Invoice();
        invoice.setBarberId(Common.currentBarber.getBarberId());
        invoice.setBarberName(Common.currentBarber.getName());
        invoice.setSalonId(Common.selectedSalon.getId());
        invoice.setSalonName(Common.selectedSalon.getName());
        invoice.setSalonAddress(Common.selectedSalon.getAddress());
        invoice.setCustomerName(Common.currentBookingInfo.getCustomerName());
        invoice.setCustomerPhone(Common.currentBookingInfo.getCustomerPhone());
        invoice.setImgUrl(imgUrl); //receive from done services
        invoice.setBarberServicesList(new ArrayList<BarberServices>(servicesAdded));
        invoice.setShoppingItemList(Common.currentBookingInfo.getCartItemList());
        invoice.setFinalPrice(calculatePrice());

        invoiceRef.document()
                .set(invoice)
                .addOnFailureListener(e -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnSuccessListener(aVoid -> sendNotificationUpdateToUser(Common.currentBookingInfo.getCustomerPhone()));
    }

    private void sendNotificationUpdateToUser(String customerPhone) {
        //Create notification
        final MyNotification myNotification = new MyNotification();
            myNotification.setTitle(Common.selectedService.getName());
            myNotification.setContent("Finish service: $" + calculatePrice() + ", Barber: " + Common.currentBarber.getName());
            myNotification.setAvatar(Common.selectedService.getAvatar());


        myNotification.setUid(UUID.randomUUID().toString());
        myNotification.setRead(false);
        myNotification.setServerTimeStamp(Timestamp.now());

        FirebaseFirestore.getInstance()
                .collection("User")
                .document(Common.currentBookingInfo.getCustomerPhone())
                .collection("Notifications")
                .document(myNotification.getUid())
                .set(myNotification)
                .addOnCompleteListener(task -> {

                    FirebaseFirestore.getInstance()
                            .collection("Tokens")
                            .whereEqualTo("userPhone", customerPhone)
                            .get()
                            .addOnCompleteListener(task1 -> {
                                dialog.dismiss();
                                if (task1.isSuccessful()) {
                                    MyToken myToken = new MyToken();
                                    for (DocumentSnapshot documentSnapshot : task1.getResult())
                                        myToken = documentSnapshot.toObject(MyToken.class);

                                    Log.d("FCM", "sendNotificationUpdateToUser: " + myToken);
                                    //send notification
                                    FCMSendData fcmSendData = new FCMSendData();
                                    Map<String, String> dataSend = new HashMap<>();
                                    dataSend.put("update_done", "true");

                                    dataSend.put(Common.TITLE_KEY, "Finish Services");
                                    dataSend.put(Common.CONTENT_KEY, "Thank you for booking our services (Barber: " + Common.currentBarber.getName() + ")");

                                    //put information for rating
                                    dataSend.put(Common.RATING_STATE_KEY, Common.stateName);
                                    dataSend.put(Common.RATING_SALON_ID, Common.selectedSalon.getId());
                                    dataSend.put(Common.RATING_SALON_NAME, Common.selectedSalon.getName());
                                    dataSend.put(Common.RATING_BARBER_ID, Common.currentBarber.getBarberId());

                                    fcmSendData.setTo(myToken.getToken());
                                    fcmSendData.setData(dataSend);

                                    compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(Schedulers.newThread())
                                            .subscribe(fcmResponse -> {
                                                dialog.dismiss();
                                                dismiss();
                                                EventBus.getDefault().postSticky(new DismissDoneServiceEvent(true));
                                            }, throwable -> {
                                                dialog.dismiss();
                                                Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }));


                                }
                            });

                }).addOnFailureListener(e -> {
                        if (dialog.isShowing())
                            dialog.dismiss();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private void init() {
        dialog = new SpotsDialog.Builder().setContext(getContext())
                .setCancelable(false).build();

        ifcmService = RetrofitClient.getInstance().create(IFCMService.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}

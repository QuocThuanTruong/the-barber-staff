package com.qtt.barberstaffapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.qtt.barberstaffapp.Adapter.MyCartAdapter;
import com.qtt.barberstaffapp.Adapter.MyServiceAdapter;
import com.qtt.barberstaffapp.Common.Common;
import com.qtt.barberstaffapp.Common.SpacesItemDecoration;
import com.qtt.barberstaffapp.EventBus.DismissDoneServiceEvent;
import com.qtt.barberstaffapp.Fragment.ShoppingFragment;
import com.qtt.barberstaffapp.Fragment.TotalPriceFragment;
import com.qtt.barberstaffapp.Interface.IBarberServicesLoadListener;
import com.qtt.barberstaffapp.Interface.IOnShoppingItemSelected;
import com.qtt.barberstaffapp.Model.BarberServices;
import com.qtt.barberstaffapp.Model.CartItem;
import com.qtt.barberstaffapp.Model.FCMSendData;
import com.qtt.barberstaffapp.Model.LookBook;
import com.qtt.barberstaffapp.Model.MyToken;
import com.qtt.barberstaffapp.Model.ShoppingItem;
import com.qtt.barberstaffapp.Model.User;
import com.qtt.barberstaffapp.Retrofit.IFCMService;
import com.qtt.barberstaffapp.Retrofit.RetrofitClient;
import com.qtt.barberstaffapp.databinding.ActivityDoneServiceBinding;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class DoneServiceActivity extends AppCompatActivity implements IBarberServicesLoadListener, IOnShoppingItemSelected {

    private static final int MY_CAMERA_REQUEST_CODE = 301;
    ActivityDoneServiceBinding binding;

    AlertDialog dialog;
    IBarberServicesLoadListener iBarberServicesLoadListener;
    HashSet<BarberServices> servicesAdded;
    LayoutInflater inflater;
    MyCartAdapter myCartAdapter;

    Uri fileUri;

    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoneServiceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setCustomerInformation();

        init();
        initView();
        loadBarberServices();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        getSupportActionBar().setTitle("Check out");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.recyclerServices.setHasFixedSize(true);
        binding.recyclerServices.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.recyclerServices.addItemDecoration(new SpacesItemDecoration(8));

        binding.recyclerCart.setHasFixedSize(true);
        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recyclerCart.addItemDecoration(new SpacesItemDecoration(8));

        binding.switchPicture.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.cardPicture.setVisibility(View.VISIBLE);
                binding.btnFinish.setEnabled(false);
            } else {
                binding.imgAddCustomerHair.setVisibility(View.VISIBLE);
                binding.imgCustomerHair.setVisibility(View.GONE);
                binding.cardPicture.setVisibility(View.GONE);
                binding.btnFinish.setEnabled(true);
            }
        });


        binding.addShopping.setOnClickListener(v -> {
            ShoppingFragment shoppingFragment = ShoppingFragment.getInstance(DoneServiceActivity.this);
            shoppingFragment.show(getSupportFragmentManager(), "Shopping");
        });

        binding.imgAddCustomerHair.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            fileUri = getOutputMediaFileUri();

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, MY_CAMERA_REQUEST_CODE);
        });

        binding.btnFinish.setOnClickListener(v -> {
            if (!binding.switchPicture.isChecked()) {
                //Create fragment total price
                TotalPriceFragment totalPriceFragment = TotalPriceFragment.getInstance();
                Bundle bundle = new Bundle();
                bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(Common.selectedService));
                totalPriceFragment.setArguments(bundle);
                totalPriceFragment.show(getSupportFragmentManager(), "Price");
            } else {
                upLoadPicture(fileUri);
            }
        });
    }

    private void upLoadPicture(Uri fileUri) {
        if (fileUri != null) {
            dialog.show();

            String fileName = Common.getFileName(getContentResolver(), fileUri);
            String path = new StringBuilder("Customer_Pictures/").append(fileName).toString();

            storageReference = FirebaseStorage.getInstance().getReference(path);

            UploadTask uploadTask = storageReference.putFile(fileUri);

            Task<Uri> task = uploadTask.continueWithTask(task1 -> {
                if (!task1.isSuccessful()) {
                    Toast.makeText(DoneServiceActivity.this, "Failed to upload picture!", Toast.LENGTH_SHORT).show();
                }

                return storageReference.getDownloadUrl();

            }).addOnCompleteListener(task12 -> {
                if (task12.isSuccessful()) {
                    String url = task12.getResult().toString().substring(0, task12.getResult().toString().indexOf("&token"));
                    Log.d("AAAAA", "download: " + url);
                    dialog.dismiss();

                    //Create fragment total price
                    TotalPriceFragment totalPriceFragment = TotalPriceFragment.getInstance();
                    Bundle bundle = new Bundle();
                    bundle.putString(Common.SERVICES_ADDED, new Gson().toJson(servicesAdded));
                    bundle.putString(Common.IMG_URL, url);
                    totalPriceFragment.setArguments(bundle);
                    totalPriceFragment.show(getSupportFragmentManager(), "Price");


                }
            }).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(DoneServiceActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "Image is not found!", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {
        File mediaDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "BarberStaffApp");

        if (!mediaDir.exists()) {
            if (!mediaDir.mkdir()) {
                return null;
            }
        }

        String time_tamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaDir.getPath() + File.separator + "IMG_" + time_tamp +
                "_" + new Random().nextInt() + ".jpg");

        return mediaFile;
    }

    private void loadBarberServices() {
        ///AllSalon/Florida/Branch/0n7ikrtgQXW4EXhuJ0qy/Services/
        dialog.show();

        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.stateName)
                .collection("Branch")
                .document(Common.selectedSalon.getId())
                .collection("Services")
                .get()
                .addOnFailureListener(e -> iBarberServicesLoadListener.onBarberServicesLoadFailed(e.getMessage()))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<BarberServices> barberServices = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            BarberServices barberServices1 = documentSnapshot.toObject(BarberServices.class);
                            barberServices.add(barberServices1);
                        }

                        iBarberServicesLoadListener.onBarberServicesLoadSuccess(barberServices);
                    }
                });
    }

    private void init() {
        dialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(this)
                .build();

        iBarberServicesLoadListener = this;
        servicesAdded = new HashSet<>();
        inflater = LayoutInflater.from(this);

    }

    private void setCustomerInformation() {
        binding.tvCustomerName.setText(Common.currentBookingInfo.getCustomerName());
        binding.tvCustomerPhone.setText(Common.currentBookingInfo.getCustomerPhone());

        FirebaseFirestore.getInstance()
                .collection("User")
                .whereEqualTo("phoneNumber", Common.currentBookingInfo.getCustomerPhone())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = null;
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            user = documentSnapshot.toObject(User.class);
                        }

                        if (user != null && !user.getAvatar().isEmpty()) {
                            Picasso.get().load(user.getAvatar()).error(R.drawable.user_avatar).into(binding.imgUserAvatar);
                        }
                    }
                });
        //avatar
    }



    @Override
    public void onBarberServicesLoadSuccess(final List<BarberServices> barberServicesList) {
        for (BarberServices barberServices : barberServicesList) {
            if (barberServices.getUid().equals(Common.currentBookingInfo.getBarberServiceList().get(0))) {
                Common.selectedService = barberServices;
            }
        }

        MyServiceAdapter myServiceAdapter = new MyServiceAdapter(this, barberServicesList, Common.currentBookingInfo.getBarberServiceList());
        binding.recyclerServices.setAdapter(myServiceAdapter);



        loadExtraItems();
        dialog.dismiss();
    }

    @Override
    public void onBarberServicesLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    @Override
    public void onShoppingItemSelected(ShoppingItem shoppingItem) {
        //shoppingItemList.add(shoppingItem);

        CartItem cartItem = new CartItem();
        cartItem.setProductId(shoppingItem.getId());
        cartItem.setProductImage(shoppingItem.getImage());
        cartItem.setProductName(shoppingItem.getName());
        cartItem.setProductPrice(shoppingItem.getPrice());
        cartItem.setProductQuantity(1);
        cartItem.setUserPhone(Common.currentBookingInfo.getCustomerPhone());

        if (Common.currentBookingInfo.getCartItemList() == null) {
            Common.currentBookingInfo.setCartItemList(new ArrayList<CartItem>());
        }

        boolean updateQuantityFlag = false;

        for (int i = 0; i < Common.currentBookingInfo.getCartItemList().size(); ++i) {
            CartItem cartItem1 = Common.currentBookingInfo.getCartItemList().get(i);

            if (cartItem1.getProductName().equals(shoppingItem.getName())) {
                updateQuantityFlag = true;
                CartItem updateItem = cartItem1;
                updateItem.setProductQuantity(updateItem.getProductQuantity() + 1);
                Common.currentBookingInfo.getCartItemList().set(i, updateItem);
                break;
            }
        }

        if (!updateQuantityFlag) {
            Common.currentBookingInfo.getCartItemList().add(cartItem);
            Log.d("Shopping", "onShoppingItemSelected: " + Common.currentBookingInfo.getCartItemList().size());

        }

        loadExtraItems();
        myCartAdapter.notifyDataSetChanged();

        Toast.makeText(this, "Added to extra items", Toast.LENGTH_SHORT).show();
    }

    private void loadExtraItems() {
        if (Common.currentBookingInfo.getCartItemList() != null) {
            myCartAdapter = new MyCartAdapter(Common.currentBookingInfo.getCartItemList(), this);
            binding.recyclerCart.setAdapter(myCartAdapter);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap bitmap = null;
            ExifInterface exifInterface = null;

            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
                exifInterface = new ExifInterface(getContentResolver().openInputStream(fileUri));

                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                Bitmap rotateBitmap = null;
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotateBitmap = rotateBitmap(bitmap, 90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotateBitmap = rotateBitmap(bitmap, 180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotateBitmap = rotateBitmap(bitmap, 270);
                        break;
                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        rotateBitmap = bitmap;
                        break;
                }

                binding.imgAddCustomerHair.setVisibility(View.GONE);
                binding.imgCustomerHair.setVisibility(View.VISIBLE);
                binding.imgCustomerHair.setImageBitmap(rotateBitmap);
                binding.btnFinish.setEnabled(true);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDismissBottomSheetDialog(DismissDoneServiceEvent event) {
        if(event.getDismiss())
            finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

package com.qtt.barberstaffapp;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.qtt.barberstaffapp.Common.Common;
import com.qtt.barberstaffapp.databinding.ActivityUpdateProfileBinding;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import dmax.dialog.SpotsDialog;

public class UpdateProfileActivity extends AppCompatActivity {
    ActivityUpdateProfileBinding binding;
    private static final int MY_CAMERA_REQUEST_CODE = 911;
    Uri fileUri;
    AlertDialog dialog;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUpdateProfileBinding.inflate(getLayoutInflater());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");

        dialog = new SpotsDialog.Builder()
                .setCancelable(false)
                .setContext(this)
                .build();

        initView();
        setContentView(binding.getRoot());
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

        binding.edtUserName.setText(Common.currentBarber.getName());
        binding.edtUserAddress.setText(Common.currentBarber.getAddress());
        binding.edtUserPhone.setText(Common.currentBarber.getPhone());


        if (!Common.currentBarber.getAvatar().isEmpty()) {
            Picasso.get().load(Common.currentBarber.getAvatar()).error(R.drawable.user_avatar).into(binding.imgUserAvatar);
        }

        binding.imgAddAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            fileUri = getOutputMediaFileUri();

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(intent, MY_CAMERA_REQUEST_CODE);
        });

        binding.btnUpdate.setOnClickListener(v -> {
            dialog.show();
            if (!Common.currentBarber.getName().equals(binding.edtUserName.getText().toString())) {
                Common.currentBarber.setName(binding.edtUserName.getText().toString());

                FirebaseFirestore.getInstance().collection("AllSalon")
                        .document(Common.stateName)
                        .collection("Branch")
                        .document(Common.selectedSalon.getId())
                        .collection("Barbers")
                        .document(Common.currentBarber.getBarberId())
                        .update("name", binding.edtUserName.getText().toString())
                        .addOnCompleteListener(task13 -> {
                            Log.d("Update_profile", "update name: successfully");
                        }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            if (!Common.currentBarber.getAddress().equals(binding.edtUserAddress.getText().toString())) {
                Common.currentBarber.setAddress(binding.edtUserAddress.getText().toString());

                FirebaseFirestore.getInstance().collection("AllSalon")
                        .document(Common.stateName)
                        .collection("Branch")
                        .document(Common.selectedSalon.getId())
                        .collection("Barbers")
                        .document(Common.currentBarber.getBarberId())
                        .update("address", binding.edtUserAddress.getText().toString())
                        .addOnCompleteListener(task13 -> {
                            Log.d("Update_profile", "update address: successfully");
                        }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            if (fileUri != null || Common.currentBarber.getAvatar().isEmpty()) {
                upLoadPicture(fileUri);
            }

            if (dialog.isShowing()) {
                dialog.dismiss();
            }

            Toast.makeText(this, "Update successfully", Toast.LENGTH_SHORT).show();

            binding.edtUserName.setText(Common.currentBarber.getName());
            binding.edtUserAddress.setText(Common.currentBarber.getAddress());
            binding.edtUserPhone.setText(Common.currentBarber.getPhone());


        });
    }

    private void upLoadPicture(Uri fileUri) {
        if (fileUri != null) {
            dialog.show();

            String fileName = Common.getFileName(getContentResolver(), fileUri);
            String path = new StringBuilder("User_Avatar/").append(fileName).toString();

            storageReference = FirebaseStorage.getInstance().getReference(path);

            UploadTask uploadTask = storageReference.putFile(fileUri);

            Task<Uri> task = uploadTask.continueWithTask(task1 -> {
                if (!task1.isSuccessful()) {
                    Toast.makeText(UpdateProfileActivity.this, "Failed to upload picture!", Toast.LENGTH_SHORT).show();
                }

                return storageReference.getDownloadUrl();

            }).addOnCompleteListener(task12 -> {
                if (task12.isSuccessful()) {
                    String url = task12.getResult().toString().substring(0, task12.getResult().toString().indexOf("&token"));
                    Log.d("AAAAA", "download: " + url);


                    ///AllSalon/Florida/Branch/0n7ikrtgQXW4EXhuJ0qy/Barbers/UyQvnFQSQ45PJ26FuT8L
                    FirebaseFirestore.getInstance().collection("AllSalon")
                            .document(Common.stateName)
                            .collection("Branch")
                            .document(Common.selectedSalon.getId())
                            .collection("Barbers")
                            .document(Common.currentBarber.getBarberId())
                            .update("avatar", url)
                            .addOnCompleteListener(task13 -> {
                                Log.d("Update_profile", "upLoadPicture: successfully");
                                Common.currentBarber.setAvatar(url);
                                Picasso.get().load(Common.currentBarber.getAvatar()).error(R.drawable.user_avatar).into(binding.imgUserAvatar);
                                dialog.dismiss();
                            }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }).addOnFailureListener(e -> {
                dialog.dismiss();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private File getOutputMediaFile() {
        File mediaDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "BarberStaffApp");

        if (!mediaDir.exists()) {
            mediaDir.mkdir();
        }

        String time_tamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaDir.getPath() + File.separator + "IMG_" + time_tamp +
                "_" + new Random().nextInt() + ".jpg");

        return mediaFile;
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

                binding.imgUserAvatar.setImageBitmap(rotateBitmap);

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
}
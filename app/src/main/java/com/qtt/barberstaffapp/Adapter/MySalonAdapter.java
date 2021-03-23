package com.qtt.barberstaffapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.qtt.barberstaffapp.Common.Common;
import com.qtt.barberstaffapp.Common.CustomLoginDialog;
import com.qtt.barberstaffapp.Interface.IDialogClickListener;
import com.qtt.barberstaffapp.Interface.IGetBarberListener;
import com.qtt.barberstaffapp.Interface.IRecyclerItemSelectedListener;
import com.qtt.barberstaffapp.Interface.IUserLoginRememberListener;
import com.qtt.barberstaffapp.Model.Barber;
import com.qtt.barberstaffapp.Model.Salon;
import com.qtt.barberstaffapp.R;
import com.qtt.barberstaffapp.StaffHomeActivity;

import java.util.ArrayList;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MySalonAdapter extends RecyclerView.Adapter<MySalonAdapter.SalonViewHolder> implements IDialogClickListener {

    Context context;
    List<Salon> salonList;
    List<CardView> cardViewList;

    IUserLoginRememberListener iUserLoginRememberListener;
    IGetBarberListener iGetBarberListener;

    public MySalonAdapter(Context context, List<Salon> salonList, IUserLoginRememberListener iUserLoginRememberListener, IGetBarberListener iGetBarberListener) {
        this.context = context;
        this.salonList = salonList;
        this.iGetBarberListener = iGetBarberListener;
        this.iUserLoginRememberListener = iUserLoginRememberListener;

        cardViewList = new ArrayList<>();
    }

    @NonNull
    @Override
    public SalonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.layout_salon, parent, false);
        return new SalonViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SalonViewHolder holder, int position) {
        holder.tvSalonName.setText(salonList.get(position).getName());
        holder.tvSalonAddress.setText(salonList.get(position).getAddress());

        if (!cardViewList.contains(holder.cardViewSalon))
            cardViewList.add(holder.cardViewSalon);

        holder.setiRecyclerItemSelectedListener(new IRecyclerItemSelectedListener() {
            @Override
            public void onItemSelectedListener(View view, int position) {
                Common.selectedSalon = salonList.get(position);

                showLoginDialog();
            }
        });
    }

    private void showLoginDialog() {
        CustomLoginDialog.getInstance()
                .showLoginDialog("STAFF LOGIN",
                        "LOGIN", "CANCEL", context, this);
    }

    @Override
    public int getItemCount() {
        return salonList.size();
    }

    @Override
    public void onClickPositiveButton(final DialogInterface dialogInterface, final String userName, String password) {
        final AlertDialog dialog = new SpotsDialog.Builder().setContext(context).setCancelable(false).build();
        dialog.show();

        ///AllSalon/Florida/Branch/0n7ikrtgQXW4EXhuJ0qy/Barbers/Nsa4hBFukd8UZYMiRe5y
        FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.stateName)
                .collection("Branch")
                .document(Common.selectedSalon.getId())
                .collection("Barbers")
                .limit(1)
                .whereEqualTo("username", userName)
                .whereEqualTo("password", password)
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        dialogInterface.dismiss();
                        
                        dialog.dismiss();

                        iUserLoginRememberListener.onUserLoginSuccess(userName);

                        Barber barber = new Barber();
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            barber = documentSnapshot.toObject(Barber.class);
                            barber.setBarberId(documentSnapshot.getId());
                        }

                        iGetBarberListener.onGetBarberSuccess(barber);

                        Intent intent = new Intent(context, StaffHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);

                    } else {
                        Toast.makeText(context, "Wrong username/ password", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    @Override
    public void onClickNegativeButton(DialogInterface dialogInterface) {
        dialogInterface.dismiss();
    }

    public class SalonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvSalonName, tvSalonAddress;
        CardView cardViewSalon;

        IRecyclerItemSelectedListener iRecyclerItemSelectedListener;

        public void setiRecyclerItemSelectedListener(IRecyclerItemSelectedListener iRecyclerItemSelectedListener) {
            this.iRecyclerItemSelectedListener = iRecyclerItemSelectedListener;
        }

        public SalonViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSalonName = itemView.findViewById(R.id.tv_salon_name);
            tvSalonAddress = itemView.findViewById(R.id.tv_salon_address);
            cardViewSalon = itemView.findViewById(R.id.card_view_salon);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerItemSelectedListener.onItemSelectedListener(v, getAdapterPosition());
        }
    }
}
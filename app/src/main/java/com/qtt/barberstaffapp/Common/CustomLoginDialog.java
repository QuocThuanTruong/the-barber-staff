package com.qtt.barberstaffapp.Common;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.qtt.barberstaffapp.Interface.IDialogClickListener;
import com.qtt.barberstaffapp.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CustomLoginDialog {
    public static CustomLoginDialog customLoginDialog;
    public IDialogClickListener iDialogClickListener;

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.edt_user)
    TextInputEditText edtUser;
    @BindView(R.id.edt_password)
    TextInputEditText edtPassword;
    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_cancel)
    Button btnCancel;

    public static CustomLoginDialog getInstance() {
        if (customLoginDialog == null)
            customLoginDialog = new CustomLoginDialog();
        return customLoginDialog;
    }

    public void showLoginDialog(String title, String positiveText, String negativeText, Context context, final IDialogClickListener iDialogClickListener) {
        this.iDialogClickListener = iDialogClickListener;

        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_login);

        ButterKnife.bind(this, dialog);

        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
            tvTitle.setVisibility(View.VISIBLE);
        } else
            tvTitle.setVisibility(View.GONE);

        btnLogin.setText(positiveText);
        btnCancel.setText(negativeText);

        dialog.setCancelable(false);
        dialog.show();

        Window window = dialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iDialogClickListener.onClickPositiveButton(dialog, edtUser.getText().toString(), edtPassword.getText().toString());
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iDialogClickListener.onClickNegativeButton(dialog);
            }
        });
    }
}

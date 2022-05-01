package com.example.bchainprac.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.example.bchainprac.R;
import com.example.bchainprac.engine.UIEngine;
import com.example.bchainprac.utilities.Utilities;

public class PopUpDialog {
    private static AlertDialog alert;
    private ErrorDialogListener listener;

    public PopUpDialog(ErrorDialogListener listener) {
        this.listener = listener;
    }

    public void showMessageDialog(final String title, final String message, final Activity activity) {
        if (activity == null)
            return;
        UIEngine.initialize(activity.getApplicationContext());
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                final AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                if (!Utilities.isNullString(title))
                    dialog.setTitle(UIEngine.createSpannableString(title, UIEngine.Fonts.APP_FONT_LIGHT));
                dialog.setMessage(UIEngine.createSpannableString(message, UIEngine.Fonts.APP_FONT_LIGHT));
                dialog.setCancelable(false);

                dialog.setPositiveButton(activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onOkClick();
                    }
                });

                dialog.setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (alert != null && alert.isShowing())
                            alert.dismiss();
                        listener.onCancelClick();
                    }
                });

                if (alert != null && alert.isShowing())
                    alert.dismiss();
                alert = dialog.create();
                alert.show();
            }
        });
    }

    public interface ErrorDialogListener {
        void onOkClick();

        void onCancelClick();
    }
}

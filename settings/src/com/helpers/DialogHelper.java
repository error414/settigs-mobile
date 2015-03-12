package com.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.spirit.R;

public class DialogHelper {

    public static void showBankChoiceDialog(Context context, int titleId, final BankChosenListener bankChosenListener) {
        CharSequence[] banksToCompare = new CharSequence[2];
        final int[] banksToCompareValue = new int[banksToCompare.length];
        String[] banks = context.getResources().getStringArray(R.array.bank_values);
        int pos = -1;
        int activeBank = Globals.getInstance().getActiveBank();

        for (int i = 0; i < banks.length; i++) {
            if (activeBank != i) {
                banksToCompareValue[++pos] = i;
                banksToCompare[pos] = banks[i];
            }

        }
        new AlertDialog.Builder(context)
                .setTitle(context.getString(titleId))
                .setSingleChoiceItems(banksToCompare, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        bankChosenListener.onBankChosen(banksToCompareValue[which]);
                    }
                })
                .show()
        ;
    }


    public static interface BankChosenListener {
        void onBankChosen(int bank);
    }
}

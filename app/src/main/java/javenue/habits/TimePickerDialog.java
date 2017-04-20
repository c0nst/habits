package javenue.habits;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

public class TimePickerDialog extends DialogFragment {
    private GoalDialogListener mListener;

    interface GoalDialogListener {
        void onDialogPositive(DialogFragment dialog);
        void onDialogNegative(DialogFragment dialog);
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.dialog_time_picker, null);
        TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.timePicker);

        int alarmTime = getArguments().getInt("alarmTime");

        if (Build.VERSION.SDK_INT >= 23) {
            timePicker.setHour(alarmTime / 60);
            timePicker.setMinute(alarmTime % 60);
        } else {
            timePicker.setCurrentHour(alarmTime / 60);
            timePicker.setCurrentMinute(alarmTime % 60);
        }

        builder.setView(dialogView)
                .setPositiveButton(R.string.time_picker_dialog_done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null)
                            mListener.onDialogPositive(TimePickerDialog.this);
                    }
                })
                .setNegativeButton(R.string.time_picker_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null)
                            mListener.onDialogNegative(TimePickerDialog.this);
                    }
                });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof GoalDialogListener)
            mListener = (GoalDialogListener) context;
    }
}

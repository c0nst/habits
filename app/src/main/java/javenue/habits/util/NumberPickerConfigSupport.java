package javenue.habits.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import javenue.habits.R;

public class NumberPickerConfigSupport extends NumberPicker {

    public NumberPickerConfigSupport(Context context) {
        super(context);
        setDividerColor();
    }

    public NumberPickerConfigSupport(Context context, AttributeSet attrs) {
        super(context, attrs);
        processAttributeSet(attrs);
        setDividerColor();
    }

    public NumberPickerConfigSupport(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        processAttributeSet(attrs);
        setDividerColor();
    }

    private void processAttributeSet(AttributeSet attrs) {
        this.setMinValue(attrs.getAttributeIntValue(null, "min", 0));
        this.setMaxValue(attrs.getAttributeIntValue(null, "max", 0));
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    private void updateView(View view) {
        if(view instanceof EditText){
            ((EditText) view).setTextSize(25);
            ((EditText) view).setTextColor(Color.BLACK);
        }
    }

    private void setDividerColor() {
        try {
            java.lang.reflect.Field pf = NumberPicker.class.getDeclaredField("mSelectionDivider");
            if (pf == null)
                return;

            int color = ContextCompat.getColor(getContext(), R.color.colorPrimary);
            ColorDrawable colorDrawable = new ColorDrawable(color);

            pf.setAccessible(true);
            pf.set(this, colorDrawable);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
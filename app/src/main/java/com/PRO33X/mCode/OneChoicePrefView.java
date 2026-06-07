package com.PRO33X.mCode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.PRO33X.MyPrayers.R;
import java.util.LinkedHashMap;
import java.util.Map;

public class OneChoicePrefView extends LinearLayout {
    /*
     * Oh boy this's a viper's nest
     * this class is for making multi option dialog
     * each has its button and selected is marked and unclickable
     * we manage the preference at the same time
     * the whole approach isn't recommended but it can be used this way
     */
    private TextView titleView;
    private TextView subtitleView;
    private String prefKey;
    private String defaultValue;
    private LinkedHashMap<String, String> options = new LinkedHashMap<>();

    public OneChoicePrefView(Context context) {
        super(context);
        init(context, null);
    }

    public OneChoicePrefView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public OneChoicePrefView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        int p = dp2Px(8);
        setPadding(p, p, p, p);

        titleView = new TextView(context);
        subtitleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        subtitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        subtitleView.setPadding(0, dp2Px(2), 0, 0);
        addView(titleView);
        addView(subtitleView);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OneChoicePrefView);
            String title = a.getString(R.styleable.OneChoicePrefView_title);
            String key = a.getString(R.styleable.OneChoicePrefView_prefKey);
            a.recycle();
            if (title != null) titleView.setText(title);
            if (key != null) prefKey = key;
        }

        TypedValue ripple = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, ripple, true);
        setBackgroundResource(ripple.resourceId);
        setClickable(true);
        setFocusable(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void setPrefKey(String key) {
        this.prefKey = key;
    }

    public void setOptions(LinkedHashMap<String, String> options) {
        this.options = options;
        if (defaultValue == null && !options.isEmpty())
            defaultValue = options.values().iterator().next();
        subtitleView.setText(resolveCurrentLabel());
    }

    public void setOptions(LinkedHashMap<String, String> options, String defaultValue) {
        this.defaultValue = defaultValue;
        setOptions(options);
    }

    private void showDialog() {
        if (prefKey == null || options.isEmpty()) return;

        final Context context = getContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String current = prefs.getString(prefKey, defaultValue);

        LinearLayout wrapper = new LinearLayout(context);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(dp2Px(16), dp2Px(16), dp2Px(16), dp2Px(16));

        ScrollView scroll = new ScrollView(context);
        scroll.addView(wrapper);

        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(scroll)
                .create();

        for (Map.Entry<String, String> entry : options.entrySet()) {
            wrapper.addView(buildOption(dialog, prefs, entry.getKey(), entry.getValue(),
            entry.getValue().equals(current)));
        }

        dialog.show();
    }

    private Button buildOption(final AlertDialog dialog, final SharedPreferences prefs,
            final String label, final String prefValue, boolean selected) {
        Button btn = new Button(getContext());
        btn.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT));
        btn.setText(label);

        if (selected) {
            btn.setBackgroundColor(Color.parseColor("#52B2BF"));
            btn.setClickable(false);
            btn.setFocusable(false);
        } else {
            btn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    prefs.edit().putString(prefKey, prefValue).apply();
                    subtitleView.setText(label);
                    dialog.dismiss();
                }
            });
        }
        return btn;
    }

    private String resolveCurrentLabel() {
        if (options.isEmpty()) return "";
        String saved = (prefKey != null)
                ? PreferenceManager.getDefaultSharedPreferences(getContext()).getString(prefKey, defaultValue)
                : defaultValue;
        for (Map.Entry<String, String> entry : options.entrySet())
            if (entry.getValue().equals(saved)) return entry.getKey();
        return options.keySet().iterator().next();
    }

    private int dp2Px(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
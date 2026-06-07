package com.PRO33X.mCode;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.PRO33X.MyPrayers.R;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class MultiChoicePrefView extends LinearLayout {

    private TextView titleView;
    private TextView subtitleView;
    private String prefKey;
    private String noneSelectedText = "";
    private Set<String> defaultValues = new HashSet<>();
    private LinkedHashMap<String, String> options = new LinkedHashMap<>();

    public MultiChoicePrefView(Context context) {
        super(context);
        init(context, null);
    }

    public MultiChoicePrefView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MultiChoicePrefView(Context context, AttributeSet attrs, int defStyleAttr) {
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
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultiChoicePrefView);
            String title           = a.getString(R.styleable.MultiChoicePrefView_title);
            String key             = a.getString(R.styleable.MultiChoicePrefView_prefKey);
            String noneSelectedTxt = a.getString(R.styleable.MultiChoicePrefView_noneSelectedText);
            a.recycle();
            if (title          != null) titleView.setText(title);
            if (key            != null) prefKey = key;
            if (noneSelectedTxt != null) noneSelectedText = noneSelectedTxt;
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

    public void setNoneSelectedText(String text) {
        this.noneSelectedText = (text != null) ? text : "";
    }

    public void setOptions(LinkedHashMap<String, String> options) {
        setOptions(options, new HashSet<String>());
    }

    public void setOptions(LinkedHashMap<String, String> options, Set<String> defaultValues) {
        this.options = options;
        this.defaultValues = (defaultValues != null) ? defaultValues : new HashSet<String>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        subtitleView.setText(resolveLabelsFor(prefs.getStringSet(prefKey, this.defaultValues)));
    }

    private void showDialog() {
        if (prefKey == null || options.isEmpty()) return;

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final Set<String> pending = new HashSet<>(prefs.getStringSet(prefKey, defaultValues));

        LinearLayout wrapper = new LinearLayout(getContext());
        wrapper.setOrientation(LinearLayout.VERTICAL);
        int pad = dp2Px(16);
        wrapper.setPadding(pad, pad, pad, pad);

        for (Map.Entry<String, String> entry : options.entrySet()) {
            final String label = entry.getKey();
            final String value = entry.getValue();

            CheckBox cb = new CheckBox(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, dp2Px(4), 0, dp2Px(4));
            cb.setLayoutParams(lp);
            cb.setText(label);
            cb.setChecked(pending.contains(value));
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton btn, boolean checked) {
                    if (checked) pending.add(value);
                    else         pending.remove(value);
                }
            });
            wrapper.addView(cb);
        }

        ScrollView scroll = new ScrollView(getContext());
        scroll.addView(wrapper);

        new AlertDialog.Builder(getContext())
                .setTitle(titleView.getText())
                .setView(scroll)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        prefs.edit().putStringSet(prefKey, pending).apply();
                        subtitleView.setText(resolveLabelsFor(pending));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private String resolveLabelsFor(Set<String> values) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            if (values.contains(entry.getValue())) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(entry.getKey());
            }
        }
        return sb.length() > 0 ? sb.toString() : noneSelectedText;
    }

    private int dp2Px(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
}
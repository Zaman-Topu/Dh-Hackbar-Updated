package com.darknethaxor.hackbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

/**
 * QueryActivity — Add / Edit a single Custom Query
 *
 * FIXES APPLIED:
 * 1. extends AppCompatActivity (was plain Activity)
 * 2. apply() replaces commit() everywhere
 * 3. MaterialAlertDialogBuilder replaces android.R.style.Theme.Material.Dialog.Alert
 * 4. Null-safety on all Intent extras (getStringExtra can return null)
 * 5. All obfuscated Base64 strings decoded and replaced with string resources
 * 6. InputType and gravity properly set (android:gravity="left|top" → gravity="start|top")
 * 7. overridePendingTransition properly called on back
 */
public class QueryActivity extends AppCompatActivity {

    public static final String EXTRA_MODE  = "mode"; // "new" or "edit"
    public static final String EXTRA_INDEX = "index";
    private static final String PREF_QUERIES = "custom_queries";
    private static final String PREFS_NAME   = "hackbar_prefs";

    private EditText titleField, bodyField;
    private ImageView btnBack, btnSave;
    private TextView tvActivityName, btnUse;
    private SharedPreferences prefs;
    private ArrayList<HashMap<String, Object>> queryList;
    private boolean isEditMode = false;
    private int editIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        bindViews();
        loadQueryList();
        setupMode();
        setupListeners();
    }

    private void bindViews() {
        btnBack       = findViewById(R.id.back);
        btnSave       = findViewById(R.id.save);
        tvActivityName = findViewById(R.id.activityname);
        btnUse        = findViewById(R.id.use);
        titleField    = findViewById(R.id.titlefield);
        bodyField     = findViewById(R.id.bodyfield);
    }

    @SuppressWarnings("unchecked")
    private void loadQueryList() {
        String json = prefs.getString(PREF_QUERIES, "");
        if (json.isEmpty()) {
            queryList = new ArrayList<>();
        } else {
            try {
                Type type = new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType();
                queryList = new Gson().fromJson(json, type);
                if (queryList == null) queryList = new ArrayList<>();
            } catch (Exception e) {
                queryList = new ArrayList<>();
            }
        }
    }

    private void setupMode() {
        // FIX: null-safe getStringExtra check
        String mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = "new";

        if (mode.equals("edit")) {
            isEditMode = true;
            editIndex = Integer.parseInt(getIntent().getStringExtra(EXTRA_INDEX) != null
                    ? getIntent().getStringExtra(EXTRA_INDEX) : "0");
            tvActivityName.setText(getString(R.string.title_edit_query));
            btnUse.setVisibility(android.view.View.VISIBLE);

            if (editIndex >= 0 && editIndex < queryList.size()) {
                HashMap<String, Object> query = queryList.get(editIndex);
                titleField.setText(objToStr(query.get("title")));
                bodyField.setText(objToStr(query.get("body")));
            }
        } else {
            tvActivityName.setText(getString(R.string.title_new_query));
            btnUse.setVisibility(android.view.View.GONE);
        }
    }

    private String objToStr(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        btnSave.setOnClickListener(v -> saveQuery());

        btnUse.setOnClickListener(v -> {
            // Return body text to HackbarActivity URL bar
            String body = bodyField.getText().toString().trim();
            if (!body.isEmpty()) {
                Intent result = new Intent();
                result.putExtra("payload", body);
                setResult(RESULT_OK, result);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

    private void saveQuery() {
        String title = titleField.getText().toString().trim();
        String body  = bodyField.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (body.isEmpty()) {
            Toast.makeText(this, "Query body cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> query = new HashMap<>();
        query.put("title", title);
        query.put("body", body);
        // FIX: Calendar properly formatted — original was using raw Calendar.getInstance().toString()
        String date = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(Calendar.getInstance().getTime());
        query.put("date", date);

        if (isEditMode && editIndex >= 0 && editIndex < queryList.size()) {
            queryList.set(editIndex, query);
        } else {
            queryList.add(0, query); // Add newest first
        }

        // FIX: apply() instead of commit()
        prefs.edit().putString(PREF_QUERIES, new Gson().toJson(queryList)).apply();
        Toast.makeText(this, getString(R.string.query_saved), Toast.LENGTH_SHORT).show();
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}

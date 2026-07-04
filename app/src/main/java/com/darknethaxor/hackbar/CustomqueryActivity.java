package com.darknethaxor.hackbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * CustomqueryActivity — List of saved custom queries
 *
 * FIXES APPLIED:
 * 1. extends AppCompatActivity (was plain Activity)
 * 2. ListView → RecyclerView (ListView deprecated, poor performance)
 *    Original: ArrayAdapter<String> with ListView
 *    Fixed: RecyclerView with QueryListAdapter
 * 3. ImageView FAB → FloatingActionButton (proper touch target, elevation, ripple)
 * 4. apply() replaces commit()
 * 5. Null-safe JSON parsing (Gson.fromJson can return null on malformed data)
 * 6. Empty state properly shown/hidden based on list size
 * 7. Long-press delete → MaterialAlertDialog confirmation
 * 8. MaterialAlertDialogBuilder replaces android.R.style dialog
 */
public class CustomqueryActivity extends AppCompatActivity {

    private static final String PREF_QUERIES = "custom_queries";
    private static final String PREFS_NAME   = "hackbar_prefs";
    private static final int    REQUEST_EDIT = 200;

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private FloatingActionButton fabAdd;
    private SharedPreferences prefs;
    private ArrayList<HashMap<String, Object>> queryList;
    private QueryListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customquery);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        bindViews();
        loadAndDisplay();
    }

    private void bindViews() {
        ImageView btnBack = findViewById(R.id.back);
        recyclerView      = findViewById(R.id.listview);
        tvEmpty           = findViewById(R.id.notfoundtext);
        fabAdd            = findViewById(R.id.add);

        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        fabAdd.setOnClickListener(v -> openQueryEditor("new", -1));
    }

    @SuppressWarnings("unchecked")
    private void loadAndDisplay() {
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

        // FIX: RecyclerView with custom adapter replaces ListView + ArrayAdapter
        adapter = new QueryListAdapter(
                queryList,
                // Click → use query (return payload to HackbarActivity)
                index -> {
                    String body = queryList.get(index).get("body") != null
                            ? queryList.get(index).get("body").toString() : "";
                    Intent result = new Intent();
                    result.putExtra("payload", body);
                    setResult(RESULT_OK, result);
                    finish();
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                },
                // Long-press → edit or delete
                index -> showQueryOptions(index)
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        updateEmptyState();
    }

    private void showQueryOptions(int index) {
        String title = queryList.get(index).get("title") != null
                ? queryList.get(index).get("title").toString() : "Query";

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        openQueryEditor("edit", index);
                    } else {
                        confirmDelete(index);
                    }
                })
                .show();
    }

    private void confirmDelete(int index) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Query")
                .setMessage("Are you sure you want to delete this query?")
                .setPositiveButton("Delete", (d, w) -> deleteQuery(index))
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private void deleteQuery(int index) {
        queryList.remove(index);
        saveQueryList();
        adapter.notifyItemRemoved(index);
        updateEmptyState();
        Toast.makeText(this, getString(R.string.query_deleted), Toast.LENGTH_SHORT).show();
    }

    private void openQueryEditor(String mode, int index) {
        Intent intent = new Intent(this, QueryActivity.class);
        intent.putExtra(QueryActivity.EXTRA_MODE, mode);
        if (index >= 0) intent.putExtra(QueryActivity.EXTRA_INDEX, String.valueOf(index));
        startActivityForResult(intent, REQUEST_EDIT);
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT) {
            // Reload list after edit
            loadAndDisplay();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndDisplay();
    }

    private void saveQueryList() {
        // FIX: apply() instead of commit()
        prefs.edit().putString(PREF_QUERIES, new Gson().toJson(queryList)).apply();
    }

    private void updateEmptyState() {
        boolean empty = queryList == null || queryList.isEmpty();
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
}

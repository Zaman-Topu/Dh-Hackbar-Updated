package com.darknethaxor.hackbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Map;

/**
 * CustomqueryActivity — Manage Custom Payloads
 * Reverted to original UI (ListView), uses modern SharedPreferences logic.
 */
public class CustomqueryActivity extends AppCompatActivity {

    private static final String PREF_NAME = "custom_queries";
    private SharedPreferences prefs;

    private ImageView btnBack, btnAdd;
    private TextView tvActivityName, tvNotFound;
    private ListView listView;
    
    private ArrayList<String> queryNames = new ArrayList<>();
    private QueryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customquery); // Original layout

        prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        bindViews();
        loadQueries();
    }

    private void bindViews() {
        btnBack = findViewById(R.id.back);
        btnAdd = findViewById(R.id.add);
        tvActivityName = findViewById(R.id.activityname);
        tvNotFound = findViewById(R.id.notfoundtext);
        listView = findViewById(R.id.listview);

        tvActivityName.setText("Custom Queries");
        tvNotFound.setText("No custom queries added yet.\nClick '+' to add one.");

        btnBack.setOnClickListener(v -> finish());
        btnAdd.setOnClickListener(v -> showAddDialog());

        adapter = new QueryAdapter(this, queryNames);
        listView.setAdapter(adapter);
    }

    private void loadQueries() {
        queryNames.clear();
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            queryNames.add(entry.getKey());
        }
        
        if (queryNames.isEmpty()) {
            tvNotFound.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            tvNotFound.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
        adapter.notifyDataSetChanged();
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Custom Query");

        View view = getLayoutInflater().inflate(R.layout.query, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.title);
        EditText etPayload = view.findViewById(R.id.query);
        etName.setHint("Query Name");
        etPayload.setHint("Payload String");

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String payload = etPayload.getText().toString().trim();
            if (!name.isEmpty() && !payload.isEmpty()) {
                prefs.edit().putString(name, payload).apply();
                loadQueries();
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Name and payload cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteQuery(String name) {
        new AlertDialog.Builder(this)
            .setTitle("Delete")
            .setMessage("Are you sure you want to delete '" + name + "'?")
            .setPositiveButton("Yes", (d, w) -> {
                prefs.edit().remove(name).apply();
                loadQueries();
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private class QueryAdapter extends BaseAdapter {
        private final Context context;
        private final ArrayList<String> items;

        QueryAdapter(Context context, ArrayList<String> items) {
            this.context = context;
            this.items = items;
        }
        @Override public int getCount() { return items.size(); }
        @Override public Object getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // We use listitem.xml for simplicity as per original UI
                convertView = LayoutInflater.from(context).inflate(R.layout.listitem, parent, false);
            }
            TextView tv = convertView.findViewById(R.id.text);
            String name = items.get(position);
            String payload = prefs.getString(name, "");
            tv.setText(name + "\n" + payload);
            
            convertView.setOnLongClickListener(v -> {
                deleteQuery(name);
                return true;
            });

            return convertView;
        }
    }
}

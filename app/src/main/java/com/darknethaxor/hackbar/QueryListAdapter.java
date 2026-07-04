package com.darknethaxor.hackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * QueryListAdapter — RecyclerView adapter for Custom Queries list
 *
 * FIXES APPLIED:
 * 1. Replaces deprecated ListView + ArrayAdapter pattern
 * 2. ViewHolder pattern for efficient recycling
 * 3. Click and LongClick callbacks via interfaces (no anonymous inner class leaks)
 */
public class QueryListAdapter extends RecyclerView.Adapter<QueryListAdapter.ViewHolder> {

    public interface OnItemClickListener  { void onClick(int index); }
    public interface OnItemLongClickListener { void onLongClick(int index); }

    private final ArrayList<HashMap<String, Object>> items;
    private final OnItemClickListener clickListener;
    private final OnItemLongClickListener longClickListener;

    public QueryListAdapter(
            ArrayList<HashMap<String, Object>> items,
            OnItemClickListener clickListener,
            OnItemLongClickListener longClickListener) {
        this.items             = items;
        this.clickListener     = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_query, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, Object> item = items.get(position);
        String title = item.get("title") != null ? item.get("title").toString() : "";
        String body  = item.get("body")  != null ? item.get("body").toString()  : "";
        String date  = item.get("date")  != null ? item.get("date").toString()  : "";

        holder.title.setText(title);
        holder.body.setText(body);
        holder.date.setText(date);

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_ID && clickListener != null) {
                clickListener.onClick(pos);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_ID && longClickListener != null) {
                longClickListener.onLongClick(pos);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, body, date;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            body  = itemView.findViewById(R.id.body);
            date  = itemView.findViewById(R.id.datentime);
        }
    }
}

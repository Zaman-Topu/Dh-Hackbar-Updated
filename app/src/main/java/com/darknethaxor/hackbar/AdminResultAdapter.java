package com.darknethaxor.hackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/** RecyclerView adapter for admin panel scan results */
public class AdminResultAdapter extends RecyclerView.Adapter<AdminResultAdapter.ViewHolder> {

    public interface OnUrlClick { void onClick(String url); }

    private final ArrayList<String> urls;
    private final OnUrlClick listener;

    public AdminResultAdapter(ArrayList<String> urls, OnUrlClick listener) {
        this.urls = urls;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_result, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = urls.get(position);
        holder.tvUrl.setText(url);
        holder.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(url); });
    }

    @Override public int getItemCount() { return urls != null ? urls.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUrl;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUrl = itemView.findViewById(R.id.result_url);
        }
    }
}

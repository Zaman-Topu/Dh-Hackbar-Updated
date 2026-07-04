package com.darknethaxor.hackbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * PayloadAdapter — Bottom Sheet RecyclerView for injection payload list
 */
public class PayloadAdapter extends RecyclerView.Adapter<PayloadAdapter.ViewHolder> {

    public interface OnPayloadSelected { void onSelect(String payload); }

    private final String[] payloads;
    private final OnPayloadSelected listener;

    public PayloadAdapter(String[] payloads, OnPayloadSelected listener) {
        this.payloads = payloads;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_payload, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.text.setText(payloads[position]);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSelect(payloads[position]);
        });
    }

    @Override public int getItemCount() { return payloads != null ? payloads.length : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.payload_text);
        }
    }
}

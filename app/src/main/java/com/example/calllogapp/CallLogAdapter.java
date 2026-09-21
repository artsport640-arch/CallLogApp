package com.example.calllogapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CallLogAdapter extends RecyclerView.Adapter<CallLogAdapter.ViewHolder> {

    private List<CallLogItem> callLogList;

    public CallLogAdapter(List<CallLogItem> callLogList) {
        this.callLogList = callLogList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_call_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CallLogItem item = callLogList.get(position);
        
        holder.nameText.setText(item.getName());
        holder.numberText.setText(item.getNumber());
        holder.typeText.setText(item.getCallType());
        holder.dateText.setText(item.getDate());
        holder.durationText.setText(item.getDuration());
        holder.icon.setImageResource(item.getIconRes());

        if (item.getCallType().equals("فائتة")) {
            holder.typeText.setTextColor(0xFFE53935);
        } else if (item.getCallType().equals("واردة")) {
            holder.typeText.setTextColor(0xFF43A047);
        } else if (item.getCallType().equals("صادرة")) {
            holder.typeText.setTextColor(0xFF1E88E5);
        }
    }

    @Override
    public int getItemCount() {
        return callLogList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        TextView numberText;
        TextView typeText;
        TextView dateText;
        TextView durationText;
        ImageView icon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.nameText);
            numberText = itemView.findViewById(R.id.numberText);
            typeText = itemView.findViewById(R.id.typeText);
            dateText = itemView.findViewById(R.id.dateText);
            durationText = itemView.findViewById(R.id.durationText);
            icon = itemView.findViewById(R.id.callIcon);
        }
    }
}

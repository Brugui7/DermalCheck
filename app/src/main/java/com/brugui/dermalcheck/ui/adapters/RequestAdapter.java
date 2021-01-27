package com.brugui.dermalcheck.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.interfaces.OnItemClick;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Status;
import com.brugui.dermalcheck.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private final List<Request> data;
    private OnItemClick listener;

    public RequestAdapter(List<Request> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final View vStatusIndicator;
        private final TextView tvEstimatedProbability;
        private final TextView tvPatientId;
        private final TextView tvCreationDate;
        private final CardView cvContainer;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            this.vStatusIndicator = itemView.findViewById(R.id.vStatusIndicator);
            this.tvEstimatedProbability = itemView.findViewById(R.id.tvEstimatedProbability);
            this.tvPatientId = itemView.findViewById(R.id.tvPatientId);
            this.tvCreationDate = itemView.findViewById(R.id.tvCreationDate);
            this.cvContainer = itemView.findViewById(R.id.cvContainer);
        }
    }

    @NonNull
    @Override
    public RequestAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.request_list_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull RequestAdapter.RequestViewHolder holder, int position) {
        Request request = data.get(position);
        holder.tvEstimatedProbability.setText(request.getEstimatedProbability() + "%");
        holder.tvPatientId.setText(request.getPatientId());
        holder.tvCreationDate.setText(Constants.D_M_Y_H_M.format(request
                .getCreationDate()
                .getTime()
        ));
        if (request.getStatus().equalsIgnoreCase(Status.DIAGNOSED_STATUS_NAME)){
            holder.vStatusIndicator.setBackgroundColor(Color.parseColor("#FF4CAF50"));
        } else {
            holder.vStatusIndicator.setBackgroundColor(Color.parseColor("#FFFF5722"));
        }
        holder.cvContainer.setOnClickListener(view -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }
}

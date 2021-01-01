package com.brugui.dermalcheck.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.brugui.dermalcheck.R;
import com.brugui.dermalcheck.data.model.Request;

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<Request> data;

    public RequestAdapter(List<Request> data) {
        this.data = data;
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        private View vStatusIndicator;
        private TextView tvEstimatedProbability, tvPatientId, tvCreationDate;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            this.vStatusIndicator = itemView.findViewById(R.id.vStatusIndicator);
            this.tvEstimatedProbability = itemView.findViewById(R.id.tvEstimatedProbability);
            this.tvPatientId = itemView.findViewById(R.id.tvPatientId);
            this.tvCreationDate = itemView.findViewById(R.id.tvCreationDate);
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
        holder.tvEstimatedProbability.setText(String.valueOf(request.getEstimatedProbability()));
        holder.tvPatientId.setText(request.getPatientId());
        holder.tvCreationDate.setText(request.getCreationDate().toString()); //TODO format
        //TODO color del indicador
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}

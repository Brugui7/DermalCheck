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
import com.brugui.dermalcheck.data.model.ImageProbability;
import com.brugui.dermalcheck.data.model.Request;
import com.brugui.dermalcheck.data.model.Status;
import com.brugui.dermalcheck.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;

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
        private final DonutProgressView dpvChart;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            this.vStatusIndicator = itemView.findViewById(R.id.vStatusIndicator);
            this.tvEstimatedProbability = itemView.findViewById(R.id.tvEstimatedProbability);
            this.tvPatientId = itemView.findViewById(R.id.tvPatientId);
            this.tvCreationDate = itemView.findViewById(R.id.tvCreationDate);
            this.cvContainer = itemView.findViewById(R.id.cvContainer);
            this.dpvChart = itemView.findViewById(R.id.dpvChart);
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
        float estimatedProbability = (float) request.getEstimatedProbability();
        holder.tvEstimatedProbability.setText(Math.round(estimatedProbability) + "%");
        holder.tvPatientId.setText(request.getPatientId());
        holder.tvCreationDate.setText(Constants.D_M_Y_H_M.format(request
                .getCreationDate()
                .getTime()
        ));

        DonutSection section = new DonutSection("", getChartColor(estimatedProbability), estimatedProbability);
        holder.dpvChart.setCap(100f);
        holder.dpvChart.submitData(new ArrayList<>(Collections.singleton(section)));

        if (request.getStatus().equalsIgnoreCase(Status.DIAGNOSED_STATUS_NAME)) {
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


    /**
     * @param estimatedProbability float
     * @return int color
     */
    private int getChartColor(double estimatedProbability) {
        if (estimatedProbability < 30) {
            return Color.parseColor("#4caf50");
        } else if (estimatedProbability < 70) {
            return Color.parseColor("#ff9800");
        }
        return Color.parseColor("#f44336");
    }
}

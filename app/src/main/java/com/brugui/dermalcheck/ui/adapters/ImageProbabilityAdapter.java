package com.brugui.dermalcheck.ui.adapters;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

public class ImageProbabilityAdapter extends RecyclerView.Adapter<ImageProbabilityAdapter.RequestViewHolder> {

    private final List<ImageProbability> data;
    private int positionSelected = -1;

    public ImageProbabilityAdapter(List<ImageProbability> data, OnItemClick listener) {
        this.data = data;
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;
        private final TextView tvEstimatedProbability, tvLabel;
        private final CardView cvContainer;
        private final DonutProgressView dpvChart;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            this.cvContainer = itemView.findViewById(R.id.cvContainer);
            this.tvEstimatedProbability = itemView.findViewById(R.id.tvEstimatedProbability);
            this.dpvChart = itemView.findViewById(R.id.dpvChart);
            this.ivImage = itemView.findViewById(R.id.ivImage);
            this.tvLabel = itemView.findViewById(R.id.tvLabel);
        }
    }

    @NonNull
    @Override
    public ImageProbabilityAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_probability_list_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ImageProbabilityAdapter.RequestViewHolder holder, int position) {
        ImageProbability imageProbability = data.get(position);
        float estimatedProbability = imageProbability.getEstimatedProbability();
        holder.tvEstimatedProbability.setText(estimatedProbability + "%");
        holder.ivImage.setImageURI(imageProbability.getImageUri());

        DonutSection section = new DonutSection(
                "Prueba",
                getChartColor(estimatedProbability),
                estimatedProbability);
        holder.dpvChart.setCap(100f);
        holder.dpvChart.submitData(new ArrayList<>(Collections.singleton(section)));
        holder.tvLabel.setText(imageProbability.getLabel());

        boolean isSelected = positionSelected == position;
        holder.cvContainer.setCardElevation(isSelected ? 16 : 0);
        holder.cvContainer.setBackgroundColor(isSelected
                ? Color.parseColor("#937ee9")
                : Color.WHITE
        );

        holder.cvContainer.setOnClickListener(view -> {
            positionSelected = position;
            this.notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    private int getChartColor(float estimatedProbability) {

        if (estimatedProbability < 30) {
            return Color.parseColor("#4caf50");
        }
        if (estimatedProbability < 70) {
          return Color.parseColor("#ff9800");
        }
        return Color.parseColor("#f44336");
    }

    public int getPositionSelected() {
        return positionSelected;
    }
}

package com.brugui.dermalcheck.ui.adapters;

import android.graphics.Color;
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
import com.google.android.gms.common.util.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.futured.donut.DonutProgressView;
import app.futured.donut.DonutSection;

/**
 * Every aspect of the data is handled here because there are 5 phototype and these ain't gonna change
 */
public class PhototypeAdapter extends RecyclerView.Adapter<PhototypeAdapter.RequestViewHolder> {

    private final int[] data;
    private int positionSelected;

    public PhototypeAdapter() {
        this.data = new int[]{1, 2, 3, 4, 5};
        this.positionSelected = -1;
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvText;
        private final CardView cvContainer;


        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            this.tvText = itemView.findViewById(R.id.tvText);
            this.cvContainer = itemView.findViewById(R.id.cvContainer);

        }
    }

    @NonNull
    @Override
    public PhototypeAdapter.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.simple_selectable_list_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull PhototypeAdapter.RequestViewHolder holder, int position) {
        holder.tvText.setText(String.valueOf(data[position]));

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
        return data != null ? data.length : 0;
    }

    public int getPositionSelected() {
        return positionSelected;
    }

    public void setPositionSelected(int position) {
        positionSelected = position;
        this.notifyDataSetChanged();
    }
}

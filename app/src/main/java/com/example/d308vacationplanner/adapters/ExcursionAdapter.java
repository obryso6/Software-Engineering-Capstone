package com.example.d308vacationplanner.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.d308vacationplanner.R;
import com.example.d308vacationplanner.UI.ExcursionDetails;
import com.example.d308vacationplanner.entities.Excursion;

import java.util.ArrayList;
import java.util.List;

// This class demonstrates the Adapter pattern and uses inheritance by extending RecyclerView.Adapter.
// Encapsulation is implemented through private fields and public methods to access those fields.

public class ExcursionAdapter extends RecyclerView.Adapter<ExcursionAdapter.ExcursionViewHolder> {

    private final List<Excursion> excursions = new ArrayList<>();
    private final OnExcursionActionListener actionListener;

    public interface OnExcursionActionListener {
        void onExcursionEdit(Excursion excursion);

        void onExcursionDelete(Excursion excursion);
    }

    public ExcursionAdapter(OnExcursionActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ExcursionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item in the RecyclerView
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_excursion, parent, false);
        return new ExcursionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ExcursionViewHolder holder, int position) {
        Excursion currentExcursion = excursions.get(position);
        holder.textViewExcursionName.setText(currentExcursion.getExcursionName());
        holder.textViewExcursionDate.setText(currentExcursion.getExcursionDate());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ExcursionDetails.class);
            intent.putExtra("excursionId", currentExcursion.getExcursionID());
            intent.putExtra("excursionName", currentExcursion.getExcursionName());
            intent.putExtra("excursionDate", currentExcursion.getExcursionDate());
            intent.putExtra("vacationId", currentExcursion.getVacationID());
            v.getContext().startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return excursions.size();
    }

    public void setExcursions(List<Excursion> newExcursions) {
        ExcursionDiffCallback diffCallback = new ExcursionDiffCallback(this.excursions, newExcursions);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.excursions.clear();
        this.excursions.addAll(newExcursions);

        diffResult.dispatchUpdatesTo(this);
    }

    // Make the ViewHolder class private to encapsulate it within the adapter
    public class ExcursionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private final TextView textViewExcursionName;
        private final TextView textViewExcursionDate;

        public ExcursionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewExcursionDate = itemView.findViewById(R.id.textView_excursion_date);
            textViewExcursionName = itemView.findViewById(R.id.textView_excursion_name);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION && actionListener != null) {
                Excursion clickedExcursion = excursions.get(position);
                Intent intent = new Intent(view.getContext(), ExcursionDetails.class);
                intent.putExtra("excursionId", clickedExcursion.getExcursionID());
                intent.putExtra("excursionName", clickedExcursion.getExcursionName());
                intent.putExtra("excursionDate", clickedExcursion.getExcursionDate());
                intent.putExtra("vacationId", clickedExcursion.getVacationID());
                view.getContext().startActivity(intent);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            // Handle delete action when the item is long-clicked
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION && actionListener != null) {
                Excursion clickedExcursion = excursions.get(position);
                actionListener.onExcursionDelete(clickedExcursion);
            }
            return true;
        }
    }

    public static class ExcursionDiffCallback extends DiffUtil.Callback {

        private final List<Excursion> oldList;
        private final List<Excursion> newList;

        public ExcursionDiffCallback(List<Excursion> oldList, List<Excursion> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getExcursionID() == newList.get(newItemPosition).getExcursionID();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Excursion oldExcursion = oldList.get(oldItemPosition);
            Excursion newExcursion = newList.get(newItemPosition);

            // Check for null before calling equals()
            boolean isNameSame = (oldExcursion.getExcursionName() != null && oldExcursion.getExcursionName().equals(newExcursion.getExcursionName()));
            boolean isDateSame = (oldExcursion.getExcursionDate() != null && oldExcursion.getExcursionDate().equals(newExcursion.getExcursionDate()));

            return isNameSame && isDateSame;
        }


    }
}


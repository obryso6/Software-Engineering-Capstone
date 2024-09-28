package com.example.d308vacationplanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.d308vacationplanner.R;
import com.example.d308vacationplanner.UI.VacationDetailsView;
import com.example.d308vacationplanner.entities.Vacation;

import java.util.List;
import java.util.ArrayList;

// This class extends RecyclerView.Adapter to provide an adapter for the vacation list.
// Inheritance is used to extend base adapter functionality, and encapsulation is applied in managing vacation data.

public class VacationAdapter extends RecyclerView.Adapter<VacationAdapter.VacationViewHolder> implements Filterable {

    private final List<Vacation> vacationList;
    private List<Vacation> filteredVacationList;
    private final LayoutInflater inflater;

    public VacationAdapter(Context context, List<Vacation> vacationList) {
        this.vacationList = vacationList;
        this.filteredVacationList = new ArrayList<>(vacationList); // Initialize with all vacations
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public VacationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_vacation, parent, false);
        return new VacationViewHolder(itemView, filteredVacationList);
    }

    @Override
    public void onBindViewHolder(@NonNull VacationViewHolder holder, int position) {
        Vacation currentVacation = filteredVacationList.get(position);
        holder.vacationNameTextView.setText(currentVacation.getVacationName());
        holder.hotelNameTextView.setText(currentVacation.getHotel());
    }

    @Override
    public int getItemCount() {
        return filteredVacationList.size();
    }

    public void setVacations(List<Vacation> vacations) {
        this.vacationList.clear();
        this.vacationList.addAll(vacations);
        this.filteredVacationList = new ArrayList<>(vacations); // Reset the filtered list
        notifyDataSetChanged(); // Full update here is reasonable after setting new data
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    filteredVacationList = new ArrayList<>(vacationList);
                } else {
                    List<Vacation> filteredList = new ArrayList<>();
                    for (Vacation vacation : vacationList) {
                        // Customize the filtering condition based on your needs
                        if (vacation.getVacationName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(vacation);
                        }
                    }
                    filteredVacationList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredVacationList;
                return filterResults;
            }

            @SuppressWarnings("unchecked") // Suppress unchecked cast warning
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredVacationList = (ArrayList<Vacation>) results.values;
                notifyDataSetChanged(); // Notify adapter to refresh the view
            }
        };
    }

    public static class VacationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView vacationNameTextView;
        TextView hotelNameTextView;
        private final List<Vacation> vacationList;

        public VacationViewHolder(View itemView, List<Vacation> vacationList) {
            super(itemView);
            this.vacationList = vacationList;

            vacationNameTextView = itemView.findViewById(R.id.textView_vacation_name);
            hotelNameTextView = itemView.findViewById(R.id.textView_hotel_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getBindingAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Vacation clickedVacation = vacationList.get(position);
                Intent intent = new Intent(view.getContext(), VacationDetailsView.class);
                intent.putExtra("vacationId", clickedVacation.getVacationID());
                view.getContext().startActivity(intent);
            }
        }
    }
}

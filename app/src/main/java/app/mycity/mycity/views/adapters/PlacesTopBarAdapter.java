package app.mycity.mycity.views.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import app.mycity.mycity.R;
import app.mycity.mycity.api.model.PlaceCategory;
import app.mycity.mycity.util.EventBusMessages;

public class PlacesTopBarAdapter extends RecyclerView.Adapter<PlacesTopBarAdapter.ViewHolder> {

    private List<PlaceCategory> placeCategories;
    int selectedItem = 0;

    public PlacesTopBarAdapter(List<PlaceCategory> data) {
        this.placeCategories = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.places_top_bar_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PlaceCategory placeCategoty = placeCategories.get(position);
        holder.myTextView.setText(placeCategoty.getTitle());

        if(position == selectedItem){
            holder.myTextView.setBackgroundResource(R.drawable.places_top_bar_bg_choosen);
            holder.myTextView.setTextColor(Color.WHITE);
        } else {
            holder.myTextView.setBackgroundResource(R.drawable.places_top_bar_bg);
            holder.myTextView.setTextColor(Color.parseColor("#009688"));
        }
    }

    @Override
    public int getItemCount() {
        return placeCategories.size();
    }

    public int getCategoryId() {
        return placeCategories.get(selectedItem).getId();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.text);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedItem = getAdapterPosition();
                    notifyDataSetChanged();
                    EventBus.getDefault().post(new EventBusMessages.SortPlaces(selectedItem));
                }
            });
        }
    }

    public void update(List<PlaceCategory> categoties){
        placeCategories = categoties;
        notifyDataSetChanged();
    }
}
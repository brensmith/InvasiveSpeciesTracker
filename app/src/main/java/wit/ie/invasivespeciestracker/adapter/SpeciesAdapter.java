package wit.ie.invasivespeciestracker.adapter;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import wit.ie.invasivespeciestracker.R;
import wit.ie.invasivespeciestracker.model.Species;

/**
 * Adapter implementation that controls displaying {@link Species} items in {@link android.widget.ListView}
 */
public class SpeciesAdapter extends ArrayAdapter<Species> {

    public SpeciesAdapter(Context context, List<Species> items) {
        super(context, R.layout.row_species_item, items);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // ViewHolder pattern usage
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
                    .inflate(R.layout.row_species_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.titleView = (TextView) convertView.findViewById(R.id.title);
            viewHolder.dateView = (TextView) convertView.findViewById(R.id.date);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Render item into view
        Species item = getItem(position);
        if (item != null) {
            viewHolder.titleView.setText(item.getSpecies());
            viewHolder.dateView.setText(item.getDate());
        }

        return convertView;
    }

    /**
     * ViewHolder pattern is used to optimise views creation process
     */
    private static class ViewHolder {
        private TextView titleView;
        private TextView dateView;
    }
}

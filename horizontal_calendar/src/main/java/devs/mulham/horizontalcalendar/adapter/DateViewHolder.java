package devs.mulham.horizontalcalendar.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import devs.mulham.horizontalcalendar.R;


class DateViewHolder extends RecyclerView.ViewHolder {

    TextView textTop;
    TextView textMiddle;
    TextView textBottom;
    View layoutContent, viewSelect;
    RecyclerView eventsRecyclerView;

    DateViewHolder(View rootView) {
        super(rootView);
        textTop = rootView.findViewById(R.id.hc_text_top);
        textMiddle = rootView.findViewById(R.id.hc_text_middle);
        textBottom = rootView.findViewById(R.id.hc_text_bottom);
        layoutContent = rootView.findViewById(R.id.hc_layoutContent);
        eventsRecyclerView = rootView.findViewById(R.id.hc_events_recyclerView);
        viewSelect = rootView.findViewById(R.id.view_select);
    }
}

package com.adonai.millwright.ui;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adonai.millwright.R;
import com.adonai.millwright.db.entities.Request;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Recycler adapter with requests as items
 */
public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    public interface ClickListener {
        void onClick(Request request);
    }
    
    private List<Request> mRequests;
    private ClickListener mListener;

    public RequestAdapter(List<Request> requests, ClickListener listener) {
        this.mRequests = requests;
        this.mListener = listener;
    }

    /**
     * Создание новых View и ViewHolder элемента списка, которые впоследствии могут переиспользоваться.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_request, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public long getItemId(int position) {
        return mRequests.get(position).getId();
    }

    /**
     * Заполнение виджетов View данными из элемента списка с номером i
     */
    @Override
    @SuppressWarnings("deprecation")
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Request record = mRequests.get(i);
        SimpleDateFormat dFormat = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
        
        
        viewHolder.address.setText(record.getAddress());
        viewHolder.date.setText(dFormat.format(record.getDate()));
        viewHolder.text.setText(record.getCustomText());
        
        switch (record.getUrgency()) {
            case GREEN:
                viewHolder.date.setTextColor(Color.parseColor("#00aa00"));
                break;
            case YELLOW:
                viewHolder.date.setTextColor(Color.parseColor("#aaaa00"));
                break;
            case RED:
                viewHolder.date.setTextColor(Color.RED);
                break;
            default:
                // leave the same
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mRequests.size();
    }

    /**
     * Реализация класса ViewHolder, хранящего ссылки на виджеты.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView address;
        private TextView date;
        private TextView text;
        
        public ViewHolder(View itemView) {
            super(itemView);
            address = (TextView) itemView.findViewById(android.R.id.text1);
            date = (TextView) itemView.findViewById(android.R.id.text2);
            text = (TextView) itemView.findViewById(android.R.id.custom);
            
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Request ours = mRequests.get(getAdapterPosition());
            mListener.onClick(ours);
        }
    }
    
}
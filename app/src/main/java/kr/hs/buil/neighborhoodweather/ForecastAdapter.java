package kr.hs.buil.neighborhoodweather;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by user on 2017-09-16.
 */

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {
    private ArrayList<SimpleForecast> mDataset;

    public ForecastAdapter(ArrayList<SimpleForecast> myDataset) {mDataset = myDataset; }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.tvForeTime.setText(mDataset.get(position).time);
        holder.tvForeSky.setText(mDataset.get(position).sky);
        holder.tvforeTemp.setText(mDataset.get(position).temperature);

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setDataset(ArrayList<SimpleForecast> dataset) {
        this.mDataset = dataset;
    }




    class ViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tvforecTime)
        TextView tvForeTime;
        @BindView(R.id.tvforeSky)
        TextView tvForeSky;
        @BindView(R.id.tvForeTemp)
        TextView tvforeTemp;

        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

}

package kr.hs.buil.neighborhoodweather;

        import android.content.Context;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.BaseAdapter;
        import android.widget.TextView;

        import java.util.ArrayList;

public class ListAdapter_item extends BaseAdapter {

    public class ListItem {
        private String day;
        private String weather;
        private String ondo;

        public void setDay(String day) {
            this.day = day;
        }
        public void setWeather(String weather) {
            this.weather = weather;
        }
        public void setOndo(String ondo) {
            this.ondo = ondo;
        }
        public String getDay() {
            return day;
        }
        public String getWeather() {
            return weather;
        }
        public String getOndo() {
            return ondo;
        }
    }

    ArrayList<ListItem> item = new ArrayList<ListItem>();

    @Override
    public int getCount() {
        return item.size();
    }

    @Override
    public Object getItem(int position) {
        return item.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Context context = parent.getContext();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.custom_list, parent, false);
        }
        TextView day = (TextView) convertView.findViewById(R.id.custon_list_day);
        TextView wea = (TextView) convertView.findViewById(R.id.custon_list_weather);
        TextView ondo =(TextView) convertView.findViewById(R.id.custon_list_ondo);

        ListItem listItem = item.get(position);

        day.setText(listItem.getDay());
        wea.setText(listItem.getWeather());
        ondo.setText(listItem.getOndo());

        return convertView;
    }

    public void addItem(String day, String wea, String ondo) {
        ListItem litem = new ListItem();

        litem.setDay(day);
        litem.setWeather(wea);
        litem.setOndo(ondo);

        item.add(litem);
    }
}
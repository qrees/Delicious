package info.plocharz.safe;

import info.plocharz.safe.db.BaseModel;
import info.plocharz.safe.db.DatabaseHelper;

import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class HttpAdapter extends BaseAdapter {
    
    private int mCachedCount = 0;
    private Map<Integer, Entry> mObjectMap = new HashMap<Integer, Entry>();
    private int mTotal;
    private Context context;
    private LayoutInflater mInflater;
    private DataLoader mDataLoader;

    public HttpAdapter(Context context) {
        this.context = context;
        mInflater = LayoutInflater.from(context);
        mDataLoader = new DataLoader(context);
    }
    
    public int getCount() {
        return this.mTotal;
    }

    public Object getItem(int position) {
        return mObjectMap.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        
        if (convertView == null) { 
            convertView = mInflater.inflate(R.layout.stub_row, null); 
        }
 
        TextView text = (TextView) convertView.findViewById(R.id.textView1);
        text.setTag(position);
        text.setText("Loading...");
        mDataLoader.loadData(position, text);
        return convertView;
    }

    public void setTotal(int total) {
        this.mTotal = total;
    }

}

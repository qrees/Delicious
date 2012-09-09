package info.plocharz.safe;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.plocharz.safe.db.BaseModel;
import info.plocharz.safe.db.DatabaseHelper;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DbAdapter extends BaseAdapter {
    
    protected Map<Integer, BaseModel> object_map = new HashMap<Integer, BaseModel>();
    private LayoutInflater mInflater;
    Context context;

    RuntimeExceptionDao<BaseModel, String> dao = null;
    
    public DbAdapter(Context context, Class klasa) {
        this.context = context;
        DatabaseHelper helper = this.getHelper();
        dao = (RuntimeExceptionDao<BaseModel, String>) helper.getRuntimeExceptionDao(klasa);
        mInflater = LayoutInflater.from(context); 
    }
    
    public void add(BaseModel item) {
        dao.create(item);
        this.notifyDataSetChanged();
    }
    
    private DatabaseHelper databaseHelper = null;
    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper =
                OpenHelperManager.getHelper(this.context, DatabaseHelper.class);
        }
        return databaseHelper;
    }
    
    public int getCount() {
        return (int) dao.countOf();
    }

    public Object getItem(int arg0) {
        QueryBuilder<BaseModel, String> queryBuilder = dao.queryBuilder();
        try {
            if(!object_map.containsKey(arg0)) {
                queryBuilder.offset((long)arg0).limit((long)1);
                PreparedQuery<BaseModel> preparedQuery = queryBuilder.prepare();
                List<BaseModel> accountList = dao.query(preparedQuery);
                if(accountList.size() == 0)
                    return null;
                BaseModel item = accountList.get(0);
                object_map.put(arg0, item);
            };
            return object_map.get(arg0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getItemId(int position) {
        return position;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) { 
         
        Log.i("in getView for position " + position +  
                ", convertView is " + 
                ((convertView == null)?"null":"being recycled")); 

        if (convertView == null) { 
            convertView = mInflater.inflate(android.R.layout.simple_list_item_checked, null); 
        }
        
        BaseModel item = (BaseModel) this.getItem(position);
        TextView text = (TextView) convertView.findViewById(android.R.id.text1);
        text.setText(item.verboseName());
        return convertView; 
    } 
    
    @Override
    public int getViewTypeCount() {
        return 1;
    }
}

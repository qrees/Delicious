package info.plocharz.safe;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;

import info.plocharz.safe.db.BaseModel;
import info.plocharz.safe.db.DatabaseHelper;
import info.plocharz.safe.db.Task;

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
    
    private LayoutInflater mInflater;
    Context context;

    RuntimeExceptionDao<BaseModel, String> dao = null;
    
    public DbAdapter(Context context, Class klasa) {
        this.context = context;
        DatabaseHelper helper = this.getHelper();
        dao = helper.getSimpleDataDao(klasa);
        mInflater = LayoutInflater.from(context); 
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
            queryBuilder.offset((long)arg0).limit((long)1);
            PreparedQuery<BaseModel> preparedQuery = queryBuilder.prepare();
            List<BaseModel> accountList = dao.query(preparedQuery);
            if(accountList.size() == 0)
                return null;
            return accountList.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public long getItemId(int pozition) {
        return pozition;
    }
    
    public View getView(int position, View convertView, ViewGroup parent) { 
         
        Log.i("in getView for position " + position +  
                ", convertView is " + 
                ((convertView == null)?"null":"being recycled")); 

        if (convertView == null) { 
            convertView = mInflater.inflate(android.R.layout.simple_list_item_1, null); 
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

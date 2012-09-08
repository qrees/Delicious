package info.plocharz.safe;

import java.sql.SQLException;

import info.plocharz.safe.db.DatabaseHelper;
import info.plocharz.safe.db.Task;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.ArrayAdapter;
import android.support.v4.app.NavUtils;

public class BrowserActivity extends Activity {
    private SQLiteDatabase db = null;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        
        Intent intent = getIntent();
        String path = intent.getStringExtra(MainActivity.PATH_MESSAGE);
        
        DatabaseHelper helper = this.getHelper();
        RuntimeExceptionDao<Task, String> dao = helper.getRuntimeExceptionDao(Task.class);
        Log.i("task count %d", dao.countOf());
        
        DbAdapter adapter = new DbAdapter(this, Task.class);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
        //        android.R.layout.simple_list_item_1, new String[]{"task 1", "task 2"});
        ListView list = (ListView) findViewById(R.id.taskListView);
        list.setAdapter(adapter);
    }
    
    private DatabaseHelper databaseHelper = null;
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }
    
    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper =
                OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_browser, menu);
        return true;
    }

}

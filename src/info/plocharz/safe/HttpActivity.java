package info.plocharz.safe;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.support.v4.app.NavUtils;

public class HttpActivity extends Activity {
    public static final String LOGIN_PREF = "login";
    public static final String PASSWORD_PREF = "password";
    
    private HttpAdapter mAdapter;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);

        Intent intent = getIntent();
        int total = intent.getIntExtra(MainActivity.TOTAL_MESSAGE, -1);
        
        mAdapter = new HttpAdapter(this);
        mAdapter.setTotal(total);
        
        ListView list = (ListView) findViewById(R.id.postListView);
        list.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_http, menu);
        return true;
    }

    
}

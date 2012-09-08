package info.plocharz.safe;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v4.app.NavUtils;

public class MainActivity extends Activity {
    public final static String PATH_MESSAGE = "info.plocharz.safe.path";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void startBrowser(View view){
        Intent intent = new Intent(this, BrowserActivity.class);
        String path = "safe/data.db";
        intent.putExtra(PATH_MESSAGE, path);
        startActivity(intent);
    }
}

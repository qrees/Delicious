package info.plocharz.safe;

import net.sqlcipher.database.SQLiteDatabase;
import info.plocharz.safe.db.Task;


import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

public class BrowserActivity extends Activity implements OnItemClickListener {
    private TaskAdapter adapter;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SQLiteDatabase.loadLibs(this);
        setContentView(R.layout.activity_browser);
        
        Intent intent = getIntent();
        String path = intent.getStringExtra(MainActivity.PATH_MESSAGE);
        
        adapter = new TaskAdapter(this, Task.class);
        ListView list = (ListView) findViewById(R.id.taskListView);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_browser, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_add:
                showAddDialog();
                return true;
            case R.id.menu_clear:
                clearCompleted();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearCompleted() {
        adapter.clearCompleted();
    }
    
    private void showAddDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Add Task");
        alert.setMessage("Enter task contents");

        // Set an EditText view to get user input 
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                Task task = new Task(value);
                adapter.add(task);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
              
          }
        });

        alert.show();
    }


    private void showEditDialog(final int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        Task task = (Task) this.adapter.getItem(position);
        
        alert.setTitle("Eddit Task");
        alert.setMessage("Enter task contents");

        // Set an EditText view to get user input 
        final EditText input = new EditText(this);
        input.setText(task.verboseName());
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                adapter.setTaskText(position, value);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
              
          }
        });
        alert.show();
    }

    
    public void onItemClick(AdapterView<?> view, View target, int position, long id) {
        this.showEditDialog(position);
    }
}

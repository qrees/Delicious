package info.plocharz.safe;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
    public final static String TOTAL_MESSAGE = "info.plocharz.delicious.total";
    public final static String PATH_MESSAGE = "info.plocharz.safe.path";
    public final static String LOGIN_PREF = "login";
    public final static String PASSWORD_PREF = "password";
    
    private EditText login_input;
    private EditText password_input;
    private TextView error_view;
    private String login;
    private String password;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login_input = (EditText) this.findViewById(R.id.login_input);
        password_input = (EditText) this.findViewById(R.id.password_input);
        error_view = (TextView) this.findViewById(R.id.error_view);
        
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        String login = pref.getString(MainActivity.LOGIN_PREF, null);
        String password = pref.getString(MainActivity.PASSWORD_PREF, null);
        login_input.setText(login);
        password_input.setText(password);
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

    private class LoginTask extends RequestTask {
        protected final String INVALID_DATA_MESSAGE = "Invalid username or password";
        
        private String username;
        private String password;
        
        protected String getUrl(){
            return "/posts/all?results=1";
        }
        
        public LoginTask(Context context, String username, String password){
            super(context);
            this.username = username;
            this.password = password;
        }

        protected Request createRequest(){
            return new Request(context, username, password);
        }

        @Override
        protected void handleError() {
            error_view.setText(this.error_message);
        }
        
        @Override
        protected void handleSuccess() {
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0);
            Editor editor = pref.edit();
            editor.putString(LOGIN_PREF, login);
            editor.putString(PASSWORD_PREF, password);
            editor.commit();
            ((MainActivity) this.context).redirectToList(this.doc);
        }
    }
    
    private void redirectToList(Document doc){
        Element root = doc.getDocumentElement();
        String total_s = root.getAttribute("total");
        int total_i = Integer.parseInt(total_s);
        Intent intent = new Intent(this, HttpActivity.class);
        intent.putExtra(MainActivity.TOTAL_MESSAGE, total_i);
        startActivity(intent);
    }
    
    public void startHttpActivity(View view){
        this.login = login_input.getText().toString();
        this.password = password_input.getText().toString();
        LoginTask task = new LoginTask(this, this.login, this.password);
        task.execute();
    }
}

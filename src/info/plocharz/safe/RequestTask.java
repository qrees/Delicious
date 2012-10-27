package info.plocharz.safe;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

public abstract class RequestTask extends AsyncTask<String, Void, Boolean> {
    
    protected final String INVALID_DATA_MESSAGE = "Invalid data";
    protected final String INTERNAL_ERROR_MESSAGE = "Internal error";
    protected final String ACCESS_DENIED_MESSAGE = "Access denied";
    
    protected Document doc;
    protected String error_message;
    protected Context context;
    protected Request request;
    
    public RequestTask(Context context){
        this.context = context;
    }
    
    abstract protected String getUrl();
    
    protected Request createRequest(){
        return new Request(context);
    }
    
    protected void performRequest(String url) throws IOException {
        this.request.get(url);
    }
    
    @Override
    protected Boolean doInBackground(String... args) {
        this.request = this.createRequest();
        try {
            this.performRequest(this.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
            this.error_message = "Failed to connect to the server";
            return false;
        }
        String data;
        try {
            data = this.request.body();
            Log.i(data);
        } catch (Exception e1) {
            e1.printStackTrace();
            return false;
        }
        try {
            doc = this.request.getXML();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPreExecute() {
        ((Activity) context).setProgressBarIndeterminateVisibility(true);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        ((Activity) context).setProgressBarIndeterminateVisibility(false);
        int status;
        if(result == false){
            this.handleError();
            return;
        }
        
        try {
            status = request.status();
        } catch (Exception e1) {
            e1.printStackTrace();
            this.error_message = INTERNAL_ERROR_MESSAGE;
            this.handleError();
            return;
        }
        
        if (status == 200){
            
        }else
        if(status == 401){
            this.error_message = ACCESS_DENIED_MESSAGE;
            this.handleError();
            return;
        }else{
            this.error_message = INVALID_DATA_MESSAGE;
            this.handleError();
            return;
        }

        this.handleSuccess();
    }

    protected void handleSuccess() {
        
    }

    protected void handleError() {
    }
    
}

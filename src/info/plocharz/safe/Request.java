package info.plocharz.safe;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.SharedPreferences;

public class Request {

    private Context context;
    private String user_name = null;
    private String password = null;
    private DefaultHttpClient http_client;
    private HttpResponse response;
    private String httpHost = null;
    private StringBuilder bodyCache = null;

    public Request() {
        init();
    }
    
    public Request(Context context){
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences("MyPref", 0);
        String login = pref.getString(HttpActivity.LOGIN_PREF, null);
        String password = pref.getString(HttpActivity.PASSWORD_PREF, null);
        this.user_name = login;
        this.password = password;
        this.context = context;
        init();
        if(user_name != null & password != null)
            setCredentials(user_name, password);
    }
    
    public Request(Context context, String user_name, String password) {
        this.user_name = user_name;
        this.password = password;
        this.context = context;
        init();
        setCredentials(user_name, password);
    }

    public void setCredentials(String user_name, String password) {
        this.http_client.getCredentialsProvider().setCredentials(
                new AuthScope(null, -1),
                new UsernamePasswordCredentials(user_name, password));
    }

    private void init() {
        this.httpHost = context.getResources().getString(R.string.HTTP_HOST);
        HttpParams httpParams = new BasicHttpParams();
        this.http_client = new DefaultHttpClient(httpParams);
    }

    public String createAbsoluteURL(String url) {
        Log.i("Host: %s %s", httpHost, url);
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        } else {
            return httpHost + url;
        }
    }

    public HttpResponse get(String url) throws IOException {
        assert bodyCache != null;
        String full_url = createAbsoluteURL(url);
        HttpGet request = new HttpGet(full_url);
        Log.i("Sending GET to %s", full_url);
        this.response = this.http_client.execute(request);
        Log.i("Response code [%d]", this.response.getStatusLine()
                .getStatusCode());
        return this.response;
    }

    public HttpResponse delete(String url) throws IOException {
        String full_url = createAbsoluteURL(url);

        HttpDelete request = new HttpDelete(full_url);
        Log.i("Sending DELETE to %s", full_url);
        this.response = this.http_client.execute(request);
        Log.i("Response code [%d]", this.response.getStatusLine()
                .getStatusCode());
        return this.response;
    }

    public HttpResponse post(String url, Map<String, String> data) {
        String full_url = createAbsoluteURL(url);
        HttpPost httppost = new HttpPost(full_url);

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            Iterator<Entry<String, String>> it = data.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                nameValuePairs.add(new BasicNameValuePair(key, value));
            }

            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            this.response = http_client.execute(httppost);

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this.response;

    }
/*
    public InputStream stream() throws Exception {
        if (this.response == null) {
            throw new Exception(
                    "You need to perform request before checking fetching response body");
        } else {
            InputStream is = this.response.getEntity().getContent();
            return is;
        }
    }
    */
    public String body() throws Exception {
        if (this.response == null)
            throw new Exception(
                    "You need to perform request before checking fetching response body");

        if(this.bodyCache == null){
            bodyCache = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(this.response.getEntity().getContent()));
            String line;
            while ((line = r.readLine()) != null) {
                bodyCache.append(line);
            }
        }
        return bodyCache.toString();
    }

    public int status() throws Exception {
        if (this.response == null) {
            throw new Exception(
                    "You need to perform request before checking status code");
        } else {
            return this.response.getStatusLine().getStatusCode();
        }
    }
    
    public Document getXML() throws Exception {
        String data = this.body();
        InputStream is = new ByteArrayInputStream( data.getBytes( "utf-8" ) );
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(is);
    }
}

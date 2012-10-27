package info.plocharz.safe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.Semaphore;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.view.View;
import android.app.Activity;
import android.widget.TextView;

public class DataLoader {
    private Semaphore mQueueSemaphore = new Semaphore(0);
    private Context mContext;
    
    public DataLoader(Context context){
        mContext = context;
        init();
    }

    public void init() {
        Log.i("Start thread");
        mThread = new DataLoaderThread();
        mThread.start();
    }
    
    private class LazyViewRef {
        public int mPosition;
        public View mConvertView;

        public LazyViewRef(int position, View convertView) {
            mPosition = position;
            mConvertView = convertView;
        }
    }

    Stack<LazyViewRef> lazyrefs = new Stack<LazyViewRef>();
    
    private class UpdateUI implements Runnable {
        private int mStart;
        private int mEnd;
        
        private List<View> mViews;
        private NodeList mPosts;

        public UpdateUI(int start, int end, NodeList posts, List<View> views) {
            mPosts = posts;
            mViews = views;
            mStart = start;
            mEnd = end;
        }

        public void run() {
            int length = mPosts.getLength();
            for(int i = 0; i < length; i++){
                
                View view = mViews.remove(0);
                
                Element node = (Element)mPosts.item(i);
                TextView text = (TextView) view;
                //TextView text = (TextView) view.findViewById(R.id.textView1);
                /*Log.i("%s %s %s %s %s",
                        node.getAttribute("description"),
                        node.getAttribute("href"),
                        node.getAttribute("private"),
                        node.getAttribute("shared"),
                        node.getAttribute("time"));*/
                Integer tag = (Integer)text.getTag();
                if(tag.intValue() != mStart + i)
                    continue;
                text.setText(node.getAttribute("description"));
            }
        }
        
    }
    
    private class DataLoaderThread extends Thread {

        private String getUrl(int count, int offset){
            return String.format("posts/all?results=%d&start=%d", count, offset);
        }
        
        private void load(List<View> views, int start, int end){
            Request request = new Request(mContext);
            Document root;
            String url;
            if(start > end){
                int tmp = end;
                end = start;
                start = tmp;
            }
            url = this.getUrl(end-start+1, start);
            try {
                request.get(url);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            try {
                root = request.getXML();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            NodeList posts = root.getElementsByTagName("post");
            
            ((Activity) mContext).runOnUiThread(new UpdateUI(start, end, posts, views));
            
        }
        
        @Override
        public void run() {
            LazyViewRef lazyviewref, lazyviewref_prev;
            ArrayList<View> views;
            int start, end;
            while (true) {
                views = new ArrayList<View>();
                try {
                    mQueueSemaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                lazyviewref = lazyrefs.pop();
                views.add(lazyviewref.mConvertView);
                end = start = lazyviewref.mPosition;
                
                try {
                    synchronized (this) {
                        this.wait(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                while(mQueueSemaphore.availablePermits() > 0  & Math.abs(end-start) < 30){
                    lazyviewref_prev = lazyrefs.peek();
                    if(Math.abs(lazyviewref_prev.mPosition - end) > 1){
                        break;
                    }
                    try {
                        mQueueSemaphore.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    lazyrefs.pop();
                    Integer tag = (Integer)lazyviewref_prev.mConvertView.getTag();
                    if(tag.intValue() != lazyviewref_prev.mPosition){
                        Log.i("stale view, rejecting");
                        mQueueSemaphore.drainPermits();
                        lazyrefs.clear();
                        break;
                    }
                    end = lazyviewref_prev.mPosition;
                    if(end > start)
                        views.add(lazyviewref_prev.mConvertView);
                    else
                        views.add(0, lazyviewref_prev.mConvertView);
                }
                this.load(views, start, end);
            }
        }
    }
    
    DataLoaderThread mThread;
    
    public void loadData(int position, View convertView) {
        LazyViewRef lazyviewref = new LazyViewRef(position, convertView);
        synchronized (lazyrefs) {
            lazyrefs.push(lazyviewref);
            mQueueSemaphore.release();
        }

    }

}

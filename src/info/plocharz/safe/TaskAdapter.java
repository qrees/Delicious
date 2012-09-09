package info.plocharz.safe;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;

import info.plocharz.safe.db.Task;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

public class TaskAdapter extends DbAdapter {

    public TaskAdapter(Context context, Class klasa) {
        super(context, klasa);
    }

    public void toggle(int position){
        Task task = (Task) this.getItem(position);
        task.toggle();
        dao.createOrUpdate(task);
        this.object_map.remove(position);
        this.notifyDataSetChanged();
    }
    
    public void complete(int position){
        Task task = (Task) this.getItem(position);
        task.complete();
        dao.createOrUpdate(task);
        this.object_map.remove(position);
        this.notifyDataSetChanged();
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        CheckedTextView view = (CheckedTextView) super.getView(position, convertView, parent);
        Task task = (Task) this.getItem(position);
        task.getState();
        Log.i("Refreshing task: %s %s", task.verboseName(), task.getState().toString());
        view.setChecked(!task.getState().equals(Task.State.ACTIVE));
        return view;
    }

}

package info.plocharz.safe;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import info.plocharz.safe.db.BaseModel;
import info.plocharz.safe.db.Task;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class TaskAdapter extends DbAdapter implements OnCheckedChangeListener {
    
    
    public TaskAdapter(Context context, Class klasa) {
        super(context, klasa, R.layout.task_item);
    }

    public void toggle(int position){
        Task task = (Task) this.getItem(position);
        task.toggle();
        dao.createOrUpdate(task);
        this.object_map.remove(position);
        this.notifyDataSetChanged();
    }
    
    @Override
    protected QueryBuilder<BaseModel, String> query() throws SQLException {
        QueryBuilder<BaseModel, String> builder = dao.queryBuilder();
        builder.where().eq(Task.STATE_FIELD_NAME, Task.State.ACTIVE).
                   or().eq(Task.STATE_FIELD_NAME, Task.State.COMPLETE);
        return builder;
    }
    
    public void complete(int position){
        Task task = (Task) this.getItem(position);
        task.complete();
        dao.createOrUpdate(task);
        this.object_map.remove(position);
        this.notifyDataSetChanged();
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewGroup view = (ViewGroup) super.getView(position, convertView, parent);
        view.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        Task task = (Task) this.getItem(position);
        task.getState();
        Log.i("Refreshing task: %s %s", task.verboseName(), task.getState().toString());
        CheckBox check = (CheckBox) view.findViewById(R.id.checkbox);
        check.setChecked(!task.getState().equals(Task.State.ACTIVE));
        check.setOnCheckedChangeListener(this);
        check.setTag(position);
        return view;
    }

    public void clearCompleted() {
        dao.queryForEq("state", Task.State.COMPLETE);
        UpdateBuilder<BaseModel, String> builder = dao.updateBuilder();
        try {
            builder.updateColumnValue(Task.STATE_FIELD_NAME, Task.State.HIDDEN).where().eq(Task.STATE_FIELD_NAME, Task.State.COMPLETE);
            dao.update(builder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.object_map.clear();
        this.notifyDataSetChanged();
    }

    public void onCheckedChanged(CompoundButton check, boolean arg1) {
        int position = (Integer) check.getTag();
        this.toggle(position);
    }

}

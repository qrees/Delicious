package info.plocharz.safe.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.UUID;;

@DatabaseTable(tableName = "task")
public class Task extends BaseModel {
    public static final String STATE_FIELD_NAME = "state";
    
    public enum State {
        ACTIVE, COMPLETE, HIDDEN, DELETED
    }
    
    @DatabaseField(id = true)
    private String uuid;
    
    @DatabaseField
    private String text;
    
    @DatabaseField(columnName = STATE_FIELD_NAME)
    private State state;
    
    private void init() {
        UUID uuid = UUID.randomUUID();
        this.uuid = uuid.toString();
        this.state = State.ACTIVE;
    }
    
    public Task() {
        init();
    }
    
    public Task(String text) {
        this.init();
        this.text = text;
    }
    
    @Override
    public String verboseName(){
        return this.text;
    }
    
    public void complete() {
        this.state = State.COMPLETE;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public void toggle() {
        if(this.state.equals(State.ACTIVE)){
            this.state = State.COMPLETE;
        }else{
            this.state = State.ACTIVE;
        }
    }
    
    public State getState() {
        return this.state;
    }
    
    public String getUuid() {
        return uuid;
    }
}

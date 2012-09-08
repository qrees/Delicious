package info.plocharz.safe.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.UUID;;

@DatabaseTable(tableName = "task")
public class Task extends BaseModel {
    
    @DatabaseField(id = true)
    private String uuid;
    
    @DatabaseField
    private String text;
    
    private void init() {
        UUID uuid = UUID.randomUUID();
        this.uuid = uuid.toString();
    }
    
    public Task() {
        init();
    }
    
    public Task(String text) {
        this.text = text;
    }
    
    @Override
    public String verboseName(){
        return this.text;
    }
}

package info.plocharz.safe.db;

import java.sql.SQLException;
import java.sql.Savepoint;

import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteStatement;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.GeneratedKeyHolder;

/**
 * Database connection for Android.
 * 
 * @author kevingalligan, graywatson
 */
public class AndroidDatabaseConnection implements DatabaseConnection {

    private static Logger logger = LoggerFactory.getLogger(AndroidDatabaseConnection.class);

    private final SQLiteDatabase db;
    private final boolean readWrite;

    public AndroidDatabaseConnection(SQLiteDatabase db, boolean readWrite) {
        this.db = db;
        this.readWrite = readWrite;
        logger.trace("{}: db {} opened, read-write = {}", this, db, readWrite);
    }

    public boolean isAutoCommitSupported() {
        return false;
    }

    public boolean isAutoCommit() throws SQLException {
        try {
            boolean inTransaction = db.inTransaction();
            logger.trace("{}: in transaction is {}", this, inTransaction);
            // You have to explicitly commit your transactions, so this is sort of correct
            return !inTransaction;
        } catch (android.database.SQLException e) {
            throw SqlExceptionUtil.create("problems getting auto-commit from database", e);
        }
    }

    public void setAutoCommit(boolean autoCommit) {
        // always in auto-commit mode
    }

    public Savepoint setSavePoint(String name) throws SQLException {
        try {
            db.beginTransaction();
            logger.trace("{}: save-point set with name {}", this, name);
            return new OurSavePoint(name);
        } catch (android.database.SQLException e) {
            throw SqlExceptionUtil.create("problems beginning transaction " + name, e);
        }
    }

    /**
     * Return whether this connection is read-write or not (real-only).
     */
    public boolean isReadWrite() {
        return readWrite;
    }

    public void commit(Savepoint savepoint) throws SQLException {
        try {
            db.setTransactionSuccessful();
            db.endTransaction();
            if (savepoint == null) {
                logger.trace("{}: transaction is successfuly ended", this);
            } else {
                logger.trace("{}: transaction {} is successfuly ended", this, savepoint.getSavepointName());
            }
        } catch (android.database.SQLException e) {
            throw SqlExceptionUtil.create("problems commiting transaction " + savepoint.getSavepointName(), e);
        }
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        try {
            // no setTransactionSuccessful() means it is a rollback
            db.endTransaction();
            if (savepoint == null) {
                logger.trace("{}: transaction is ended, unsuccessfuly", this);
            } else {
                logger.trace("{}: transaction {} is ended, unsuccessfuly", this, savepoint.getSavepointName());
            }
        } catch (android.database.SQLException e) {
            throw SqlExceptionUtil.create("problems rolling back transaction " + savepoint.getSavepointName(), e);
        }
    }

    public CompiledStatement compileStatement(String statement, StatementType type, FieldType[] argFieldTypes) {
        CompiledStatement stmt = new AndroidCompiledStatement(statement, db, type);
        logger.trace("{}: compiled statement got {}: {}", this, stmt, statement);
        return stmt;
    }

    public CompiledStatement compileStatement(String statement, StatementType type, FieldType[] argFieldTypes,
            int resultFlags) {
        // resultFlags argument is not used in Android-land since the {@link Cursor} is bi-directional.
        return compileStatement(statement, type, argFieldTypes);
    }

    public int insert(String statement, Object[] args, FieldType[] argFieldTypes, GeneratedKeyHolder keyHolder)
            throws SQLException {
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement(statement);
            bindArgs(stmt, args, argFieldTypes);
            long rowId = stmt.executeInsert();
            if (keyHolder != null) {
                keyHolder.addKey(rowId);
            }
            /*
             * I've decided to not do the CHANGES() statement here like we do down below in UPDATE because we know that
             * it worked (since it didn't throw) so we know that 1 is right.
             */
            int result = 1;
            logger.trace("{}: insert statement is compiled and executed, changed {}: {}", this, result, statement);
            return result;
        } catch (android.database.SQLException e) {
            throw SqlExceptionUtil.create("inserting to database failed: " + statement, e);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public int update(String statement, Object[] args, FieldType[] argFieldTypes) throws SQLException {
        return update(statement, args, argFieldTypes, "updated");
    }

    public int delete(String statement, Object[] args, FieldType[] argFieldTypes) throws SQLException {
        // delete is the same as update
        return update(statement, args, argFieldTypes, "deleted");
    }

    public <T> Object queryForOne(String statement, Object[] args, FieldType[] argFieldTypes,
            GenericRowMapper<T> rowMapper, ObjectCache objectCache) throws SQLException {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(statement, toStrings(args));
            AndroidDatabaseResults results = new AndroidDatabaseResults(cursor, objectCache);
            logger.trace("{}: queried for one result: {}", this, statement);
            if (!results.first()) {
                return null;
            } else {
                T first = rowMapper.mapRow(results);
                if (results.next()) {
                    return MORE_THAN_ONE;
                } else {
                    return first;
                }
            }
        } catch (android.database.SQLException e) {
            throw SqlExceptionUtil.create("queryForOne from database failed: " + statement, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public long queryForLong(String statement) throws SQLException {
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement(statement);
            long result = stmt.simpleQueryForLong();
            logger.trace("{}: query for long simple query returned {}: {}", this, result, statement);
            return result;
        } catch (android.database.SQLException e) {
            throw SqlExceptionUtil.create("queryForLong from database failed: " + statement, e);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public long queryForLong(String statement, Object[] args, FieldType[] argFieldTypes) throws SQLException {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(statement, toStrings(args));
            AndroidDatabaseResults results = new AndroidDatabaseResults(cursor, null);
            long result;
            if (results.first()) {
                result = results.getLong(0);
            } else {
                result = 0L;
            }
            logger.trace("{}: query for long raw query returned {}: {}", this, result, statement);
            return result;
        } catch (android.database.SQLException e) {
            throw SqlExceptionUtil.create("queryForLong from database failed: " + statement, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void close() throws SQLException {
        try {
            db.close();
            logger.trace("{}: db {} closed", this, db);
        } catch (android.database.SQLException e) {
            throw SqlExceptionUtil.create("problems closing the database connection", e);
        }
    }

    public boolean isClosed() throws SQLException {
        try {
            boolean isOpen = db.isOpen();
            logger.trace("{}: db {} isOpen returned {}", this, db, isOpen);
            return !isOpen;
        } catch (android.database.SQLException e) {
            throw SqlExceptionUtil.create("problems detecting if the database is closed", e);
        }
    }

    public boolean isTableExists(String tableName) {
        Cursor cursor =
                db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '" + tableName + "'", null);
        boolean result;
        if (cursor != null && cursor.getCount() > 0) {
            result = true;
        } else {
            result = false;
        }
        logger.trace("{}: isTableExists '{}' returned {}", this, tableName, result);
        return result;
    }

    private int update(String statement, Object[] args, FieldType[] argFieldTypes, String label) throws SQLException {
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement(statement);
            bindArgs(stmt, args, argFieldTypes);
            stmt.execute();
        } catch (android.database.SQLException e) {
            throw SqlExceptionUtil.create("updating database failed: " + statement, e);
        } finally {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        }
        int result;
        try {
            stmt = db.compileStatement("SELECT CHANGES()");
            result = (int) stmt.simpleQueryForLong();
        } catch (android.database.SQLException e) {
            // ignore the exception and just return 1
            result = 1;
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        logger.trace("{} statement is compiled and executed, changed {}: {}", label, result, statement);
        return result;
    }

    private void bindArgs(SQLiteStatement stmt, Object[] args, FieldType[] argFieldTypes) throws SQLException {
        if (args == null) {
            return;
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                stmt.bindNull(i + 1);
            } else {
                SqlType sqlType = argFieldTypes[i].getSqlType();
                switch (sqlType) {
                    case STRING :
                    case LONG_STRING :
                    case CHAR :
                        stmt.bindString(i + 1, arg.toString());
                        break;
                    case BOOLEAN :
                    case BYTE :
                    case SHORT :
                    case INTEGER :
                    case LONG :
                        stmt.bindLong(i + 1, ((Number) arg).longValue());
                        break;
                    case FLOAT :
                    case DOUBLE :
                        stmt.bindDouble(i + 1, ((Number) arg).doubleValue());
                        break;
                    case BYTE_ARRAY :
                    case SERIALIZABLE :
                        stmt.bindBlob(i + 1, (byte[]) arg);
                        break;
                    case DATE :
                        // this is mapped to a STRING under Android
                    case BLOB :
                        // this is only for derby serializable
                    case BIG_DECIMAL :
                        // this should be handled as a STRING
                        throw new SQLException("Invalid Android type: " + sqlType);
                    case UNKNOWN :
                    default :
                        throw new SQLException("Unknown sql argument type: " + sqlType);
                }
            }
        }
    }

    private String[] toStrings(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        String[] strings = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                strings[i] = null;
            } else {
                strings[i] = arg.toString();
            }
        }

        return strings;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(super.hashCode());
    }

    private static class OurSavePoint implements Savepoint {

        private String name;

        public OurSavePoint(String name) {
            this.name = name;
        }

        public int getSavepointId() {
            return 0;
        }

        public String getSavepointName() {
            return name;
        }
    }
} 
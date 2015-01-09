package org.gawst.asyncdb;

import java.io.File;

import org.gawst.asyncdb.adapter.UIHandler;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * A {@link org.gawst.asyncdb.DataSource} backed by a {@link android.database.sqlite.SQLiteOpenHelper} storage.
 *
 * @author Created by robUx4 on 12/31/2014.
 */
public class SqliteDataSource<E> extends CursorDataSource<E, Long, Void> {

	private final Context context;
	private final SQLiteOpenHelper db;
	private final String tableName;
	private final String databaseName;

	/**
	 * Constructor. (API v14 minimum)
	 *
	 * @param context                Context used to erase the database file in case it's corrupted.
	 * @param db                     The SQL database used to read/write data.
	 * @param tableName              Name of the SQL table that contains the elements to read.
	 * @param databaseElementHandler Handler to transform {@code Cursor} into {@link E} elements or {@link E} elements to selections.
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public SqliteDataSource(@NonNull Context context, @NonNull SQLiteOpenHelper db, @NonNull String tableName, @NonNull DatabaseElementHandler<E> databaseElementHandler) {
		this(context, db, tableName, db.getDatabaseName(), databaseElementHandler);
	}

	/**
	 * Constructor.
	 *
	 * @param context                Context used to erase the database file in case it's corrupted.
	 * @param db                     The SQL database used to read/write data.
	 * @param tableName              Name of the SQL table that contains the elements to read.
	 * @param databaseName           Name of the database file on disk, in case it's corrupted and needs to be erased.
	 * @param databaseElementHandler Handler to transform {@code Cursor} into {@link E} elements or {@link E} elements to selections.
	 */
	public SqliteDataSource(@NonNull Context context, @NonNull SQLiteOpenHelper db, @NonNull String tableName, @NonNull String databaseName, @NonNull DatabaseElementHandler<E> databaseElementHandler) {
		super(databaseElementHandler);
		this.context = context;
		this.db = db;
		this.tableName = tableName;
		this.databaseName = databaseName;
	}

	@Override
	public Void getDatabaseId() {
		return null;
	}

	public Cursor query(String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		UIHandler.assertNotUIThread();
		return db.getReadableDatabase().query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
	}

	@Override
	public int clearAllData() {
		UIHandler.assertNotUIThread();
		int result = db.getWritableDatabase().delete(tableName, "1", null);
		SQLiteDatabase.releaseMemory();
		return result;
	}

	@Override
	public Long insert(@NonNull ContentValues values) throws RuntimeException {
		long id = db.getWritableDatabase().insertOrThrow(tableName, null, values);
		if (id == -1)
			return null;
		return id;
	}

	@Override
	public int update(@NonNull ContentValues updateValues, String selection, String[] selectionArgs) {
		return db.getWritableDatabase().update(tableName, updateValues, selection, selectionArgs);
	}

	@Override
	public int delete(String selection, String[] selectionArgs) {
		UIHandler.assertNotUIThread();
		return db.getWritableDatabase().delete(tableName, selection, selectionArgs);
	}

	@Override
	public void eraseSource() {
		File corruptedDbFile = context.getDatabasePath(databaseName);
		corruptedDbFile.delete();
	}
}

package st.gaw.db;

import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public abstract class InMemoryDbMap<K, V, H extends Map<K, V>> extends InMemoryDbHelper<Map.Entry<K,V>> {

	protected InMemoryDbMap(Context context, String name, int version) {
		super(context, name, version);
	}

	/**
	 * return the object used to the in-memory elements
	 * @return
	 */
	abstract protected H getMap();
	
	protected void onDataCleared() {}

	@Override
	protected void addCursorInMemory(Cursor c) {
		Map.Entry<K, V> entry = getEntryFromCursor(c);
		if (entry!=null)
			getMap().put(entry.getKey(), entry.getValue());
	}

	protected abstract Map.Entry<K, V> getEntryFromCursor(Cursor c);
	
	/**
	 * the where clause that should be used to update/delete the item
	 * <p> see {@link #getKeySelectArgs(Object)}
	 * @param itemKey the key of the item about to be selected in the database
	 * @return a string for the whereClause in {@link SQLiteDatabase#update(String, ContentValues, String, String[])} or {@link SQLiteDatabase#delete(String, String, String[])}
	 */
	protected abstract String getKeySelectClause(K itemKey);
	/**
	 * the where arguments that should be used to update/delete the item
	 * <p> see {@link #getKeySelectClause(Object)}
	 * @param itemKey the key of the  item about to be selected in the database
	 * @return a string array for the whereArgs in {@link SQLiteDatabase#update(String, ContentValues, String, String[])} or {@link SQLiteDatabase#delete(String, String, String[])}
	 */
	protected abstract String[] getKeySelectArgs(K itemKey);
	
	@Override
	protected final String getItemSelectClause(Entry<K, V> itemToSelect) {
		return getKeySelectClause(itemToSelect.getKey());
	}
	
	@Override
	protected final String[] getItemSelectArgs(Entry<K, V> itemToSelect) {
		return getKeySelectArgs(itemToSelect.getKey());
	}
	
	@Override
	protected void clearDataInMemory() {
		getMap().clear();
		super.clearDataInMemory();
		onDataCleared();
	}

	public V remove(K key) {
		V result = getMap().remove(key);
		if (result!=null)
			scheduleRemoveOperation(new MapEntry<K,V>(key,result));
		return result;
	}
	
	public V put(K key, V value) {
		V result = getMap().put(key, value);
		if (result==null)
			scheduleAddOperation(new MapEntry<K,V>(key, value));
		else
			scheduleUpdateOperation(new MapEntry<K,V>(key, value));
		return result;
	}
	
	public V get(K key) {
		return getMap().get(key);
	}
	
	public boolean containsKey(K key) {
		return getMap().containsKey(key);
	}
	
	public void notifyItemChanged(K key) {
		V value = getMap().get(key);
		if (value!=null)
			scheduleUpdateOperation(new MapEntry<K,V>(key, value));
	}
}
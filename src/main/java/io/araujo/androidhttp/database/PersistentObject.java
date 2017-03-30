package io.araujo.androidhttp.database;

import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Araújo on 27/05/2015.
 */
public class PersistentObject<T> extends SugarRecord<T> {

    private static Map<Class, List> memoryList = new HashMap<Class, List>();

    public PersistentObject() {
        super();
    }

    public static <T extends PersistentObject<?>> T findFirstFromMemory(Class clazz) {
        List<T> list = memoryList.get(clazz);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static <T> List<T> findAllFromMemory(Class clazz) {
        return (List<T>) memoryList.get(clazz);
    }

    public void saveInMemory() {
        getList(getClass()).add(this);
    }

    public static <T extends PersistentObject<?>> void deleteAllFromMemory(Class clazz) {
        getList(clazz).clear();
    }

    // Saves everything from database to memory, of the type specified
    public static <T extends PersistentObject<?>> void migrate(Class clazz) {
        Iterator<T> iterator = findAll(clazz);
        List<T> itemsToSaveInMemory = new ArrayList<T>();
        if (iterator != null) {
            for (Iterator<T> it = iterator; it.hasNext(); ) {
                T item = (it.next());
                item.findExtra();
                itemsToSaveInMemory.add(item);
            }
        }
        deleteAllFromMemory(clazz);
        for (T item : itemsToSaveInMemory) {
            item.saveInMemory();
        }
    }

    private static List getList(Class key) {
        List list = memoryList.get(key);
        if (list == null) {
            list = new ArrayList();
            memoryList.put(key, list);
        }
        return list;
    }

    public static <T> T findFirst(Class clazz) {
        List<T> list = listAll(clazz);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    public static <T> List<T> findAllBest(Class clazz) {
        List<T> list = (List<T>) (List<?>) findAllFromMemory(clazz);
        if (list == null) {
            list = listAll(clazz);
        }
        return list;
    }

    public static <T> T findFirstBest(Class clazz) {
        List list = findAllFromMemory(clazz);
        if (list != null && !list.isEmpty()) {
            return (T) list.get(0);
        } else {
            list = listAll(clazz);
            if (list != null && !list.isEmpty()) {
                return (T) list.get(0);
            }
        }
        return null;
    }

    public void findExtra() {

    }

    public String getIdField() {
        return "";
    }

    public Object getIdValue() {
        return null;
    }
}

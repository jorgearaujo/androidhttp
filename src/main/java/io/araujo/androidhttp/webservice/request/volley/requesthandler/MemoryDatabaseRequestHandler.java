package io.araujo.androidhttp.webservice.request.volley.requesthandler;

import com.google.common.collect.Lists;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.araujo.androidhttp.database.DatabaseTask;
import io.araujo.androidhttp.database.PersistentObject;
import io.araujo.androidhttp.listener.service.BaseServiceListener;
import io.araujo.androidhttp.util.ReflectionUtil;
import io.araujo.androidhttp.webservice.RetrieveFrom;
import io.araujo.androidhttp.webservice.WebService;
import io.araujo.androidhttp.webservice.request.RequestHandler;

/**
 * Created by Araújo on 28/05/2015.
 */
public class MemoryDatabaseRequestHandler<P, L ,E> extends RequestHandler<P, L, E> {

    private boolean isList;
    private Class clazz;
    private Type type;
    private RetrieveFrom from;

    public MemoryDatabaseRequestHandler(RetrieveFrom from) {
        this.from = from;
    }

    @Override
    public <Z extends PersistentObject> void execute(final WebService<P, L, E> webService, P params, final BaseServiceListener<L> listener) {
        new DatabaseTask<Void, L>() {
            @Override
            protected L dbOperation(Void... params) throws SQLException {
                String field = webService.getField();
                Object value = webService.getValue();
                calculateReturnClass(webService.getClass());

                L result;
                if (webService.getField() != null && webService.getValue() != null) {
                    //Look from database
                    if (isList) {
                        List<Z> list = PersistentObject.find((Class) ((ParameterizedType) type).getActualTypeArguments()[0], field + " = ?", String.valueOf(value));
                        for (Z item : list) {
                            item.findExtra();
                        }
                        return (L) list;
                    } else {
                        List<L> list =  (List<L>) PersistentObject.find(clazz, field + " = ?", String.valueOf(value));
                        if (list != null && !list.isEmpty()) {
                            result = list.get(0);
                            ((PersistentObject) result).findExtra();
                            return result;
                        }
                        return null;
                    }
                }
                else if (isList) {
                    result = (L) PersistentObject.findAllFromMemory((Class) ((ParameterizedType) type).getActualTypeArguments()[0]);
                    if (result == null) {
                        List<Z> list = (List<Z>) PersistentObject.listAll((Class)((ParameterizedType) type).getActualTypeArguments()[0]);
                        for (Z item : list) {
                           item.findExtra();
                        }
                        return (L) list;
                    }
                    return result;
                } else {
                    result = (L) PersistentObject.findFirstFromMemory(clazz);
                    if (result == null) {
                        List<L> list = (List<L>) Lists.newArrayList(PersistentObject.findAll(clazz));
                        if (list != null && !list.isEmpty()) {
                            result = list.get(0);
                            ((PersistentObject) result).findExtra();
                            return result;
                        }
                    }
                    return result;
                }
            }

            @Override
            protected void doPostExecute(L result) {
                super.doPostExecute(result);
                listener.onResponse(result, from);
            }
        }.execute();
    }

    protected void calculateReturnClass(Class<?> cl) {
        Class<?> clazz = null;
        Type type = null;

        Type pt = ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[1];
        if (pt instanceof GenericArrayType) {
            type = ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[1];
            isList = true;
            this.type = type;
        } else if (ReflectionUtil.getRawClass(pt).equals(List.class) || ReflectionUtil.getRawClass(pt).equals(Set.class)) {
            type = (ParameterizedType) ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[1];
            isList = true;
            this.type = type;
        } else {
            clazz = (Class<?>) ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[1];
            isList = false;
            this.clazz = clazz;
        }
    }

}

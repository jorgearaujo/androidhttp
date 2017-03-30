package io.araujo.androidhttp.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ReflectionUtil {
	public static final boolean DEBUG = false;

	@SuppressWarnings("unchecked")
	public static HashMap<Class, Boolean> visitedClasses;
	
	private static Boolean isPrimitive(Field field) {
		return field.getType().equals(String.class) || field.getType().equals(Collection.class) || field.getType().equals(Integer.class) || field.getType().equals(Double.class)
				|| field.getType().equals(Boolean.class) || field.getType().isPrimitive() || field.getType().isEnum();
	}

	public static Annotation findAnnotation(Class<?> clazz, Class<?> annotation) {
		for (Annotation ann : clazz.getDeclaredAnnotations()) {
			if (ann.annotationType().equals(annotation)) {
				return ann;
			}
		}
		return null;
	}

	public static Class<?> getRawClass(final Type type) {
		if (Class.class.isInstance(type)) {
			return Class.class.cast(type);
		}
		if (ParameterizedType.class.isInstance(type)) {
			final ParameterizedType parameterizedType = ParameterizedType.class.cast(type);
			return getRawClass(parameterizedType.getRawType());
		} else if (GenericArrayType.class.isInstance(type)) {
			GenericArrayType genericArrayType = GenericArrayType.class.cast(type);
			Class<?> c = getRawClass(genericArrayType.getGenericComponentType());
			return Array.newInstance(c, 0).getClass();
		} else {
			return null;
		}
	}

	public static String compareObject(Object data1, Object data2) {
		return compareObject(data1, data2, new ArrayList<Object>(), true, 0);
	}

	private static void log(int level, String msg) {
//		for (int i = 0; i < level; i++)
//			msg = "   " + msg;
//
//		Log.d("DAO2", msg);
	}

	private static String compareObject(Object data1, Object data2, List<Object> checked, boolean recursive, int level) {
		if (data1 == null && data2 == null) {
			return null;
		}

		if (data1 == null) {
			return "data1 = null";
		}

		if (data2 == null) {
			return "data2 = null";
		}

		log(level, "compareObject " + data1 + ", " + data2 + ", recursive=" + recursive);

		if (checked.contains(data1)) {
			return null;
		}

		checked.add(data1);

		try {
			for (Field field : data1.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				Object childData1 = field.get(data1);
				Object childData2 = field.get(data2);
				field.setAccessible(false);

				if (childData1 == null && childData2 == null) {
					continue;
				}

				if (childData1 == null || childData2 == null) {
					// it's ok that 1 collection is null while the other is empty
					if (field.getType().equals(Collection.class)) {
						if (childData1 == null && ((Collection<?>) childData2).isEmpty()) {
							continue;
						}
						if (childData2 == null && ((Collection<?>) childData1).isEmpty()) {
							continue;
						}
					}

					return "Field " + field + " of childData1=" + childData1 + ", childData2=" + childData2 + ", data1=" + data1 + ", data2=" + data2;
				}

				if (isPrimitive(field)) {
					if (field.getType().equals(Collection.class)) {
						if (recursive == false)
							continue;

						Collection<?> c1 = (Collection<?>) childData1;
						Collection<?> c2 = (Collection<?>) childData2;

						if (c1.size() != c2.size()) {
							return "Collection " + field + " doesn't match";
						}

						for (Object item1 : c1) {
							// No resursive checking
							// if (parentData1 != null) {
							// log(level, "- " + item1.getClass() + ", " + parentData1.getClass());
							// if (item1.getClass() == parentData1.getClass() && compareObject(item1, parentData1, null, null, false, level + 1) == null) {
							// continue;
							// }
							// }

							boolean matchFound = false;

							for (Object item2 : c2) {
								if (matchFound)
									continue;

								String result = compareObject(item1, item2, checked, recursive, level + 1);

								log(level, " Compare " + item1 + " with " + item2 + ": " + result);

								if (result == null) {
									matchFound = true;
									continue;
								}
							}

							if (matchFound == false) {
								return "Collection " + field + " doesn't match, item " + item1 + " missing";
							}
						}
					}

					else if (childData1.equals(childData2) == false) {
						return "Field " + field + " doesn't match";
					}
				} else {
					String result = compareObject(childData1, childData2, checked, recursive, level + 1);

					if (result != null) {
						return result;
					}
				}
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Annotation findAnnotation(Class<?> annotation, Field field) {
		if (field != null && field.getAnnotations() != null) {
			for (Annotation fieldAnnotation : field.getAnnotations()) {
				if (fieldAnnotation.annotationType().equals(annotation)) {
					return fieldAnnotation;
				}
			}
		}
		return null;
	}
}
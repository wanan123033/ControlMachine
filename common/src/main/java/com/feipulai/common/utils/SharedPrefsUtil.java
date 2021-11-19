package com.feipulai.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;

import com.orhanobut.logger.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author bear
 * SharedPrefereces工具类
 */
public class SharedPrefsUtil {

    /**
     * 向SharedPreferences中写入int类型数据
     *
     * @param context 上下文环境
     * @param name    对应的xml文件名称
     * @param key     键
     * @param value   值
     */
    public static void putValue(Context context, String name, String key, int value) {
        Editor sp = getEditor(context, name);
        sp.putInt(key, value);
        sp.commit();
    }

    /**
     * 向SharedPreferences中写入boolean类型的数据
     *
     * @param context 上下文环境
     * @param name    对应的xml文件名称
     * @param key     键
     * @param value   值
     */
    public static void putValue(Context context, String name, String key, boolean value) {
        Editor sp = getEditor(context, name);
        sp.putBoolean(key, value);
        sp.commit();
    }

    /**
     * 向SharedPreferences中写入String类型的数据
     *
     * @param context 上下文环境
     * @param name    对应的xml文件名称
     * @param key     键
     * @param value   值
     */
    public static void putValue(Context context, String name, String key, String value) {
        Editor sp = getEditor(context, name);
        sp.putString(key, value);
        sp.commit();
    }

    /**
     * 向SharedPreferences中写入float类型的数据
     *
     * @param context 上下文环境
     * @param name    对应的xml文件名称
     * @param key     键
     * @param value   值
     */
    public static void putValue(Context context, String name, String key, float value) {
        Editor sp = getEditor(context, name);
        sp.putFloat(key, value);
        sp.commit();
    }

    /**
     * 向SharedPreferences中写入long类型的数据
     *
     * @param context 上下文环境
     * @param name    对应的xml文件名称
     * @param key     键
     * @param value   值
     */
    public static void putValue(Context context, String name, String key, long value) {
        Editor sp = getEditor(context, name);
        sp.putLong(key, value);
        sp.commit();
    }

    /**
     * 从SharedPreferences中读取int类型的数据
     *
     * @param context  上下文环境
     * @param name     对应的xml文件名称
     * @param key      键
     * @param defValue 如果读取不成功则使用默认值
     * @return 返回读取的值
     */
    public static int getValue(Context context, String name, String key, int defValue) {
        SharedPreferences sp = getSharedPreferences(context, name);
        int value = sp.getInt(key, defValue);
        return value;
    }

    /**
     * 从SharedPreferences中读取boolean类型的数据
     *
     * @param context  上下文环境
     * @param name     对应的xml文件名称
     * @param key      键
     * @param defValue 如果读取不成功则使用默认值
     * @return 返回读取的值
     */
    public static boolean getValue(Context context, String name, String key, boolean defValue) {
        SharedPreferences sp = getSharedPreferences(context, name);
        boolean value = sp.getBoolean(key, defValue);
        return value;
    }

    /**
     * 从SharedPreferences中读取String类型的数据
     *
     * @param context  上下文环境
     * @param name     对应的xml文件名称
     * @param key      键
     * @param defValue 如果读取不成功则使用默认值
     * @return 返回读取的值
     */
    public static String getValue(Context context, String name, String key, String defValue) {
        SharedPreferences sp = getSharedPreferences(context, name);
        String value = sp.getString(key, defValue);
        return value;
    }

    /**
     * 从SharedPreferences中读取float类型的数据
     *
     * @param context  上下文环境
     * @param name     对应的xml文件名称
     * @param key      键
     * @param defValue 如果读取不成功则使用默认值
     * @return 返回读取的值
     */
    public static float getValue(Context context, String name, String key, float defValue) {
        SharedPreferences sp = getSharedPreferences(context, name);
        float value = sp.getFloat(key, defValue);
        return value;
    }

    /**
     * 从SharedPreferences中读取long类型的数据
     *
     * @param context  上下文环境
     * @param name     对应的xml文件名称
     * @param key      键
     * @param defValue 如果读取不成功则使用默认值
     * @return 返回读取的值
     */
    public static long getValue(Context context, String name, String key, long defValue) {
        SharedPreferences sp = getSharedPreferences(context, name);
        long value = sp.getLong(key, defValue);
        return value;
    }

    
    
    private static Map<Class<?>,Object> prefMap = new HashMap<>();
    
    
	private static final String SEPARATOR = "#";
    
    //获取Editor实例
    private static Editor getEditor(Context context, String name) {
        return getSharedPreferences(context, name).edit();
    }

    //获取SharedPreferences实例
    private static SharedPreferences getSharedPreferences(Context context, String name) {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    /**
     * 保存一个Bean到{@link SharedPreferences}中，
     * SharedPreferences文件不存在会进行新建操作
     * @return 返回是否保存成功
     */
    public static <T> boolean save(Context context, T t) {
        Editor editor = buildNewEditor(context, t);
        return editor.commit();
    }

    private static <T> SharedPreferences getSharedPreferences(Context context, Class<T> clx) {
        return context.getSharedPreferences(clx.getName(), Context.MODE_PRIVATE);
    }

    /**
     * 保存Bean属性
     */
    private static <T> Editor buildNewEditor(Context context, T t) {
        final Class<?> clx = t.getClass();
        // We should remove all data before save data
        remove(context, clx);
        // Get all data form t
        Map<String, Data> map = new HashMap<>();
        //获取bean属性字段
        buildValuesToMap(clx, t, "", map);

        SharedPreferences sp = getSharedPreferences(context, clx);
        Editor editor = sp.edit();
        // Get all existing key
        Set<String> existKeys = sp.getAll().keySet();
        // Foreach the sava data
        Set<String> keys = map.keySet();
        for (String key : keys) {
            Data data = map.get(key);

            final Class<?> type = data.type;
            final Object value = data.value;
	
	        try{
		        if(value == null){
			        removeKeyFamily(editor,existKeys,key);
		        }else if(type.equals(Byte.class) || type.equals(byte.class)){
			        editor.putInt(key,(Byte)value);
		        }else if(type.equals(Short.class) || type.equals(short.class)){
			        editor.putInt(key,(Short)value);
		        }else if(type.equals(Integer.class) || type.equals(int.class)){
			        editor.putInt(key,(Integer)value);
		        }else if(type.equals(Long.class) || type.equals(long.class)){
			        editor.putLong(key,(Long)value);
		        }else if(type.equals(Float.class) || type.equals(float.class)){
			        editor.putFloat(key,(Float)value);
		        }else if(type.equals(Double.class) || type.equals(double.class)){
			        editor.putString(key,(String.valueOf(value)));
		        }else if(type.equals(Boolean.class) || type.equals(boolean.class)){
			        editor.putBoolean(key,(Boolean)value);
		        }else if(type.equals(Character.class) || type.equals(char.class)){
			        editor.putString(key,value.toString());
		        }else if(type.equals(String.class)){
			        editor.putString(key,value.toString());
		        }else{
			        Logger.d(String.format("Con't support save this type:%s, value:%s, key:%s",type,value,key));
		        }
	        }catch(IllegalArgumentException e){
		        Logger.d("Save error:" + e.getMessage());
	        }
        }
        return editor;
    }

    private static void removeKeyFamily(Editor editor, Set<String> existKeys,
                                        String removeKey) {
        String preFix = removeKey + SEPARATOR;
        for (String str : existKeys) {
            if (str.equals(removeKey) || str.startsWith(preFix))
                editor.remove(str);
        }
    }

    private static class Data {
        Class<?> type;
        Object value;

        Data(Class<?> type, Object value) {
            this.type = type;
            this.value = value;
        }
    }

    /**
     * 清空一个Bean对应的{@link SharedPreferences}
     * 之后调用{@link #loadFormSource(Context, Class)} 都返回NULL
     */
    public static <T> void remove(Context context, Class<T> clx) {
        SharedPreferences sp = getSharedPreferences(context, clx);
        Editor editor = sp.edit();
        editor.clear();
        EditorCompat.getInstance().apply(editor);
    }

    public final static class EditorCompat {

        private static EditorCompat sInstance;

        private static class Helper {
            Helper() {
            }

            public void apply(@NonNull Editor editor) {
                try {
                    editor.apply();
                } catch (AbstractMethodError unused) {
                    // The app injected its own pre-Gingerbread
                    // SharedPreferences.Editor implementation without
                    // an apply method.
                    editor.commit();
                }
            }
        }

        private final Helper mHelper;

        private EditorCompat() {
            mHelper = new Helper();
        }

        public static EditorCompat getInstance() {
            if (sInstance == null) {
                sInstance = new EditorCompat();
            }
            return sInstance;
        }

        public void apply(@NonNull Editor editor) {
            // Note that this redirection is needed to not break the public API chain
            // of getInstance().apply() calls. Otherwise this method could (and should)
            // be static.
            mHelper.apply(editor);
        }
    }

    /**
     * 获取bean字段
     */
    private static <T> void buildValuesToMap(Class<?> clx, T t, String preFix,
                                             Map<String, Data> map) {
        if (clx == null || clx.equals(Object.class) || t == null) {
            return;
        }
        final Field[] fields = clx.getDeclaredFields();
        if (fields == null || fields.length == 0)
            return;
        for (Field field : fields) {
            if (isContSupport(field))
                continue;
            final String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            boolean isAccessible = field.isAccessible();
            if (!isAccessible)
                field.setAccessible(true);
            final Object valus;
            try {
                valus = field.get(t);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
            if (isBasicType(fieldType)) {
                String key = preFix + fieldName;
                if (!map.containsKey(key)) {
                    map.put(key, new Data(fieldType, valus));
                }
            } else {
                buildValuesToMap(fieldType, valus, preFix + fieldName + SEPARATOR, map);
            }
        }
        buildValuesToMap(clx.getSuperclass(), t, preFix, map);
    }

    private static boolean isContSupport(Field field) {
        return (Modifier.isStatic(field.getModifiers())
                || Modifier.isFinal(field.getModifiers())
                || Modifier.isAbstract(field.getModifiers()));
    }

    private static boolean isBasicType(Class<?> clx) {
        return clx.equals(Byte.class) || clx.equals(byte.class)
                || clx.equals(Short.class) || clx.equals(short.class)
                || clx.equals(Integer.class) || clx.equals(int.class)
                || clx.equals(Long.class) || clx.equals(long.class)
                || clx.equals(Float.class) || clx.equals(float.class)
                || clx.equals(Double.class) || clx.equals(double.class)
                || clx.equals(Boolean.class) || clx.equals(boolean.class)
                || clx.equals(Character.class) || clx.equals(char.class)
                || clx.equals(String.class);

    }

    /**
     * 从{@link SharedPreferences}文件中加载数据到Bean中，
     * 如果SharedPreferences文件不存在或者未存储任何信息,新建bean并将内容保存到SharedPreferences中
     * 对于同一个 clx  每次获取的都是相同的对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T loadFormSource(Context context,Class<T> clx){
	    T result = (T)prefMap.get(clx);
	    if(result != null){
		    return result;
	    }
	    SharedPreferences sp = getSharedPreferences(context,clx);
	    startReloadIfChangedUnexpectedly(sp);
	    Set<String> existKeys = sp.getAll().keySet();
	    if(existKeys.size() != 0){
		    //return null;
	        result = (T)buildTargetFromSource(clx,null,"",existKeys,sp);
	    }
	    if(result == null){
		    try{
			    result = clx.newInstance();
			    SharedPrefsUtil.save(context,result);
		    }catch(InstantiationException e){
			    e.printStackTrace();
		    }catch(IllegalAccessException e){
			    e.printStackTrace();
		    }
	    }
	    prefMap.put(clx,result);
	    return result;
    }
	
	private static void startReloadIfChangedUnexpectedly(SharedPreferences sp){
		try{
			Class<?> type = sp.getClass();
			String methodName = "startReloadIfChangedUnexpectedly";
			Method method;
			do{
				method = type.getDeclaredMethod(methodName);
				type = type.getSuperclass();
			}while(type != null && method == null);
			method.setAccessible(true);
			method.invoke(sp);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//@SuppressWarnings("TryWithIdenticalCatches")
    private static <T> Object buildTargetFromSource(Class<T> clx, T target, String preFix,
                                                    Set<String> existKeys, SharedPreferences sp) {
        // Each to Object
        if (clx == null || clx.equals(Object.class)) {
            return target;
        }
        if (target == null) {
            try {
                target = clx.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
        Field[] fields = clx.getDeclaredFields();
        if (fields == null || fields.length == 0)
            return target;
        for (Field field : fields) {
	        if(isContSupport(field)){
		        continue;
	        }
	        final String fieldName = field.getName();
	        Class<?> fieldType = field.getType();
	        field.setAccessible(true);
	        // Build the key
	        String key = preFix + fieldName;
	        // Get target value
	        Object value = null;
	        // 读出SharedPreferences中的值到bean中
	        if(isBasicType(fieldType)){
		        if(existKeys.contains(key)){
                    if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
                        value = (byte) sp.getInt(key, 0);
                    } else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
                        value = (short) sp.getInt(key, 0);
                    } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                        value = sp.getInt(key, 0);
                    } else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                        value = sp.getLong(key, 0);
                    } else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
                        value = sp.getFloat(key, 0);
                    } else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                        String intValue = sp.getString(key, "0.00");
                        if (intValue.equals("0")) {
                            value = 0.0;
                        } else {
                            value = Double.valueOf(sp.getString(key, "0.00"));
                        }
                    } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                        value = sp.getBoolean(key, false);
                    } else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
                        value = sp.getString(key, "").charAt(0);
                    } else if (fieldType.equals(String.class)) {
                        value = sp.getString(key, "");
                    }
                }
            } else {
                value = buildTargetFromSource(fieldType, null, preFix + fieldName + SEPARATOR, existKeys, sp);
            }
            // Set the field value
            if (value != null) {
                try {
                    // 设置属性值
                    field.set(target, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
//                    Logger.d(String.format("Set field error, Key:%s, type:%s, value:%s",
//                            key, fieldType, value));
                }
            } else {
//                Logger.d(String.format("Get field value error, Key:%s, type:%s",
//                        key, fieldType));
            }
        }
        return buildTargetFromSource(clx.getSuperclass(), target, preFix, existKeys, sp);
    }
	
	/**
	 * 初始化设置,初始化之后,loadFormSource不会返回null
	 */
	//public static <T> T initSetting(Context context,Class<T> clz){
	//	T setting = SharedPrefsUtil.loadFormSource(context,clz);
	//	if(setting == null){
	//		try{
	//			setting = clz.newInstance();
	//			SharedPrefsUtil.save(context,setting);
	//		}catch(InstantiationException e){
	//			throw new RuntimeException("InstantiationException");
	//		}catch(IllegalAccessException e){
	//			throw new RuntimeException("IllegalAccessException");
	//		}
	//	}
	//	return setting;
	//}
 
}
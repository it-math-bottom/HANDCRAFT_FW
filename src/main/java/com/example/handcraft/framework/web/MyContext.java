package com.example.handcraft.framework.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class MyContext {

	private static Map<String, Class<?>> TYPES_MAP = new HashMap<String, Class<?>>();
	private static Map<String, Object>   BEANS_MAP = new HashMap<String, Object>();
	
	/**
	 * コンテナへの登録を行う。
	 * ただし、この時点で行うのは型の登録のみで、インスタンス生成はBeanの取得時に行う。
	 * 
	 * @param name
	 * @param type
	 */
	public static void registerType(final String name, final Class<?> type) {
		TYPES_MAP.put(name, type);
	}
	
	/**
	 * Beanの取得を行う。
	 * 引数のBean名で取得可能であれば、Bean格納マップから取得する。
	 * 取得できない場合（オブジェクト登録されていない場合）、インスタンスの生成とマップへの格納も行う。
	 * ※Map#computeIfAbsentにより実現されている
	 * 
	 * @param name
	 * @return
	 */
	public static Object getBean(final String name) {
		// Beanインスタンス未生成時の生成ロジック
		Function<String, Object> mappingFunction = key -> {
			Class<?> type = TYPES_MAP.get(key);
			Objects.requireNonNull(type, name + " not found.");
			try {
				return type.newInstance();
			} catch (InstantiationException | IllegalAccessException ex) {
				throw new RuntimeException(name + " cannot instanciate", ex);
			}
		};
		
		// Beanの取得 or （生成 → 登録 → 取得）
		return BEANS_MAP.computeIfAbsent(name, mappingFunction);
	}
}

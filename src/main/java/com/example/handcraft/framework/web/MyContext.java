package com.example.handcraft.framework.web;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.example.handcraft.framework.web.annotation.MyAutowire;
import com.example.handcraft.framework.web.annotation.MyComponent;

public class MyContext {

	private static final Map<String, Class<?>> TYPES_MAP = new HashMap<String, Class<?>>();
	private static final Map<String, Object>   BEANS_MAP = new HashMap<String, Object>();
	
	private static final Path CLASS_PATH;
	static {
		final URL thisResource = MyContext.class.getResource(
				"/" + MyContext.class.getName().replace('.', '/') + ".class");
		try {
			CLASS_PATH = new File(thisResource.toURI()).toPath()
					.getParent()
					.getParent()
					.getParent()
					.getParent()
					.getParent()
					.getParent();
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
	}
	
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
	 * クラスパス配下の{@link MyComponent}付与クラスを対象に、コンテナの登録を行う。
	 * ただし、この時点で行うのは型の登録のみで、インスタンス生成はBeanの取得時に行う。
	 */
	public static void autoRegister() {
		try (final Stream<Path> filesStream = Files.walk(CLASS_PATH)) {
			filesStream
				.filter(path -> !Files.isDirectory(path))                      // ファイルのみを対象とする
				.filter(path -> path.toString().endsWith(".class"))            // .classのみを対象とする
				.map(path -> CLASS_PATH.relativize(path))                       // クラスパスとの相対パスに変換する
				.map(path -> path.toString().replace(File.separatorChar, '.')) // セパレータを '.' にする
				.map(name -> StringUtils.substringBeforeLast(name, ".class"))  // 拡張子.classを除外する（FQCNになる）
				.map(name -> {
					try {
						return Class.forName(name);
					} catch (ClassNotFoundException  ex) {
						throw new RuntimeException(ex);
					}})                                                         // FQCNを元にクラス情報に変換する
				.filter(clazz -> clazz.isAnnotationPresent(MyComponent.class))
				.forEach(clazz -> registerType(clazz.getName(), clazz));
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
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
		final Function<String, Object> mappingFunction = key -> {
			final Class<?> type = TYPES_MAP.get(key);
			Objects.requireNonNull(type, name + " not found.");
			try {
				return createObject(type);
			} catch (InstantiationException | IllegalAccessException ex) {
				throw new RuntimeException(name + " cannot instanciate", ex);
			}
		};
		
		// Beanの取得 or （生成 → 登録 → 取得）
		return BEANS_MAP.computeIfAbsent(name, mappingFunction);
	}

	/**
	 * Beanインスタンス未生成時に呼び出されるオブジェクト生成処理
	 * 
	 * @param <T>
	 * @param type
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private static <T> T createObject(final Class<T> type)
			throws InstantiationException, IllegalAccessException
	{
		final T obj = type.newInstance();
		
		// 対象クラスのフィールドを走査し、MyAutowireアノテーションが付与されているか否かを調べる
		// 付与されていれば、フィールドのBeanを取得し、値セットする
		for (Field field : type.getDeclaredFields()) {
			if (!field.isAnnotationPresent(MyAutowire.class)) {
				continue;
			}
			
			field.setAccessible(true);
			field.set(obj, getBean(field.getClass().getName()));
		}
		
		return obj;
	}
}

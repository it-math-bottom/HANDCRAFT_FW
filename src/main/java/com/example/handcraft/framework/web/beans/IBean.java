package com.example.handcraft.framework.web.beans;

public interface IBean<T extends IBean<T>> {

	default void showMyName() {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) getClass();
		System.out.println("このクラスの名前は ★" + clazz.getName() + "★ です。");
	}
}

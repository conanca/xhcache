package com.dolplay.xhcache.assets.lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Lang {
	/**
	 * 较方便的创建一个列表，比如：
	 * 
	 * <pre>
	 * List&lt;Pet&gt; pets = Lang.list(pet1, pet2, pet3);
	 * </pre>
	 * 
	 * 注，这里的 List，是 ArrayList 的实例
	 * 
	 * @param eles
	 *            可变参数
	 * @return 列表对象
	 */
	public static <T> ArrayList<T> list(T... eles) {
		ArrayList<T> list = new ArrayList<T>(eles.length);
		for (T ele : eles)
			list.add(ele);
		return list;
	}

	/**
	 * 创建一个 Hash 集合
	 * 
	 * @param eles
	 *            可变参数
	 * @return 集合对象
	 */
	public static <T> Set<T> set(T... eles) {
		Set<T> set = new HashSet<T>();
		for (T ele : eles)
			set.add(ele);
		return set;
	}
}

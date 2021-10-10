package client.util;

import client.domain.ClassResult;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能: 排序方法工厂类
 * @author unknown100name
 * @date 2021.10.06
 */
public class ComparatorFactory {

    private static final Map<String, Comparator<ClassResult>> comparatorMap = new HashMap<>();

    public static Comparator<ClassResult> getComparator(String order){
        return comparatorMap.get(order);
    }

    static {
        comparatorMap.put("alpha", new AlphaComparator());
        comparatorMap.put("count", new CountComparator());
        comparatorMap.put(null, new DefaultComparator());
        comparatorMap.put(null, new DefaultComparator());

    }

    static class AlphaComparator implements Comparator<ClassResult> {

        @Override
        public int compare(ClassResult o1, ClassResult o2) {
            if (o1.getClassName().compareTo(o2.getClassName()) == 0){
                return o1.getCount().compareTo(o2.getCount());
            }
            return o1.getClassName().compareTo(o2.getClassName());
        }
    }

    static class CountComparator implements Comparator<ClassResult> {

        @Override
        public int compare(ClassResult o1, ClassResult o2) {
            if (o1.getCount().compareTo(o2.getCount()) == 0){
                return o1.getClassName().compareTo(o2.getClassName());
            }
            return o1.getCount().compareTo(o2.getCount());
        }
    }


    static class DefaultComparator implements Comparator<ClassResult> {
        @Override
        public int compare(ClassResult o1, ClassResult o2) {
            return 0;
        }
    }

}

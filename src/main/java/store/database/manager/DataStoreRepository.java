package store.database.manager;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import store.database.entity.ClassEntity;

import java.util.List;

/**
 * 功能: 数据库连接层
 * @author unknown100name
 * @date 2021.10.06
 */
@Repository
public interface DataStoreRepository extends JpaRepository<ClassEntity, Long> {

    /**
     *  查询 className 对应的信息
     * @param className className
     * @return ClassEntity
     */
    ClassEntity getByClassNameEquals(String className);

    /**
     * 查询 ClassName 模糊匹配且最大值和最小值包含区间的信息
     * @param min 调用次数最小值
     * @param max 调用次数最大值
     * @param className className
     * @return ClassEntity List
     */
    List<ClassEntity> getByCountBetweenAndClassNameLike(long min, long max, String className);

    /**
     * 批量查询 classname 对应的信息
     * @param classNameList className List
     * @return ClassEntity List
     */
    List<ClassEntity> getByClassNameIn(List<String> classNameList);
}

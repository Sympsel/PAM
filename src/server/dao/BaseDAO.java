package server.dao;

import java.util.List;

/**
 * 基础 DAO 接口
 * 定义通用的 CRUD 操作
 *
 * @param <T> 实体类型
 * @param <K> 主键类型
 */
public interface BaseDAO<T, K> {

    /**
     * 保存实体
     *
     * @param entity 要保存的实体
     * @return 是否保存成功
     */
    boolean save(T entity);

    /**
     * 根据 ID 查询实体
     *
     * @param id 实体 ID
     * @return 实体对象，不存在则返回 null
     */
    T findById(K id);

    /**
     * 查询所有实体
     *
     * @return 实体列表
     */
    List<T> findAll();

    /**
     * 更新实体
     *
     * @param entity 要更新的实体
     * @return 是否更新成功
     */
    boolean update(T entity);

    /**
     * 删除实体
     *
     * @param id 实体 ID
     * @return 是否删除成功
     */
    boolean delete(K id);

    /**
     * 统计实体数量
     *
     * @return 实体总数
     */
    int count();
}

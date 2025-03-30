package ru.msu.cmc.webapp.DAO;

import ru.msu.cmc.webapp.entities.CommonEntity;

import java.util.Collection;

public interface CommonDAO<T extends CommonEntity<ID>, ID> {
    T getById(ID id);
    Collection<T> getAll();
    void save(T entity);
    void saveCollection(Collection<T> entities);
    void delete(T entity);
    void deleteById(ID id);
    void update(T entity);
    String likeExpr(String param);
}

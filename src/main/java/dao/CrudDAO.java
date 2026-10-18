package dao;

import util.DatabaseException;

import java.util.List;

/** Interface (syllabus): the common operations every DAO offers. */
public interface CrudDAO<T> {

    void insert(T item) throws DatabaseException;

    void delete(int id) throws DatabaseException;

    T findById(int id) throws DatabaseException;

    List<T> findAll() throws DatabaseException;

    /** Default method: DAOs that cannot update (like results) simply don't override it. */
    default void update(T item) throws DatabaseException {
        throw new UnsupportedOperationException("Update is not supported here");
    }
}

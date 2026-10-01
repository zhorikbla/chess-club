package ru.mirea.project.repository;

import java.sql.SQLException;
import java.util.List;

public interface CrudRepository<T>{
    List<T> findAll() throws SQLException;
    T findById(long id) throws SQLException;
    boolean deleteById(long id) throws SQLException;
}
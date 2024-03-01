package ru.starkov.dao;

import java.util.List;

/**
 * A generic interface representing basic CRUD (Create, Read, Update, Delete) operations for a
 * repository of entities.
 *
 * @param <T> the type of entity managed by this repository
 */
public interface CrudRepository<T> {

  T save(T t);

  void update(T t);

  List<T> findAll();
}

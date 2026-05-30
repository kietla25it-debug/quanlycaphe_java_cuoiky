package auroracafe.repository;

import java.util.List;

/** Repository chuẩn theo yêu cầu đồ án: UI không làm việc trực tiếp với SQL. */
public interface Repository<T> {
    List<T> findAll();
    T findById(int id);
    boolean insert(T item);
    boolean update(T item);
    boolean delete(int id);
    List<T> search(String keyword);
}

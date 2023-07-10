package by.sadovnick.dao;

import by.sadovnick.entity.RowData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RowDataDao extends JpaRepository<RowData, Long> {
}

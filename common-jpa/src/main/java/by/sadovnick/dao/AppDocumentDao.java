package by.sadovnick.dao;

import by.sadovnick.entity.AppDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppDocumentDao  extends JpaRepository<AppDocument, Long> {
}

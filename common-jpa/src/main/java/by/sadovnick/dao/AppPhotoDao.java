package by.sadovnick.dao;

import by.sadovnick.entity.AppPhoto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppPhotoDao extends JpaRepository<AppPhoto, Long> {
}

package by.sadovnick.service.impl;

import by.sadovnick.CryptoTool;
import by.sadovnick.dao.AppUserDao;
import by.sadovnick.entity.AppUser;
import by.sadovnick.service.UserActivationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserActivationServiceImpl implements UserActivationService {
    private final AppUserDao appUserDao;
    private final CryptoTool cryptoTool;

    public UserActivationServiceImpl(AppUserDao appUserDao, CryptoTool cryptoTool) {
        this.appUserDao = appUserDao;
        this.cryptoTool = cryptoTool;
    }

    /**
     * Активация пользователя. Достаем пользователя из бд и если он там есть
     * стаавим ему флаг true
     */
    @Override
    public boolean activation(String cryptoUserId) {
        Long userId = cryptoTool.idOf(cryptoUserId);
        Optional<AppUser> optional = appUserDao.findById(userId);
        if (optional.isPresent()) {
            AppUser user = optional.get();
            user.setIsActive(true);
            appUserDao.save(user);
            return true;
        }
        return false;
    }
}

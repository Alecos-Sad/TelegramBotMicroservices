package by.sadovnick.service;

import by.sadovnick.entity.AppUser;

/**
 * Регистрация пользователя
 */
public interface AppUserService {
    String registerUser(AppUser appUser);
    String setEmail(AppUser appUser, String email);
}

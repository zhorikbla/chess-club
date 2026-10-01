package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;
import ru.mirea.project.repository.UserRepository;

import java.sql.SQLException;
import java.util.List;

public class UserService{
    private final UserRepository userRepository;
    public UserService(
        UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }
    public List<User> findAll() throws SQLException {
            return userRepository.findAll();
    }
    public User findById(long id) throws SQLException {
        User user = userRepository.findById(id);
        if(user==null){
            throw new EntityNotFoundException(
                "Пользователь",
                id
            );
        }
        return user;
    }
    public User findByEmail(String email) throws SQLException {
        if (email == null || email.isBlank()){
            throw new BusinessException(
                "Email не должен бть пустым"
            );
        }
        return userRepository.findByEmail(
            email.trim()
        );
    }
    public User create(
        String fullName,
        String email,
        UserRole role,
        int rating
    ) throws SQLException {
        validate(fullName, email, role, rating);

        User existingUser =
            userRepository.findByEmail(email.trim());

        if (existingUser != null) {
            throw new BusinessException(
            "Пользователь с таким email уже существует"
            );
        }

        return userRepository.create(
            fullName,
            email,
            role,
            rating
        );
    }
    public void update(
        long id,
        String fullName,
        String email,
        UserRole role,
        int rating
    ) throws SQLException {
        findById(id);
        validate(fullName, email, role, rating);
        User userWithSameEmail = userRepository.findByEmail(email);
        if (userWithSameEmail!=null && userWithSameEmail.getId()!=id){
            throw new BusinessException(
                "Email используется другим пользователем"
            );
        }
        boolean updated = userRepository.update(
            id,
            fullName,
            email,
            role,
            rating
        );
        if (!updated){
            throw new EntityNotFoundException(
                "Пользователь",
                id
            );
        }
    }
    public void delete(
        long id,
        long currentUserId
    ) throws SQLException {
        if (id==currentUserId){
            throw new BusinessException(
                "Нельзая удалить текущего пользователя"
            );
        }
        findById(id);

        boolean deleted=userRepository.deleteById(id);

        if(!deleted){
            throw new EntityNotFoundException("Пользователь", id);
        }
    }
    private void validate(
        String fullName,
        String email,
        UserRole role,
        int rating
    ) {
        if (fullName == null || fullName.isBlank()){
            throw new BusinessException(
                "ФИО обязательно"
            );
        }
        if (email == null || email.isBlank() || !email.contains("@")){
            throw new BusinessException("Некорректный email");
        }
        if (role == null){
            throw new BusinessException("Роль обязательная");
        }
        if (rating <0 || rating >3500){
            throw new BusinessException(
                "Рейтинг должен быть от 0 до 3500"
            );
        }
    }

}

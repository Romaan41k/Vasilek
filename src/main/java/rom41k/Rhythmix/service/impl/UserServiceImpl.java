package rom41k.Rhythmix.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rom41k.Rhythmix.database.entity.User;
import rom41k.Rhythmix.dto.UpdateUserRequest;
import rom41k.Rhythmix.repository.UserRepository;
import rom41k.Rhythmix.service.interfaces.UserService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<User> allUsers() {
        return new ArrayList<>(userRepository.findAll());
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User updateUser(Long userId, UpdateUserRequest updateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Обновляем имя, если оно не пустое
        if (updateRequest.getName() != null && !updateRequest.getName().isEmpty()) {
            user.setName(updateRequest.getName());
        }

        // Обновляем пароль, если передан новый и старый пароль
        if (updateRequest.getOldPassword() != null && updateRequest.getNewPassword() != null) {
            if (!passwordEncoder.matches(updateRequest.getOldPassword(), user.getPassword())) {
                throw new RuntimeException("Old password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
        }

        // Обновляем email, если он не пустой
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isEmpty()) {
            if (userRepository.existsByEmail(updateRequest.getEmail())) {
                throw new RuntimeException("Email is already taken");
            }
            user.setEmail(updateRequest.getEmail());
        }

        return userRepository.save(user);
    }


    @Override
    public void updatePassword(Long id, String newPassword) {
        userRepository.findById(id).ifPresentOrElse(account -> {
            account.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(account);
        }, () -> {
            throw new RuntimeException("User not found with id: " + id);
        });
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        userRepository.delete(user);
    }
}

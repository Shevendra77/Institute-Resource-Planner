package com.example.irp.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.irp.entity.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> { // 🟢 Type changed from Long to Integer to match entity id

    // Existing login method (passwords same ho sakte hain, email unique check karega)
    Optional<User> findByUserEmailAndPassword(String userEmail, String password);

    // 🟢 Naya method: Registration ke waqt duplicate email check karne ke liye
    boolean existsByUserEmail(String userEmail);
}
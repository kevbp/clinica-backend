package com.clinica.personal.repository;

import com.clinica.personal.model.Personal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalRepository extends JpaRepository<Personal, Long> {

    Optional<Personal> findByKeycloakUserId(String keycloakUserId);
}

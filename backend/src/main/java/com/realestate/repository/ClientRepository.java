package com.realestate.repository;

import com.realestate.entity.Client;
import com.realestate.entity.Client.ClientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    List<Client> findByType(ClientType type);
    
    Optional<Client> findByEmail(String email);
    
    List<Client> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
        String firstName, String lastName);
    
    List<Client> findByPhoneContaining(String phone);
}

package com.example.zavodbanksystems.repos;

import com.example.zavodbanksystems.databasemodel.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
}

package com.cakemake.dao;

import com.cakemake.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long>{

    @Query("SELECT u FROM User u WHERE u.email = ?1 and u.password = ?2")
    User findUserByEmailAndPassword(String email, String password);
}



package com.project.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;

import com.project.entities.Contact;
import com.project.entities.User;

@EnableJpaRepositories
public interface UserRepository extends JpaRepository<User,Integer>{
	
	@Query("select u from User u Where u.email = :email")
	public User getUserBYUserName(@Param("email") String email);
	


}

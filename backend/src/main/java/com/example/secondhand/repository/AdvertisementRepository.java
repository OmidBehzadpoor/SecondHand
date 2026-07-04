package com.example.secondhand.repository;

import com.example.secondhand.model.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

}

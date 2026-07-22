package com.example.secondhand.repository;

import com.example.secondhand.model.City;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CityRepositoryTest {

    @Autowired
    private CityRepository cityRepository;

    @Test
    void save_shouldPersistCity_whenNameIsUnique() {
        City saved = cityRepository.saveAndFlush(City.builder().name("Tehran").build());

        assertTrue(cityRepository.findById(saved.getId()).isPresent());
    }

    @Test
    void save_shouldThrowDataIntegrityViolationException_whenNameIsDuplicated() {
        cityRepository.saveAndFlush(City.builder().name("Tehran").build());

        City duplicate = City.builder().name("Tehran").build();

        assertThrows(DataIntegrityViolationException.class,
                () -> cityRepository.saveAndFlush(duplicate));
    }
}

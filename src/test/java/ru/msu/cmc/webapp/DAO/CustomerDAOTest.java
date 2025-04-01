package ru.msu.cmc.webapp.DAO;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.msu.cmc.webapp.entities.Customer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations="classpath:application.properties")
public class CustomerDAOTest {

    @Autowired
    private CustomerDAO customerDAO;

    @Test
    void getCustomerByUsernameTest() {
        Customer customer = customerDAO.getCustomerByUsername("ivan");
        assertNotNull(customer);
        customer = customerDAO.getCustomerByUsername("hihihihahaha");
        assertNull(customer);
    }

    @Test
    void getCustomerByEmailTest() {
        Customer customer = customerDAO.getCustomerByEmail("ivan@example.com");
        assertNotNull(customer);
        customer = customerDAO.getCustomerByEmail("hihihihahaha@example.com");
        assertNull(customer);
    }

    @Test
    void getAllCustomersByFullNameTest() {
        List<Customer> customers = customerDAO.getAllCustomersByFullName("Иван");
        assertEquals(1, customers.size());
        customers = customerDAO.getAllCustomersByFullName("hihihihahaha");
        assertEquals(0, customers.size());
    }
}
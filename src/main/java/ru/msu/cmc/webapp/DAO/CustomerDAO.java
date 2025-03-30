package ru.msu.cmc.webapp.DAO;

import ru.msu.cmc.webapp.entities.Customer;
import ru.msu.cmc.webapp.entities.Order;

import java.util.List;

public interface CustomerDAO extends CommonDAO<Customer, Long> {
    Customer getCustomerByUsername(String uname);
    Customer getCustomerByEmail(String email);
    List<Customer> getAllCustomersByFullName(String fullName);
}
package ru.msu.cmc.webapp.DAO;

import ru.msu.cmc.webapp.entities.Customer;
import ru.msu.cmc.webapp.entities.Order;
import java.util.List;

public interface OrderDAO extends CommonDAO<Order, Long> {
    List<Order> getOrdersByCustomer(Customer customer, int limit);
}
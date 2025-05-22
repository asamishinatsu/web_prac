package ru.msu.cmc.webapp.DAO.implementation;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import ru.msu.cmc.webapp.DAO.OrderDAO;
import ru.msu.cmc.webapp.entities.Customer;
import ru.msu.cmc.webapp.entities.Genre;
import ru.msu.cmc.webapp.entities.Order;
import java.util.Collections;
import java.util.List;

@Repository
public class OrderDAOImpl extends CommonDAOImpl<Order, Long> implements OrderDAO {

    public OrderDAOImpl() {
        super(Order.class);
    }

    @Override
    public List<Order> getOrdersByCustomer(Customer customer, int limit) {
        if (customer == null || customer.getId() == null) {
            return Collections.emptyList();
        }

        try (Session session = sessionFactory.openSession()) {
            Query<Order> query = session
                    .createQuery("FROM Order o WHERE o.customer = :customer ORDER BY o.orderDate DESC", Order.class)
                    .setParameter("customer", customer);

            if (limit > 0) {
                query.setMaxResults(limit);
            }

            return query.getResultList();
        }
    }
}
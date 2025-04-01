package ru.msu.cmc.webapp.DAO.implementation;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import ru.msu.cmc.webapp.DAO.CustomerDAO;
import ru.msu.cmc.webapp.entities.Customer;
import ru.msu.cmc.webapp.entities.Genre;
import ru.msu.cmc.webapp.entities.Order;

import java.util.Collections;
import java.util.List;

@Repository
public class CustomerDAOImpl extends CommonDAOImpl<Customer, Long> implements CustomerDAO {

    public CustomerDAOImpl() {
        super(Customer.class);
    }

    @Override
    public Customer getCustomerByUsername(String uname) {
        try (Session session = sessionFactory.openSession()) {
            List<Customer> customerList = session
                    .createQuery("FROM Customer WHERE username = :gotUname", Customer.class)
                    .setParameter("gotUname", uname)
                    .getResultList();
            return !customerList.isEmpty() ? customerList.get(0) : null;
        }
    }
    @Override
    public Customer getCustomerByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            List<Customer> customerList = session
                    .createQuery("FROM Customer WHERE email = :gotEmail", Customer.class)
                    .setParameter("gotEmail", email)
                    .getResultList();
            return !customerList.isEmpty() ? customerList.get(0) : null;
        }
    }
    @Override
    public List<Customer> getAllCustomersByFullName(String fullName) {
        try (Session session = sessionFactory.openSession()) {
            Query<Customer> query = session
                    .createQuery("FROM Customer WHERE fullName LIKE :gotFullName", Customer.class)
                    .setParameter("gotFullName", likeExpr(fullName));
            return !query.getResultList().isEmpty() ? query.getResultList() : Collections.emptyList();
        }
    }
}
package com.sms.billing.service;

import com.sms.billing.entity.Customer;
import com.sms.billing.repository.CustomerRepository;
import com.sms.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final CustomerRepository customerRepository;

    public Customer getCustomerSummary(String organizationId){
        Customer customer = new Customer();

        Long totalCustomers = customerRepository.countCustomers(organizationId);

        return customer;
    }

}

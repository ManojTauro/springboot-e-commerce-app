package com.manoj.ecommerce.customer;

import com.manoj.ecommerce.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

@RequiredArgsConstructor
@Service
public class CustomerService {
    private final CustomerRepository repository;
    private final CustomerMapper mapper;

    public String saveCustomer(CustomerRequest request) {
        var customer = repository.save(mapper.toCustomer(request));

        return customer.getId();
    }

    public void updateCustomer(CustomerRequest request) {
        var customer = getCustomerById(request.id());

        mergeCustomer(customer, request);

        repository.save(customer);
    }

    private void mergeCustomer(Customer customer, CustomerRequest request) {
        if (StringUtils.isNotBlank(request.firstname())) customer.setFirstname(request.firstname());
        if (StringUtils.isNotBlank(request.lastname())) customer.setFirstname(request.lastname());
        if (StringUtils.isNotBlank(request.email())) customer.setFirstname(request.email());
        if (request.address() != null) customer.setAddress(request.address());
    }

    public List<CustomerResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::fromCustomer)
                .collect(Collectors.toList());
    }

    public CustomerResponse getCustomer(String id) {
        var customer = getCustomerById(id);

        return mapper.fromCustomer(customer);
    }

    private Customer getCustomerById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(
                        format("No customer found with provided id %s", id)
                ));
    }

    public void deleteCustomer(String id) {
        repository.deleteById(id);
    }
}

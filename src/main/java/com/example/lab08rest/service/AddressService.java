package com.example.lab08rest.service;

import com.example.lab08rest.dto.AddressDTO;

import java.util.List;

public interface AddressService {

    List<AddressDTO> readAll();
    AddressDTO update(AddressDTO addressDTO);
    AddressDTO create(AddressDTO addressDTO);
    List<AddressDTO> readByStartsWith(String address);
    List<AddressDTO> readAllByCustomerId(Long id);
    List<AddressDTO> readAllByCustomerIdAndName(Long customerId, String name);

}

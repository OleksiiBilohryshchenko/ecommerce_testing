package com.example.lab08rest.service.impl;

import com.example.lab08rest.dto.AddressDTO;
import com.example.lab08rest.entity.Address;
import com.example.lab08rest.mapper.MapperUtil;
import com.example.lab08rest.repository.AddressRepository;
import com.example.lab08rest.service.AddressService;
import com.example.lab08rest.service.CustomerService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final MapperUtil mapperUtil;
    private final CustomerService customerService;

    public AddressServiceImpl(AddressRepository addressRepository, MapperUtil mapperUtil, CustomerService customerService) {
        this.addressRepository = addressRepository;
        this.mapperUtil = mapperUtil;
        this.customerService = customerService;
    }

    @Override
    public List<AddressDTO> readAll() {
        return addressRepository.findAll().stream()
                .map(address -> mapperUtil.convert(address, new AddressDTO()))
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO update(AddressDTO addressDTO) {

        Address address = mapperUtil.convert(addressDTO, new Address());

        if (!customerService.existById(addressDTO.getCustomerId())){
            throw new RuntimeException("Customer couldn't be found");
        }
        Address updatedAddress = addressRepository.save(address);

        return mapperUtil.convert(updatedAddress, new AddressDTO());

    }

    @Override
    public AddressDTO create(AddressDTO addressDTO) {

        Address address = mapperUtil.convert(addressDTO, new Address());

        if (!customerService.existById(addressDTO.getCustomerId())){
            throw new RuntimeException("Customer couldn't be found");
        }
        Address savedAddress = addressRepository.save(address);

        return mapperUtil.convert(savedAddress, new AddressDTO());

    }

    @Override
    public List<AddressDTO> readByStartsWith(String address) {
        return addressRepository.findAllByStreetStartingWith(address)
                .stream().map(address1 -> mapperUtil.convert(address1, new AddressDTO()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AddressDTO> readAllByCustomerId(Long id) {
        return addressRepository.retrieveByCustomerId(id)
                .stream().map(address -> mapperUtil.convert(address, new AddressDTO()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AddressDTO> readAllByCustomerIdAndName(Long customerId, String name) {
        return addressRepository.findAllByCustomerIdAndName(customerId, name)
                .stream().map(address -> mapperUtil.convert(address, new AddressDTO()))
                .collect(Collectors.toList());
    }
}

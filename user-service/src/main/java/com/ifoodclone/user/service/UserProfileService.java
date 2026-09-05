package com.ifoodclone.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ifoodclone.user.dto.AddressDto;
import com.ifoodclone.user.dto.UserProfileDto;
import com.ifoodclone.user.entity.Address;
import com.ifoodclone.user.entity.UserProfile;
import com.ifoodclone.user.repository.AddressRepository;
import com.ifoodclone.user.repository.UserProfileRepository;

@Service
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final AddressRepository addressRepository;

    public UserProfileService(UserProfileRepository userProfileRepository, AddressRepository addressRepository) {
        this.userProfileRepository = userProfileRepository;
        this.addressRepository = addressRepository;
    }

    // auth-service owns user registration and never emits an event this service could
    // react to, so there's no proactive creation hook -- the profile row is created
    // lazily the first time it's needed (read or write).
    public UserProfile getOrCreateProfile(Long userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseGet(() -> userProfileRepository.save(UserProfile.builder().userId(userId).build()));
    }

    public UserProfile updateProfile(Long userId, UserProfileDto.UpdateRequest request) {
        UserProfile profile = getOrCreateProfile(userId);

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        return userProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public List<Address> listAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address addAddress(Long userId, AddressDto.CreateRequest request) {
        if (request.isDefault()) {
            clearDefaultAddress(userId);
        }

        Address address = Address.builder()
                .userId(userId)
                .label(request.getLabel())
                .street(request.getStreet())
                .number(request.getNumber())
                .complement(request.getComplement())
                .neighborhood(request.getNeighborhood())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .isDefault(request.isDefault())
                .build();

        address = addressRepository.save(address);

        if (request.isDefault()) {
            UserProfile profile = getOrCreateProfile(userId);
            profile.setDefaultAddressId(address.getId());
            userProfileRepository.save(profile);
        }

        return address;
    }

    public Address updateAddress(Long userId, Long addressId, AddressDto.UpdateRequest request) {
        Address address = getOwnedAddress(userId, addressId);

        if (request.getLabel() != null) {
            address.setLabel(request.getLabel());
        }
        if (request.getStreet() != null) {
            address.setStreet(request.getStreet());
        }
        if (request.getNumber() != null) {
            address.setNumber(request.getNumber());
        }
        if (request.getComplement() != null) {
            address.setComplement(request.getComplement());
        }
        if (request.getNeighborhood() != null) {
            address.setNeighborhood(request.getNeighborhood());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getState() != null) {
            address.setState(request.getState());
        }
        if (request.getZipCode() != null) {
            address.setZipCode(request.getZipCode());
        }

        return addressRepository.save(address);
    }

    public void deleteAddress(Long userId, Long addressId) {
        Address address = getOwnedAddress(userId, addressId);
        addressRepository.delete(address);

        UserProfile profile = getOrCreateProfile(userId);
        if (address.getId().equals(profile.getDefaultAddressId())) {
            profile.setDefaultAddressId(null);
            userProfileRepository.save(profile);
        }
    }

    public Address setDefaultAddress(Long userId, Long addressId) {
        Address address = getOwnedAddress(userId, addressId);

        clearDefaultAddress(userId);
        address.setIsDefault(true);
        address = addressRepository.save(address);

        UserProfile profile = getOrCreateProfile(userId);
        profile.setDefaultAddressId(address.getId());
        userProfileRepository.save(profile);

        return address;
    }

    private void clearDefaultAddress(Long userId) {
        addressRepository.findByUserId(userId).stream()
                .filter(Address::getIsDefault)
                .forEach(existing -> {
                    existing.setIsDefault(false);
                    addressRepository.save(existing);
                });
    }

    private Address getOwnedAddress(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));
    }
}

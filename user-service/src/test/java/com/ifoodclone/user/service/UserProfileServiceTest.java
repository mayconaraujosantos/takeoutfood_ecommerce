package com.ifoodclone.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ifoodclone.user.dto.AddressDto;
import com.ifoodclone.user.dto.UserProfileDto;
import com.ifoodclone.user.entity.Address;
import com.ifoodclone.user.entity.UserProfile;
import com.ifoodclone.user.repository.AddressRepository;
import com.ifoodclone.user.repository.UserProfileRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Profile Service Tests")
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AddressRepository addressRepository;

    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileService(userProfileRepository, addressRepository);
    }

    @Nested
    @DisplayName("getOrCreateProfile")
    class GetOrCreateProfileTests {

        @Test
        @DisplayName("Should return existing profile when found")
        void shouldReturnExisting() {
            UserProfile existing = UserProfile.builder().id(1L).userId(42L).build();
            when(userProfileRepository.findByUserId(42L)).thenReturn(Optional.of(existing));

            UserProfile result = userProfileService.getOrCreateProfile(42L);

            assertThat(result).isSameAs(existing);
        }

        @Test
        @DisplayName("Should lazily create a profile when missing")
        void shouldCreateWhenMissing() {
            when(userProfileRepository.findByUserId(42L)).thenReturn(Optional.empty());
            when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

            UserProfile result = userProfileService.getOrCreateProfile(42L);

            assertThat(result.getUserId()).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("updateProfile")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update bio and avatarUrl")
        void shouldUpdateFields() {
            UserProfile existing = UserProfile.builder().id(1L).userId(42L).build();
            when(userProfileRepository.findByUserId(42L)).thenReturn(Optional.of(existing));
            when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

            UserProfile result = userProfileService.updateProfile(42L,
                    UserProfileDto.UpdateRequest.builder().bio("Hello").avatarUrl("http://a.png").build());

            assertThat(result.getBio()).isEqualTo("Hello");
            assertThat(result.getAvatarUrl()).isEqualTo("http://a.png");
        }
    }

    @Nested
    @DisplayName("addAddress")
    class AddAddressTests {

        @Test
        @DisplayName("Should save a non-default address without touching the profile")
        void shouldSaveNonDefault() {
            when(addressRepository.save(any(Address.class))).thenAnswer(inv -> {
                Address a = inv.getArgument(0);
                a.setId(1L);
                return a;
            });

            Address result = userProfileService.addAddress(42L, AddressDto.CreateRequest.builder()
                    .label("Casa").street("Rua A").city("SP").state("SP").build());

            assertThat(result.getUserId()).isEqualTo(42L);
            assertThat(result.getIsDefault()).isFalse();
            verify(userProfileRepository, org.mockito.Mockito.never()).save(any());
        }

        @Test
        @DisplayName("Should unset previous default and update profile when marked as default")
        void shouldSetAsDefault() {
            Address previousDefault = Address.builder().id(1L).userId(42L).isDefault(true).build();
            when(addressRepository.findByUserId(42L)).thenReturn(List.of(previousDefault));
            when(addressRepository.save(any(Address.class))).thenAnswer(inv -> {
                Address a = inv.getArgument(0);
                if (a.getId() == null) {
                    a.setId(2L);
                }
                return a;
            });
            when(userProfileRepository.findByUserId(42L))
                    .thenReturn(Optional.of(UserProfile.builder().id(1L).userId(42L).build()));
            when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

            Address result = userProfileService.addAddress(42L, AddressDto.CreateRequest.builder()
                    .label("Trabalho").street("Rua B").city("SP").state("SP").isDefault(true).build());

            assertThat(previousDefault.getIsDefault()).isFalse();
            assertThat(result.getIsDefault()).isTrue();
        }
    }

    @Nested
    @DisplayName("deleteAddress")
    class DeleteAddressTests {

        @Test
        @DisplayName("Should throw when address does not belong to the user")
        void shouldThrowWhenNotOwned() {
            when(addressRepository.findByIdAndUserId(1L, 42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userProfileService.deleteAddress(42L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    @Nested
    @DisplayName("setDefaultAddress")
    class SetDefaultAddressTests {

        @Test
        @DisplayName("Should mark the address as default and update the profile")
        void shouldMarkAsDefault() {
            Address address = Address.builder().id(1L).userId(42L).isDefault(false).build();
            when(addressRepository.findByIdAndUserId(1L, 42L)).thenReturn(Optional.of(address));
            when(addressRepository.findByUserId(42L)).thenReturn(List.of(address));
            when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userProfileRepository.findByUserId(42L))
                    .thenReturn(Optional.of(UserProfile.builder().id(1L).userId(42L).build()));
            when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

            Address result = userProfileService.setDefaultAddress(42L, 1L);

            assertThat(result.getIsDefault()).isTrue();
        }
    }
}

package com.ifoodclone.auth.service;

import com.ifoodclone.auth.entity.User;
import com.ifoodclone.auth.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Usuário com email '%s' não encontrado", username)));

        if (!user.getActive()) {
            throw new UsernameNotFoundException(
                    String.format("Usuário com email '%s' está inativo", username));
        }

        return user;
    }

    /**
     * Carrega usuário por email ou telefone
     */
    public UserDetails loadUserByEmailOrPhone(String identifier) throws UsernameNotFoundException {
        User user = userRepository.findByEmailOrPhone(identifier)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Usuário com identificador '%s' não encontrado", identifier)));

        if (!user.getActive()) {
            throw new UsernameNotFoundException(
                    String.format("Usuário com identificador '%s' está inativo", identifier));
        }

        return user;
    }

    /**
     * Carrega usuário por ID
     */
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Usuário com ID '%d' não encontrado", userId)));

        if (!user.getActive()) {
            throw new UsernameNotFoundException(
                    String.format("Usuário com ID '%d' está inativo", userId));
        }

        return user;
    }
}
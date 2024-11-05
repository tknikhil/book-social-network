package in.tkn.book_network.security;

import in.tkn.book_network.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository repository;
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userEamil) throws UsernameNotFoundException {
        return repository.findByEmail(userEamil)
                .orElseThrow(()->new UsernameNotFoundException("User Not Found"));
    }
}

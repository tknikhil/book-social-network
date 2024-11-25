package in.tkn.book_network.auth;

import in.tkn.book_network.email.EmailService;
import in.tkn.book_network.email.EmailTemplateName;
import in.tkn.book_network.role.RoleRepository;
import in.tkn.book_network.user.Token;
import in.tkn.book_network.user.TokenRepository;
import in.tkn.book_network.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import in.tkn.book_network.user.User;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void registerUser(RegistrationRequest requst) throws MessagingException {
        //todo better exception handling
        var userRole = roleRepository.findByRole("User").orElseThrow(()->new IllegalArgumentException("Role User was not Initialized"));
        var user = User.builder()
                .firstName(requst.getFirstName())
                .lastName(requst.getLastName())
                .email(requst.getEmail())
                .password(passwordEncoder.encode(requst.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();

        userRepository.save(user);
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);
        //sendEmail
        emailService.sendEmail(user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation");
    }

    private String generateAndSaveActivationToken(User user) {
//        generateToken
        String generateToken =generateActivationToken(6);
        var token = Token.builder()
                .token(generateToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);
        return generateToken;
    }

    private String generateActivationToken(int token) {
    String characters="0123456789";
    StringBuilder tokenBuilder =new StringBuilder();
    SecureRandom secureRandom =new SecureRandom();
    for(int i=0;i<token;i++){
        int randomIndex = secureRandom.nextInt(characters.length());
        tokenBuilder.append(characters.charAt(randomIndex));
    }
    return tokenBuilder.toString();
    }
}

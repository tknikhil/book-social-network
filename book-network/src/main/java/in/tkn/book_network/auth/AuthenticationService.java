package in.tkn.book_network.auth;

import in.tkn.book_network.email.EmailService;
import in.tkn.book_network.email.EmailTemplateName;
import in.tkn.book_network.role.RoleRepository;
import in.tkn.book_network.security.JwtService;
import in.tkn.book_network.user.Token;
import in.tkn.book_network.user.TokenRepository;
import in.tkn.book_network.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import in.tkn.book_network.user.User;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void registerUser(RegistrationRequest request) throws MessagingException {
        //todo better exception handling
        log.info("Role Table value :"+roleRepository.findByName("USER"));
        var userRole = roleRepository.findByName("USER").orElseThrow(()->new IllegalArgumentException("Role User was not Initialized"));

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .build();

        userRepository.save(user);
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user);
        log.info(user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation");
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
        log.info(token.getToken(),"Generated Token");
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

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var auth=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

    var claims= new HashMap<String,Object>();
    var user= ((User)auth.getPrincipal());
    claims.put("fullName",user.getFullName());
    var jwtToken = jwtService.generateToken(claims,user);
        return AuthenticationResponse
                .builder()
                .token(jwtToken)
                .build();
    }
//issue  : After Activation token has expired new created token was not going stored in database
//Reason: it's because of generateAndSaveActivationToken method being called inside sendValidationEmail after throwing an exception (RuntimeException) for an expired token
// @Transaction  and RuntimeException Once RuntimeException hits @Transaction rollback the transaction any database changes made during this method execution
// sol 1:
//    for now need to handle proper way for exception
    @Transactional(noRollbackFor = RuntimeException.class)
    public void activateAccount(String token) throws MessagingException {
        Token savedToken= tokenRepository.findByToken(token).orElseThrow(()->new RuntimeException("Invalid token"));
        if(LocalDateTime.now().isAfter(savedToken.getExpiresAt())){
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation token has expired ,New Token has been sent to the same email address");
        }
        var user = userRepository.findById(savedToken.getUser().getId()).orElseThrow(()->new UsernameNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
        savedToken.setValidatedAt(LocalDateTime.now());
        log.error(user.toString());
        log.info(savedToken.getToken());
        tokenRepository.save(savedToken);
    }


}

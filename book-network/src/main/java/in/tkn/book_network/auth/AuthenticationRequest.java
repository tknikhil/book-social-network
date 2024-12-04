package in.tkn.book_network.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthenticationRequest {
    @Email(message="not a correct email")
    @NotEmpty(message="Email Name is mandatory")
    @NotBlank(message="Email Name is mandatory")
    private String email;
    @NotEmpty(message="Password Name is mandatory")
    @NotBlank(message="Password Name is mandatory")
    @Size(min=8,message = "Password should be minimum 8 char long")
    private String password;
}

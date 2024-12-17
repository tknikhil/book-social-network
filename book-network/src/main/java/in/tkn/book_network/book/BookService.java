package in.tkn.book_network.book;

import in.tkn.book_network.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    public Integer save(BookRequest request, Authentication connectedUser) {
        User user =((User) connectedUser.getPrincipal());
        Book book=bookMapper.toBook(request );
        book.setOwner(user);
        return bookRepository.save(book).getId();
    }
}

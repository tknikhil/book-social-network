package in.tkn.book_network.feedback;

import in.tkn.book_network.book.Book;
import in.tkn.book_network.book.BookRepository;
import in.tkn.book_network.exception.OperationNotPermittedException;
import in.tkn.book_network.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedbackService {

private final BookRepository bookRepository;
private final FeedbackMapper feedbackMapper;
private final FeedbackRepository feedbackRepository;

    public Integer save(FeedbackRequest request, Authentication connectedUser) {
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(()-> new EntityNotFoundException("No book found with ID :"+request.bookId()));
        if(book.isArchive()||!book.isShareable()){
            throw  new OperationNotPermittedException("You cannot give feedback for an archived or not shareable book");
        }
        User user =((User) connectedUser.getPrincipal());
        if(Objects.equals(book.getOwner().getId(),user.getId())){
            throw new OperationNotPermittedException("You cannot give feedback to your own book");
        }

        Feedback feedback=feedbackMapper.toFeedback(request);
        return feedbackRepository.save(feedback).getId();
    }
}

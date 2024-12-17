package in.tkn.book_network.book;

import org.springframework.stereotype.Service;

@Service
public class BookMapper {
    public Book toBook(BookRequest request) {
        return Book.builder()
                .id(request.Id())
                .title(request.title())
                .authorName(request.authorName())
                .synopsis(request.synopsis())
                .archive(false)
                .shareable(request.shareable())
                .build();
    }
}

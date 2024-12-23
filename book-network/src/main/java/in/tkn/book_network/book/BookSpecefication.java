package in.tkn.book_network.book;

import org.springframework.data.jpa.domain.Specification;

public class BookSpecefication {

    public static Specification<Book> withOwnerId(Integer ownerId){
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("owner").get("id"),ownerId);
    }
}

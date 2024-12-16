package in.tkn.book_network.feedback;

import in.tkn.book_network.book.Book;
import in.tkn.book_network.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Feedback extends BaseEntity {

    private Double rate;
    private String comment;
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;


}

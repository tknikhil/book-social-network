package in.tkn.book_network.book;

import in.tkn.book_network.common.BaseEntity;
import in.tkn.book_network.feedback.Feedback;
import in.tkn.book_network.histroy.BookTransactionHistory;
import in.tkn.book_network.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public class Book extends BaseEntity {
    private String title;
    private String authorName;
    private String isbn;
    private String synopsis;
    private String bookCover;
    private boolean archive;
    private boolean shareable;

    @ManyToOne
    @JoinColumn(name="owner_id")
    private User owner;
    @OneToMany(mappedBy = "book")
    private List<Feedback> feedbacks;
    @OneToMany(mappedBy = "book")
    private List<BookTransactionHistory> histories;

    @Transient
    public  double getRate(){
        if(feedbacks ==null ||feedbacks.isEmpty()){
            return 0.0;
        }
        var rate= this.feedbacks.stream()
                .mapToDouble(Feedback::getRate)
                .average()
                .orElse(0.0);
        double roundedRate =Math.round(rate *10.0)/10.0;
        return roundedRate;
    }
}

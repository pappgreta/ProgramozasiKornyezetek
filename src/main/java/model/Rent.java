package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Rent {

    @Id
    @GeneratedValue
    private int id;

    @Column(nullable = false)
    private String bookId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private Date startDate;

    @Column(nullable = false)
    private Date deadline;
}

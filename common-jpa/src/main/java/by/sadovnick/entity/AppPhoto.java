package by.sadovnick.entity;

import lombok.*;

import javax.persistence.*;

/**
 * Сущность документа для сохранения в таблицу бд.
 */
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_photo")
public class AppPhoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String telegramField;
    @OneToOne
    private BinaryContent binaryContent;
    private Integer fileSize;
}

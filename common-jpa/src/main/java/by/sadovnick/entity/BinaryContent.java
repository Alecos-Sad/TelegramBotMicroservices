package by.sadovnick.entity;

import lombok.*;

import javax.persistence.*;

/**
 * Документ в виде потока байт.
 */
@Builder
@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "binary_content")
public class BinaryContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private byte[] fileArrayOfByte;
}

package ru.practicum.ewm.dto.comment;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class CommentDto {
    private Long id;
    @NotBlank
    @Length(min = 1, max = 2000)
    private String text;
    private String authorName;
    private String created;
    private String updated;
}

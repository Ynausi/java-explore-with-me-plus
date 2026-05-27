package ru.practicum.dto.users;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String email;

    private Long id;

    private String name;
}

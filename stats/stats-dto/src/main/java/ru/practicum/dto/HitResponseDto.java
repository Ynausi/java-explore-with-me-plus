package ru.practicum.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HitResponseDto {
    private String app;
    private String uri;
    private String ip;
    private String timestamp;
}

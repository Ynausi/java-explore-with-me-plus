package ru.practicum.controller.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.rating.RatingDto;
import ru.practicum.dto.users.UserShortDto;

import java.util.List;

@RestController
@RequestMapping("/admin/ratings")
@RequiredArgsConstructor
public class AdminRatingController {

    @GetMapping
    public List<UserShortDto> getUsersByReactionOrByEventIds(@RequestParam List<Long> eventIds,
                                                             @RequestParam String reaction) {
        return null;
    }

    @GetMapping
    public List<RatingDto> getReactionsByUsersIds(@RequestParam List<Long> usersIds) {
        return null;
    }

}

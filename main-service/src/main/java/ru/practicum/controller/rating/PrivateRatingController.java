package ru.practicum.controller.rating;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.Event;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrivateRatingController {

    @PostMapping("{userId}/ratings/{eventId}/{reaction}")
    public Event saveReaction(@PathVariable Long eventId,
                              @PathVariable Long userId,
                              @PathVariable String reaction) {
        return null;
    }

    @DeleteMapping("{userId}/ratings/{eventId}")
    public void deleteReaction(@PathVariable Long eventId,
                               @PathVariable Long userId) {

    }

    @GetMapping("{userId}/ratings")
    public List<Event> getEventsUserLiked(@PathVariable Long userId) {
        return null;
    }
}

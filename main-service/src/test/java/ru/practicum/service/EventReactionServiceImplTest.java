package ru.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.event.EventReactionDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventReactionRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UsersRepository;
import ru.practicum.repository.event.EventRepository;
import ru.practicum.service.event.EventReactionServiceImpl;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventReactionServiceImplTest extends BaseServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventReactionRepository eventReactionRepository;

    @Mock
    private ParticipationRequestRepository requestRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventReactionServiceImpl eventReactionService;

    @ParameterizedTest
    @CsvSource({
            "LIKE, addLikeEvent",
            "DISLIKE, addDislikeEvent"
    })
    void test_addReaction_shouldSuccess(ReactionType reactionType, String methodName) throws Exception {
        Long userId = 1L;
        Long eventId = 1L;

        User user1 = makeUniqueUser(userId);
        User initiator = makeUniqueUser(2L);
        Event event1 = makeEvent(eventId, initiator, 1L);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user1));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event1));
        when(eventReactionRepository.findByReactorIdAndEventId(userId, eventId))
                .thenReturn(Optional.empty());
        when(requestRepository.existsByRequesterIdAndEventIdAndStatus(
                userId, eventId, RequestStatus.CONFIRMED))
                .thenReturn(true);

        EventReaction reaction = makeReaction(1L, user1, event1, reactionType);

        EventReactionDto expectedDto = new EventReactionDto();
        expectedDto.setReactionType(reactionType);
        expectedDto.setReactor(userId);
        expectedDto.setEvent(eventId);

        when(eventMapper.toReaction(eq(userId), eq(eventId), any(ReactionType.class)))
                .thenReturn(reaction);
        when(eventReactionRepository.save(any(EventReaction.class))).thenReturn(reaction);
        when(eventMapper.toReactionDto(any(EventReaction.class))).thenReturn(expectedDto);

        Method method = eventReactionService.getClass().getMethod(methodName, Long.class, Long.class);
        EventReactionDto result = (EventReactionDto) method.invoke(eventReactionService, userId, eventId);

        assertThat(result).isNotNull();
        assertThat(result.getReactionType()).isEqualTo(reactionType);
        verify(eventReactionRepository, times(1)).save(argThat(savedReaction ->
                savedReaction.getReactor().getId().equals(userId) &&
                        savedReaction.getReactionType() == reactionType
        ));
    }

    @Test
    void test_addLikeEvent_whenUserNotParticipant_shouldThrowException() {
        Long userId = 1L;
        Long eventId = 1L;

        when(usersRepository.findById(userId)).thenReturn(Optional.of(makeUniqueUser(userId)));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(makeEvent(eventId, makeUniqueUser(2L), 1L)));
        when(requestRepository.existsByRequesterIdAndEventIdAndStatus(
                userId, eventId, RequestStatus.CONFIRMED))
                .thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> eventReactionService.addLikeEvent(userId, eventId));

        verify(eventReactionRepository, never()).save(any());
    }

    @Test
    void test_addDislikeEvent_whenUserNotParticipant_shouldThrowException() {
        Long userId = 1L;
        Long eventId = 1L;

        when(usersRepository.findById(userId)).thenReturn(Optional.of(makeUniqueUser(userId)));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(makeEvent(eventId, makeUniqueUser(2L), 1L)));
        when(requestRepository.existsByRequesterIdAndEventIdAndStatus(
                userId, eventId, RequestStatus.CONFIRMED))
                .thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> eventReactionService.addDislikeEvent(userId, eventId));

        verify(eventReactionRepository, never()).save(any());
    }



}

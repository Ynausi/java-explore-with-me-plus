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
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventReactionRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UsersRepository;
import ru.practicum.repository.event.EventRepository;
import ru.practicum.service.event.EventReactionServiceImpl;

import java.lang.reflect.InvocationTargetException;
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

    private User user;
    private User initiator;
    private Event event;
    private EventReaction reaction;
    private EventReactionDto expectedDto;

    private void prepareData(Long userId, Long eventId, ReactionType typeEntity, ReactionType typeDto) {
        user = makeUniqueUser(userId);
        initiator = makeUniqueUser(2L);
        event = makeEvent(eventId, initiator, 1L);

        reaction = makeReaction(1L, user, event, typeEntity);

        expectedDto = new EventReactionDto();
        expectedDto.setReactionType(typeDto);
        expectedDto.setReactor(userId);
        expectedDto.setEvent(eventId);
    }

    @ParameterizedTest
    @CsvSource({
            "LIKE, addLikeEvent",
            "DISLIKE, addDislikeEvent"
    })
    void test_addReaction_shouldSuccess(ReactionType reactionType, String methodName) throws Exception {
        Long userId = 1L;
        Long eventId = 1L;

        prepareData(userId, eventId, reactionType, reactionType);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventReactionRepository.findByReactorIdAndEventId(userId, eventId))
                .thenReturn(Optional.empty());
        when(requestRepository.existsByRequesterIdAndEventIdAndStatus(
                userId, eventId, RequestStatus.CONFIRMED))
                .thenReturn(true);

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

    @ParameterizedTest
    @CsvSource({
            "addLikeEvent",
            "addDislikeEvent"
    })
    void test_addReaction_whenUserNotParticipant_shouldThrowException(String methodName) throws Exception {
        Long userId = 1L;
        Long eventId = 1L;

        when(usersRepository.findById(userId)).thenReturn(Optional.of(makeUniqueUser(userId)));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(makeEvent(eventId, makeUniqueUser(2L), 1L)));
        when(requestRepository.existsByRequesterIdAndEventIdAndStatus(
                userId, eventId, RequestStatus.CONFIRMED))
                .thenReturn(false);

        Method method = eventReactionService.getClass().getMethod(methodName, Long.class, Long.class);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> method.invoke(eventReactionService, userId, eventId));

        assertThat(exception.getCause()).isInstanceOf(BadRequestException.class);

        verify(eventReactionRepository, never()).save(any());
    }

    @Test
    void test_addLikeEvent_whenExistsDislike_shouldChangeToLike() {
        Long userId = 1L;
        Long eventId = 1L;

        prepareData(userId, eventId, ReactionType.DISLIKE, ReactionType.LIKE);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        when(eventReactionRepository.findByReactorIdAndEventId(userId, eventId))
                .thenReturn(Optional.of(reaction));
        when(requestRepository.existsByRequesterIdAndEventIdAndStatus(
                userId, eventId, RequestStatus.CONFIRMED))
                .thenReturn(true);

        when(eventMapper.toReactionDto(any(EventReaction.class))).thenReturn(expectedDto);

        EventReactionDto result = eventReactionService.addLikeEvent(userId, eventId);

        assertThat(result).isNotNull();
        assertThat(result.getReactionType()).isEqualTo(ReactionType.LIKE);
    }

    @Test
    void test_deleteDislikeEvent_shouldSuccess() {
        Long userId = 1L;
        Long eventId = 1L;

        prepareData(userId, eventId, ReactionType.DISLIKE, null);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventReactionRepository.findByReactorIdAndEventId(userId, eventId))
                .thenReturn(Optional.of(reaction));

        eventReactionService.deleteDislikeEvent(userId, eventId);

        verify(eventReactionRepository, times(1)).delete(reaction);
    }

    @Test
    void test_deleteLikeEvent_shouldSuccess() {
        Long userId = 1L;
        Long eventId = 1L;

        prepareData(userId, eventId, ReactionType.LIKE, null);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventReactionRepository.findByReactorIdAndEventId(userId, eventId))
                .thenReturn(Optional.of(reaction));

        eventReactionService.deleteLikeEvent(userId, eventId);

        verify(eventReactionRepository, times(1)).delete(reaction);
    }

    @Test
    void test_deleteLikeEvent_whenNoReaction_shouldThrowNotFoundException() {
        Long userId = 1L;
        Long eventId = 1L;

        when(usersRepository.findById(userId)).thenReturn(Optional.of(makeUniqueUser(userId)));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(makeEvent(eventId, makeUniqueUser(2L), 1L)));
        when(eventReactionRepository.findByReactorIdAndEventId(userId, eventId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> eventReactionService.deleteLikeEvent(userId, eventId));

        verify(eventReactionRepository, never()).delete(any());
    }

    @Test
    void test_deleteLikeEvent_whenReactionTypeMismatch_shouldThrowNotFoundException() {
        Long userId = 1L;
        Long eventId = 1L;

        prepareData(userId, eventId, ReactionType.DISLIKE, null);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventReactionRepository.findByReactorIdAndEventId(userId, eventId))
                .thenReturn(Optional.of(reaction));

        assertThrows(NotFoundException.class,
                () -> eventReactionService.deleteLikeEvent(userId, eventId));

        verify(eventReactionRepository, never()).delete(any());
    }

    @Test
    void test_addDislikeEvent_whenExistsLike_shouldChangeToDislike() {
        Long userId = 1L;
        Long eventId = 1L;

        prepareData(userId, eventId, ReactionType.LIKE, ReactionType.DISLIKE);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        when(eventReactionRepository.findByReactorIdAndEventId(userId, eventId))
                .thenReturn(Optional.of(reaction));
        when(requestRepository.existsByRequesterIdAndEventIdAndStatus(
                userId, eventId, RequestStatus.CONFIRMED))
                .thenReturn(true);

        when(eventMapper.toReactionDto(any(EventReaction.class))).thenReturn(expectedDto);

        EventReactionDto result = eventReactionService.addDislikeEvent(userId, eventId);

        assertThat(result).isNotNull();
        assertThat(result.getReactionType()).isEqualTo(ReactionType.DISLIKE);
    }

    @Test
    void test_addLikeEvent_whenSameReactionExists_shouldThrowException() {
        Long userId = 1L;
        Long eventId = 1L;

        prepareData(userId, eventId, ReactionType.LIKE, ReactionType.LIKE);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(makeEvent(eventId, makeUniqueUser(2L), 1L)));
        when(requestRepository.existsByRequesterIdAndEventIdAndStatus(
                userId, eventId, RequestStatus.CONFIRMED))
                .thenReturn(true);

        when(eventReactionRepository.findByReactorIdAndEventId(userId, eventId))
                .thenReturn(Optional.of(reaction));

        assertThrows(ConflictException.class,
                () -> eventReactionService.addLikeEvent(userId, eventId));

        verify(eventReactionRepository, never()).save(any(EventReaction.class));
        verify(eventMapper, never()).toReactionDto(any(EventReaction.class));
    }

    @Test
    void test_addDislikeEvent_whenSameReactionExists_shouldThrowException() {
        Long userId = 1L;
        Long eventId = 1L;

        prepareData(userId, eventId, ReactionType.DISLIKE, ReactionType.DISLIKE);

        when(usersRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(makeEvent(eventId, makeUniqueUser(2L), 1L)));
        when(requestRepository.existsByRequesterIdAndEventIdAndStatus(
                userId, eventId, RequestStatus.CONFIRMED))
                .thenReturn(true);

        when(eventReactionRepository.findByReactorIdAndEventId(userId, eventId))
                .thenReturn(Optional.of(reaction));

        assertThrows(ConflictException.class,
                () -> eventReactionService.addDislikeEvent(userId, eventId));

        verify(eventReactionRepository, never()).save(any(EventReaction.class));
        verify(eventMapper, never()).toReactionDto(any(EventReaction.class));
    }

    @Test
    void test_addLikeEvent_whenUserNotFound_shouldThrowNotFoundException() {
        Long userId = 999L;
        Long eventId = 1L;

        when(usersRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> eventReactionService.addLikeEvent(userId, eventId));

        verify(eventReactionRepository, never()).findByReactorIdAndEventId(any(), any());
        verify(eventReactionRepository, never()).save(any());
    }

    @Test
    void test_addLikeEvent_whenEventNotFound_shouldThrowNotFoundException() {
        Long userId = 1L;
        Long eventId = 999L;

        when(usersRepository.findById(userId)).thenReturn(Optional.of(makeUniqueUser(userId)));
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> eventReactionService.addLikeEvent(userId, eventId));

        verify(eventReactionRepository, never()).findByReactorIdAndEventId(any(), any());
        verify(eventReactionRepository, never()).save(any());
    }

    @Test
    void test_deleteLikeEvent_whenUserNotFound_shouldThrowNotFoundException() {
        Long userId = 999L;
        Long eventId = 1L;

        when(usersRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> eventReactionService.deleteLikeEvent(userId, eventId));

        verify(eventReactionRepository, never()).findByReactorIdAndEventId(any(), any());
    }

    @Test
    void test_deleteLikeEvent_whenEventNotFound_shouldThrowNotFoundException() {
        Long userId = 1L;
        Long eventId = 999L;

        when(usersRepository.findById(userId)).thenReturn(Optional.of(makeUniqueUser(userId)));
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> eventReactionService.deleteLikeEvent(userId, eventId));

        verify(eventReactionRepository, never()).findByReactorIdAndEventId(any(), any());
    }
}

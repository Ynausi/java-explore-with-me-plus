package ru.practicum.mapper.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.mapper.users.UserMapper;
import ru.practicum.model.Event;

import static ru.practicum.constant.Constants.DATE_TIME_FORMAT;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {

    @Mapping(target = "eventDate", source = "eventDate", dateFormat = DATE_TIME_FORMAT)
    EventShortDto toEventShortDto(Event event);

    Event toEvent(NewEventDto newEventDto);

    EventFullDto toEventFullDto(Event event);
}
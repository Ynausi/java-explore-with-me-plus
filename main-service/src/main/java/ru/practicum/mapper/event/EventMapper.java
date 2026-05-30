package ru.practicum.mapper.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.LocationDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.mapper.category.CategoryMapper;
import ru.practicum.mapper.users.UserMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Location;

import static ru.practicum.constant.Constants.DATE_TIME_FORMAT;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {

    @Mapping(target = "eventDate", source = "eventDate", dateFormat = DATE_TIME_FORMAT)
    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "category", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "state", source = "eventState")
    EventFullDto toEventFullDto(Event event);

    LocationDto toLocationDto(Location location);

    Location toLocation(LocationDto locationDto);
}
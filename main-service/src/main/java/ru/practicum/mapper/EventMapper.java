package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.LocationDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.model.Event;
import ru.practicum.model.Location;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {

    @Mapping(target = "eventDate", source = "eventDate")
    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "category", ignore = true)
    Event toEvent(NewEventDto newEventDto);

    @Mapping(target = "state", source = "eventState")
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    EventFullDto toEventFullDto(Event event);

    LocationDto toLocationDto(Location location);

    Location toLocation(LocationDto locationDto);
}
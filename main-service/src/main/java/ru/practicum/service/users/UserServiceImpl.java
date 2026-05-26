package ru.practicum.service.users;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.users.NewUserRequest;
import ru.practicum.dto.users.UserDto;
import ru.practicum.exception.UserNotFoundException;
import ru.practicum.mapper.users.UserMapper;
import ru.practicum.model.QUser;
import ru.practicum.model.User;
import ru.practicum.repository.UsersRepository;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final UserMapper userMapper;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        User newUser = usersRepository.save(userMapper.newUserRequestToUser(newUserRequest));

        return userMapper.userToUserDto(newUser);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        QUser user = QUser.user;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        List<User> users;

        if (ids != null && !ids.isEmpty()) {
            booleanBuilder.and(user.id.in(ids));

            users = StreamSupport.stream(
                    usersRepository.findAll(booleanBuilder).spliterator(), false
            ).toList();

            return users.stream()
                    .map(userMapper::userToUserDto)
                    .toList();
        }

        users = queryFactory.selectFrom(user)
                .where(booleanBuilder)
                .offset(from)
                .limit(size)
                .fetch();

        return users.stream()
                .map(userMapper::userToUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        usersRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException(String.format("User with id=%s was not found", userId)));

        usersRepository.deleteById(userId);
    }
}

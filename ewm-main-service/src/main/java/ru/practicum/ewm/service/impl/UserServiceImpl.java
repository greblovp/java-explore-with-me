package ru.practicum.ewm.service.impl;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.user.NewUserRequest;
import ru.practicum.ewm.dto.user.UserDto;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.model.QUser;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.UserService;
import ru.practicum.ewm.util.PageGetter;
import ru.practicum.ewm.util.UserMapper;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public Collection<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        QUser qUser = QUser.user;
        BooleanBuilder predicate = new BooleanBuilder();

        if (ids != null && !ids.isEmpty()) {
            predicate.and(qUser.id.in(ids));
        }

        Pageable page = PageGetter.getPageRequest(from, size, Sort.by("id"));

        Iterable<User> users = userRepository.findAll(predicate, page);

        return UserMapper.toUserDto(users);
    }

    @Transactional
    @Override
    public UserDto createUser(NewUserRequest newUserRequest) {
        User newUser = UserMapper.toUser(newUserRequest);
        return UserMapper.toUserDto(userRepository.save(newUser));
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        checkUserId(userId);
        userRepository.deleteById(userId);
    }

    private void checkUserId(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ObjectNotFoundException("Пользователь с ID = " + id + " не найден.");
        }
    }
}

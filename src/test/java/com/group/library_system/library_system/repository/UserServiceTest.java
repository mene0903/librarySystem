package com.group.library_system.library_system.repository;

import com.group.library_system.library_system.service.UserService;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy; // 예외 테스트용


@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User createTestUser(String id) {
        return new User(null, "Kim", id, "eee", null, "010-0000", null,0,1 ,3);
    }

    @Test
    @DisplayName("회원가입 성공")
    void userServiceSuccess() {

        //given
        User user1 = createTestUser("123456789");
        User user2 = createTestUser("11111");

        //when
        userService.registerUser(user1);
        userService.registerUser(user2);

        //then
        Optional<User> id = userRepository.findById("123456789");
        Assertions.assertThat(id).isPresent();
        User foundUser = id.get();
        Assertions.assertThat(foundUser.getId()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("회원가입 실패")
    void userServiceFail() {

        //given
        User user1 = createTestUser("123456789");
        User user2 = createTestUser("11111");
        User user3 = createTestUser("123456789");

        //when
        userService.registerUser(user1);
        userService.registerUser(user2);

        //then
        assertThatThrownBy(() -> userService.registerUser(user3))
                .isInstanceOf(IllegalArgumentException.class) // 예외 타입 검증
                .hasMessageContaining("아이디가 중복되었습니다."); // 메시지 검증

    }


    @Test
    @DisplayName("로그인 성공")
    void loginSuccess() {
        //given
        User user = createTestUser("123456789");
        //when
        userService.registerUser(user);
        //then
        Assertions.assertThat(userService.login("123456789", "eee")).isEqualTo(user);
    }

    @Test
    @DisplayName("로그인 실패 (비밀번호)")
    void loginFailPassword() {
        //given
        User user = createTestUser("123456789");
        //when
        userService.registerUser(user);
        //then
        assertThatThrownBy(() ->
                userService.login("123456789", "wrongPass")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("로그인 실패 (아이디)")
    void loginFailID() {
        //given
        User user = createTestUser("123456789");
        //when
        userService.registerUser(user);
        //then
        assertThatThrownBy(() ->
                userService.login("dowi", "eee")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("아이디가 존재하지 않습니다.");
    }

}

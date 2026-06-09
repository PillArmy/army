package io.army.example.type.postgre;

import io.army.example.type.annotation.CurrentSession;
import io.army.example.type.domain.PostgreTypes;
import io.army.session.SyncSession;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@SpringBootTest(useMainMethod = ALWAYS)
@Transactional
@Rollback
public class PostgreArrayTests {


    @Test
    public void integerArray(@CurrentSession SyncSession session) {
        Integer[] array;
        array = new Integer[]{1, 2, 3};
        PostgreTypes postgre;
        postgre = new PostgreTypes();
        postgre.integerArray = array;
        session.save(postgre);
    }


}

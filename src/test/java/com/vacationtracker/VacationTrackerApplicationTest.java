
import com.vacationtracker.VacationTrackerApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class) // JUnit 5 with Spring support
@SpringBootTest(classes = VacationTrackerApplication.class)
public class VacationTrackerApplicationTest {

    @Autowired
    private VacationTrackerApplication application;

    @Test
    public void contextLoads() {
        assertThat(application).isNotNull();
    }
}

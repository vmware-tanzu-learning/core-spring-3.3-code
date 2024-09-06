package rewards.internal.reward;


import config.AppConfig;
import config.DbConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Integration test for the JDBC-based rewards repository implementation.
 * Verifies that the JdbcRewardRepository works with its underlying components
 * and that Spring is configuring things properly.
 */
@ActiveProfiles("jpa")
@SpringJUnitConfig(classes = {AppConfig.class, DbConfig.class})
public class JdbcRewardRepositoryIntegrationTests extends
		AbstractRewardRepositoryTests {

	@Test
	@Override
	public void testProfile() {
		assertEquals(JdbcRewardRepository.TYPE, rewardRepository.getInfo(), "JDBC expected but found " + rewardRepository.getInfo());
	}

}

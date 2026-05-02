package org.soup.ssu.bench;

import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;

@JdbcTest
@AutoConfigureEmbeddedDatabase(
    type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES,
    provider = AutoConfigureEmbeddedDatabase.DatabaseProvider.EMBEDDED,
    refresh = AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_EACH_TEST_METHOD
)
public abstract class RepositoryTest {
}

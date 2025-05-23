package edu.kdkce.openelectivefcfs.src.repository;

import edu.kdkce.openelectivefcfs.src.model.Settings;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Repository
    public class SettingsRepository {
    private final DynamoDbTable<Settings> table;

    public SettingsRepository(DynamoDbEnhancedClient client) {
        this.table = client.table("Settings", TableSchema.fromBean(Settings.class));
    }


    public Optional<Settings> findById(String id) {
        return Optional.ofNullable(table.getItem(r -> r.key(k -> k.partitionValue(id))));
    }

    public void save(Settings newSettings) {
        table.putItem(newSettings);
    }

    public void update(Settings settings) {
        table.updateItem(settings);
    }
}
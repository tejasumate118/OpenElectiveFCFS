package edu.kdkce.openelectivefcfs.repository;

import edu.kdkce.openelectivefcfs.model.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.Optional;

@Repository
public class UserRepository  {
    private final DynamoDbTable<User> table;

    public UserRepository(DynamoDbEnhancedClient client) {
        this.table = client.table("User", TableSchema.fromBean(User.class));
    }

    public Optional<User> findByEmail(String email) {
        Expression filterExpression = Expression.builder()
                .expression("email = :email")
                .expressionValues(Map.of(":email", AttributeValue.builder().s(email).build()))
                .build();

        User item = table.scan(r -> r.filterExpression(filterExpression))
                .items().stream().findFirst().orElse(null);
        return Optional.ofNullable(item);
    }

    public Optional<User> findById(String userId) {
        User item = table.getItem(r -> r.key(k -> k.partitionValue(userId)));
        return Optional.ofNullable(item);
    }

    public void save(User user) {
        table.putItem(user);
    }
    public void update(User user) {
        table.updateItem(user);
    }
}
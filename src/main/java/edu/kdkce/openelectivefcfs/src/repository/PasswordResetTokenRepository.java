package edu.kdkce.openelectivefcfs.src.repository;

import edu.kdkce.openelectivefcfs.src.model.PasswordResetToken;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;


@Repository
public class  PasswordResetTokenRepository  {
    private final DynamoDbTable<PasswordResetToken> table;

    public PasswordResetTokenRepository(DynamoDbEnhancedClient client) {
        this.table = client.table("PasswordResetToken", TableSchema.fromBean(PasswordResetToken.class));
    }

    public void deleteAllByUserId(String id) {
        table.scan().items().stream()
                .filter(token -> token.getUserId().equals(id))
                .forEach(table::deleteItem);
    }

    public void save(PasswordResetToken passwordResetToken) {
        table.putItem(passwordResetToken);
    }

    public Optional<PasswordResetToken> findByToken(String token) {
        //token is not partition key, so we need to scan the table
        PasswordResetToken item = table.scan().items().stream()
                .filter(passwordResetToken -> passwordResetToken.getToken().equals(token))
                .findFirst()
                .orElse(null);
        return Optional.ofNullable(item);
    }

    public void delete(PasswordResetToken passwordResetToken) {
        table.deleteItem(passwordResetToken);
    }
}

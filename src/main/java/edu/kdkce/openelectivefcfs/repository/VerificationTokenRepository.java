package edu.kdkce.openelectivefcfs.repository;

import edu.kdkce.openelectivefcfs.model.VerificationToken;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;


@Repository
public class VerificationTokenRepository {
    private final DynamoDbTable<VerificationToken> table;

    public VerificationTokenRepository(DynamoDbEnhancedClient client) {
        this.table = client.table("VerificationToken", TableSchema.fromBean(VerificationToken.class));
    }

    public void deleteAllByUserId(String id) {
        table.scan().items().stream()
                .filter(token -> token.getUserId().equals(id))
                .forEach(table::deleteItem);
    }

    public void save(VerificationToken verificationToken) {
        table.putItem(verificationToken);
    }

    public Optional<VerificationToken> findByToken(String token) {
        VerificationToken item = table.scan().items().stream()
                .filter(verificationToken -> verificationToken.getToken().equals(token))
                .findAny()
                .orElse(null);
        return Optional.ofNullable(item);
    }

    public void delete(VerificationToken verificationToken) {
        table.deleteItem(verificationToken);
    }
}

package edu.kdkce.openelectivefcfs.repository;

import edu.kdkce.openelectivefcfs.model.Elective;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
public class ElectiveRepository {
    private final DynamoDbTable<Elective> table;
    private static final Logger logger = LoggerFactory.getLogger(ElectiveRepository.class);

    public ElectiveRepository(DynamoDbEnhancedClient client) {
        this.table = client.table("Elective", TableSchema.fromBean(Elective.class));
    }

    public void save(Elective elective) {
        table.putItem(elective);
    }

    public void deleteById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        table.deleteItem(r -> r.key(key));

    }

    public Optional<Elective> findById(String id) {

        Elective item = table.getItem(Key.builder().partitionValue(id).build());
        // Debug
        if (item == null) {
            System.out.println("Item not found with id: " + id);
        } else {
            System.out.println("Item found: " + item);
        }
        return Optional.ofNullable(item);
    }

    public List<Elective> findAll() {
        List<Elective> electives = new ArrayList<>();
        table.scan().items().forEach(electives::add);
         return electives;
    }

    public Elective getElectivesById(String id) {
        return table.getItem(Key.builder().partitionValue(id).build());
    }

    public void update(Elective elective) {
        logger.info("Updating elective: {}" , elective.getId());
        table.updateItem(elective);
        logger.info("Elective updated: {}" , elective.getId());
    }
}

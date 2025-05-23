package edu.kdkce.openelectivefcfs.src.repository;

import edu.kdkce.openelectivefcfs.src.model.AllocationCycle;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;


import java.util.List;
import java.util.Optional;


@Repository
public class AllocationCycleRepository{
    private final DynamoDbTable<AllocationCycle> table;

    public AllocationCycleRepository(DynamoDbEnhancedClient client) {
        this.table = client.table("AllocationCycle", TableSchema.fromBean(AllocationCycle.class));
    }

    public Optional<AllocationCycle> findByCycleName(String cycleName) {
        AllocationCycle item = table.getItem(r -> r.key(k -> k.partitionValue(cycleName)));
        return Optional.ofNullable(item);

    }

    public AllocationCycle save(AllocationCycle newCycle) {
        table.putItem(newCycle);
        return newCycle;
    }

    public List<AllocationCycle> findAll() {
        return table.scan().items().stream().toList();
    }

    public Optional<AllocationCycle> findById(String cycleId) {
        AllocationCycle item = table.getItem(r -> r.key(k -> k.partitionValue(cycleId)));
        return Optional.ofNullable(item);
    }
}

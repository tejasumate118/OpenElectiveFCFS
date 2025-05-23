package edu.kdkce.openelectivefcfs.repository;

import edu.kdkce.openelectivefcfs.model.PastAllocation;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;

import java.util.List;

@Repository
public class PastAllocationRepository{
    private final DynamoDbEnhancedClient enhancedClient;
    private final DynamoDbTable<PastAllocation> table;

    public PastAllocationRepository(DynamoDbEnhancedClient enhancedClient, DynamoDbEnhancedClient client) {
        this.enhancedClient = enhancedClient;
        this.table = client.table("PastAllocation", TableSchema.fromBean(PastAllocation.class));
    }

    public void saveAll(List<PastAllocation> pastAllocations) {
        BatchWriteItemEnhancedRequest.Builder batchWriteBuilder = BatchWriteItemEnhancedRequest.builder();
        WriteBatch.Builder<PastAllocation> writeBatchBuilder = WriteBatch.builder(PastAllocation.class)
                .mappedTableResource(table);
        for (PastAllocation pastAllocation : pastAllocations) {
            writeBatchBuilder.addPutItem(r->r.item(pastAllocation));

        }
        batchWriteBuilder.addWriteBatch(writeBatchBuilder.build());
        enhancedClient.batchWriteItem(batchWriteBuilder.build());

    }

    public List<PastAllocation> findByAllocationCycleId(String  cycleId) {
        return table.scan().items().stream()
                .filter(pastAllocation -> pastAllocation.getAllocationCycleId().equals(cycleId))
                .toList();

    }
}

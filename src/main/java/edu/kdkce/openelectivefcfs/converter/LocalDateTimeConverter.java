package edu.kdkce.openelectivefcfs.converter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public AttributeValue transformFrom(LocalDateTime input) {
        return AttributeValue.fromS(input.format(FORMATTER));
    }

    @Override
    public LocalDateTime transformTo(AttributeValue input) {
        return LocalDateTime.parse(input.s(), FORMATTER);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }
    @Override
    public EnhancedType<LocalDateTime> type() {
        return EnhancedType.of(LocalDateTime.class);
    }
}

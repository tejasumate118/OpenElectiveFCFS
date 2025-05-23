package edu.kdkce.openelectivefcfs.converter;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Override
    public AttributeValue transformFrom(ZonedDateTime input) {
        return AttributeValue.fromS(input.format(FORMATTER));
    }

    @Override
    public ZonedDateTime transformTo(AttributeValue input) {
        return ZonedDateTime.parse(input.s(), FORMATTER);
    }

    @Override
    public AttributeValueType attributeValueType() {
        return AttributeValueType.S;
    }

    @Override
    public EnhancedType<ZonedDateTime> type() {
        return EnhancedType.of(ZonedDateTime.class);
    }
}

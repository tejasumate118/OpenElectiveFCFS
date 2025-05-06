package edu.kdkce.openelectivefcfs.src.converter;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


@Converter(autoApply = true)
public class ZonedDateTimeConverter implements AttributeConverter<ZonedDateTime, String> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    @Override
    public String convertToDatabaseColumn(ZonedDateTime zonedDateTime) {
        return (zonedDateTime == null) ? null : zonedDateTime.withZoneSameInstant(IST_ZONE).format(FORMATTER);
    }

    @Override
    public ZonedDateTime convertToEntityAttribute(String dbData) {
        return (dbData == null) ? null : ZonedDateTime.parse(dbData, FORMATTER);
    }
}

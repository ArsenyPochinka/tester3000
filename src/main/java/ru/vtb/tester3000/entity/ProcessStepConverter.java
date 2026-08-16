package ru.vtb.tester3000.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ProcessStepConverter implements AttributeConverter<ProcessEntity.Step, String> {

    @Override
    public String convertToDatabaseColumn(ProcessEntity.Step attribute) {
        return attribute == null ? null : attribute.getDbValue();
    }

    @Override
    public ProcessEntity.Step convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ProcessEntity.Step.fromDbValue(dbData);
    }
}

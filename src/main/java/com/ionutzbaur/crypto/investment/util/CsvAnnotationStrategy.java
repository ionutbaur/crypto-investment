package com.ionutzbaur.crypto.investment.util;

import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class CsvAnnotationStrategy<T> extends HeaderColumnNameTranslateMappingStrategy<T> {

    private final boolean writeHeader;

    public CsvAnnotationStrategy(Class<T> type, boolean writeHeader) {
        this.writeHeader = writeHeader;
        Map<String, String> map = new HashMap<>();
        for (Field field : type.getDeclaredFields()) {
            map.put(field.getName(), field.getName());
        }
        setType(type);
        setColumnMapping(map);
        setColumnOrderOnWrite(Comparator.reverseOrder());
    }

    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        String[] result = super.generateHeader(bean);
        if (writeHeader) {
            for (int i = 0; i < result.length; i++) {
                result[i] = getColumnName(i).toLowerCase();
            }
            return result;
        } else {
            return new String[0];
        }
    }

}

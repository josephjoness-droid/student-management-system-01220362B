package com.sms2.domain;

import java.util.ArrayList;
import java.util.List;

public class ImportResult {
    private int successCount;
    private int errorCount;
    private List<String> errors = new ArrayList<>();

    public void incrementSuccess()       { successCount++; }
    public void addError(String error)   { errors.add(error); errorCount++; }
    public int getSuccessCount()         { return successCount; }
    public int getErrorCount()           { return errorCount; }
    public List<String> getErrors()      { return errors; }
}
